package org.idea.irpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcDecoder;
import org.idea.irpc.framework.core.common.RpcEncoder;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.RpcProtocol;
import org.idea.irpc.framework.core.common.cache.CommonClientCache;
import org.idea.irpc.framework.core.common.config.client.ClientConfig;
import org.idea.irpc.framework.core.common.constants.RpcConstants;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.AbstractRegister;
import org.idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.idea.irpc.framework.interfaces.DataService;
import org.idea.irpc.framework.interfaces.HelloService;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;

/**
 * @author cyang
 */
@Slf4j
@Data
public class Client {
    private ClientConfig clientConfig;

    // todo: 注册到注册中心. 如: zookeeper. why?
    private AbstractRegister registryService;
    // 监听服务变化
    private IRpcListenerLoader rpcListenerLoader;

    private Bootstrap bootstrap = new Bootstrap();

    public RpcReference initClientApplication() throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new RpcEncoder())
                        .addLast(new RpcDecoder())
                        .addLast(new ClientHandler());
            }
        });

        // todo: 在哪使用?
        rpcListenerLoader = new IRpcListenerLoader();
        rpcListenerLoader.init();

        // todo: 从配置文件导入配置

//        ChannelFuture future = bootstrap.connect(clientConfig.getServerAddress(), clientConfig.getServerPort()).sync();
//        log.info("client start success on {}:{}", clientConfig.getServerAddress(), clientConfig.getServerPort());

//        this.startSendThread(future);

        // todo: 实现javassist
        return new RpcReference(new JDKProxyFactory());
    }

    /**
     * 在开始application前要先订阅服务, 才能获取zookeeper上已有的服务
     *
     * @param serviceBean service interface
     */
    public void doSubscribe(Class<?> serviceBean) {
        if (registryService == null) {
            registryService = new ZookeeperRegister(clientConfig.getRegisterAddress());
        }

        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter(RpcConstants.HOST, CommonUtils.getIpAddress());

        registryService.subscribe(url);
    }

    public void doConnectService() {
        for (String providerServiceName : SUBSCRIBE_SERVICE_LIST) {
            registryService.getProviderIps(providerServiceName).forEach(ip -> {
                try {
                    ConnectionHandler.connect(providerServiceName, ip);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            URL url = new URL();
            url.setServiceName(providerServiceName);
            // 监听服务. 监听到就会发送事件event, 触发连接更新
            registryService.doAfterSubscribe(url);
        }
    }


    /**
     * 异步发送远程调用
     *
     * @author Cheng Yang
     */
    private static class AsyncSentJob implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    RpcInvocation data = CommonClientCache.SEND_QUEUE.take();
                    String json = JSON.toJSONString(data);
                    RpcProtocol protocol = new RpcProtocol(json.getBytes());
                    ChannelFuture future = ConnectionHandler.getChannelFuture(data.getTargetServiceName());
                    if (future.channel().isActive()) {
                        future.channel().writeAndFlush(protocol);
                    } else {
                        // 连接断开，处理重连或退出
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // 日志记录，优雅退出
            }
        }
    }

    /**
     * 启动一个独立线程，异步将待发送队列中的消息通过指定的 ChannelFuture 发送到服务器。
     * 该线程会持续从发送队列中取出消息并发送，直到应用关闭。
     *
     * @param future 用于与服务器通信的 ChannelFuture，代表已建立的连接
     */
    private void startSendThread() {
        new Thread(new AsyncSentJob()).start();
    }

    public static void main(String[] args) throws Exception {
        // todo: 形成配置文件, 在initClientApplication加载
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setServerAddress("127.0.0.1");
        clientConfig.setServerPort(9999);
        clientConfig.setRegisterAddress("127.0.0.1:2181");
        clientConfig.setApplicationName("cyan-irpc-client");

        Client client = new Client();
        client.setClientConfig(clientConfig);

        RpcReference rpcReference = client.initClientApplication();

        // 订阅服务
        client.doSubscribe(HelloService.class);
        client.doSubscribe(DataService.class);

        ConnectionHandler.setBootstrap(client.getBootstrap());

        // 连接订阅的服务, 上一步订阅服务会把服务放进本地缓存, 使用前要先连接服务
        client.doConnectService();

        //
        client.startSendThread();

        HelloService helloService = rpcReference.getProxy(HelloService.class);
        DataService dataService = rpcReference.getProxy(DataService.class);
        for (int i = 0; i < 10; i++) {
            log.info("helloService: {}", helloService.sayHello("irpc"));
            log.info("dataService: {}", dataService.hello("irpc"));
        }
    }
}
