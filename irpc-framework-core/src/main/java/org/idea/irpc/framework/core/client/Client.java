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
import org.idea.irpc.framework.core.common.config.ClientConfig;
import org.idea.irpc.framework.core.common.config.PropertiesBoostrap;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.proxy.ProxyFactory;
import org.idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.AbstractRegister;
import org.idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.idea.irpc.framework.core.route.RandomRouteImpl;
import org.idea.irpc.framework.core.route.RotateRouteImpl;
import org.idea.irpc.framework.core.route.Selector;
import org.idea.irpc.framework.core.serialize.fastjson.FastJsonSerializerFactory;
import org.idea.irpc.framework.core.serialize.kryo.KryoSerializeFactory;
import org.idea.irpc.framework.interfaces.DataService;
import org.idea.irpc.framework.interfaces.HelloService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.*;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.*;

/**
 * @author cyang
 */
@Slf4j
@Data
public class Client {
    // 在 initClientApplication 进行初始化
    private ClientConfig clientConfig;

    private Bootstrap bootstrap = new Bootstrap();
    // 注册中心. 如: zookeeper. why?
    // 4 个核心api, client 和 server 各两个
    private AbstractRegister registryService;
    // 监听服务变化
    private IRpcListenerLoader rpcListenerLoader;

    // 1. 初始化
    /**
     *
     * @return 代理工厂的包装类
     */
    public RpcReference initClientApplication() {

        // 1. netty
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

        // 2. 监听服务中心的变化. init 方法注册各种事件的监听器
        rpcListenerLoader = new IRpcListenerLoader();
        rpcListenerLoader.init();

        // 3. 从配置文件导入配置
        clientConfig = PropertiesBoostrap.loadClientConfig();

        // 4. 根据配置返回代理工厂包装类
        // 5月4日: 这一步几乎和前面没有关系,也不依赖于前面, 放在一起属实是很牵强的感觉
        // todo: 实现javassist
        ProxyFactory proxyFactory;
        if (JDK_PROXY.equals(clientConfig.getProxyType())) {
            proxyFactory = new JDKProxyFactory();
        } else {
            throw new RuntimeException("unknown proxy type");
        }

        return new RpcReference(proxyFactory);
    }

    // 2. 初始化路由, 序列化方法
    /// - route. e.g. 1. random, 2. rotate
    /// - serializer. e.g. 1. fastjson, 2. kryo, 3. todo, protobuf
    public void initCacheConfig() {
        String routeStrategy = clientConfig.getRouteStrategy();
        if (RANDOM_ROUTE_STRATEGY.equalsIgnoreCase(routeStrategy)) {
            I_ROUTE = new RandomRouteImpl();
        } else if (ROTATE_ROUTE_STRATEGY.equalsIgnoreCase(routeStrategy)) {
            I_ROUTE = new RotateRouteImpl();
        }

        String serializer = clientConfig.getClientSerializer();
        switch (serializer) {
            case FAST_JSON_SERIALIZE_STRATEGY:
                CLIENT_SERIALIZE_FACTORY = new FastJsonSerializerFactory();
                break;
            case KRYO_SERIALIZE_STRATEGY:
                CLIENT_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
            default:
                throw new RuntimeException("no match serializer for " + serializer);
        }
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
        // 连接服务, 只需要设置一次, 这是工具类
        ConnectionHandler.setBootstrap(bootstrap);

        for (URL providerUrl : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerAddresses = registryService.getProviderAddresses(providerUrl.getServiceName());
            providerAddresses.forEach(addr -> {
                try {
                    ConnectionHandler.connect(providerUrl.getServiceName(), addr);
                } catch (InterruptedException e) {
                    log.error("[doConnectService]", e);
                }
            });

            // todo
            I_ROUTE.refreshRouteArr(new Selector(providerUrl.getServiceName()));

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
     * 启动一个独立线程，异步将待发送队列中的消息通过指定的 ChannelFuture 发送到服务器。
     * 该线程会持续从发送队列中取出消息并发送，直到应用关闭。
     *
     * <p>用于与服务器通信的 ChannelFuture 从本地缓存 CONNECT_MAP获取，代表已建立的连接
     */
    public void startSendThread() {
        // todo 用线程池
        new Thread(new AsyncSentJob()).start();
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
                    RpcInvocation data = SEND_QUEUE.take();
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

    public static void main(String[] args) throws Exception {
        // 1. 创建 client 并加载配置
        Client client = new Client();

        // 2. bootstrap, listener, 获得代理工厂
        RpcReference rpcReference = client.initClientApplication();

        // 3. 路由方法, 序列化方法初始化
        client.initCacheConfig();

        // 订阅服务, 读取注册中心的服务地址
        client.doSubscribe(HelloService.class);
        client.doSubscribe(DataService.class);

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
