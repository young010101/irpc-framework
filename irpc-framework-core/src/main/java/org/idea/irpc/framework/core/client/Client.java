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
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.AbstractRegister;
import org.idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.idea.irpc.framework.core.route.RandomRouteImpl;
import org.idea.irpc.framework.core.route.RotateRouteImpl;
import org.idea.irpc.framework.interfaces.DataService;
import org.idea.irpc.framework.interfaces.HelloService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.I_ROUTE;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.*;

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

    public RpcReference initClientApplication() {

        EventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group).channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline()
                        .addLast(new RpcEncoder())
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

        // 5月4日: 这一步几乎和前面没有关系,也不依赖于前面, 放在一起属实是很牵强的感觉
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

        HashMap<String, String> params = new HashMap<>(Map.of(HOST, CommonUtils.getIpAddress()));
        URL url = new URL(clientConfig.getApplicationName(), serviceBean.getName(), params);

        registryService.subscribe(url);
    }

    public void doConnectService() {
        for (URL providerUrl : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerAddresses = registryService.getProviderAddresses(providerUrl.getServiceName());
            providerAddresses.forEach(addr -> {
                try {
                    ConnectionHandler.connect(providerUrl.getServiceName(), addr);
                } catch (InterruptedException e) {
                    log.error("[doConnectService]", e);
                }
            });
            URL url = new URL();
            // todo 原作者加入了 serviceName + "/provider" 没get到
            url.setServiceName(providerUrl.getServiceName());
            // todo这里的JSON是fastjson, 不是fastjson2. 要到doAfterSubscribe里解包
            url.addParameter(PROVIDER_ADDRESSES_JSON_STRING, JSON.toJSONString(providerAddresses));
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
                    // 这里使用 Random 获取, 在路由层将改为按权重获取
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
     * <p>
     * 用于与服务器通信的 ChannelFuture 从本地缓存 CONNECT_MAP获取，代表已建立的连接
     * </p>
     */
    private void startSendThread() {
        new Thread(new AsyncSentJob()).start();
    }

    private void initRouteStrategy() {
        String routeStrategy = clientConfig.getRouteStrategy();
        if (RANDOM_ROUTE_STRATEGY.equalsIgnoreCase(routeStrategy)) {
            I_ROUTE = new RandomRouteImpl();
        } else if (ROTATE_ROUTE_STRATEGY.equalsIgnoreCase(routeStrategy)) {
            I_ROUTE = new RotateRouteImpl();
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. client 配置
        // todo: 形成配置文件, 在initClientApplication加载
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setServerAddress("127.0.0.1");
        clientConfig.setServerPort(9999);
        clientConfig.setRegisterAddress("127.0.0.1:2181");
        // 名字不重要,完全没有影响,作用应该是为了连接使用
        clientConfig.setApplicationName("cyan-client");
        clientConfig.setRouteStrategy(ROTATE_ROUTE_STRATEGY);

        // 2. 创建 client 并加载配置
        Client client = new Client();
        client.setClientConfig(clientConfig);

        client.initRouteStrategy();

        // 3. bootstrap, listener, 获得代理工厂
        RpcReference rpcReference = client.initClientApplication();

        // 订阅服务
        client.doSubscribe(HelloService.class);
        client.doSubscribe(DataService.class);

        // 只需要设置一次, 这是工具类
        ConnectionHandler.setBootstrap(client.getBootstrap());

        // 连接订阅的服务, 上一步订阅服务会把服务放进本地缓存, 使用前要先连接服务
        client.doConnectService();

        //
        client.startSendThread();

        HelloService helloService = rpcReference.getProxy(HelloService.class);
        DataService dataService = rpcReference.getProxy(DataService.class);
        for (int i = 0; i < 10; i++) {
            log.info("helloService: {}", helloService.sayHello("client"));
            log.info("dataService: {}", dataService.hello("client"));
        }
    }
}
