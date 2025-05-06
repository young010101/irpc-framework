package org.idea.irpc.framework.core.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcDecoder;
import org.idea.irpc.framework.core.common.RpcEncoder;
import org.idea.irpc.framework.core.common.config.PropertiesBoostrap;
import org.idea.irpc.framework.core.common.config.ServerConfig;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.idea.irpc.framework.core.serialize.fastjson.FastJsonSerializerFactory;
import org.idea.irpc.framework.core.serialize.kryo.KryoSerializeFactory;
import org.idea.irpc.framework.impl.DataServerImpl;
import org.idea.irpc.framework.impl.HelloServiceImpl;

import static org.idea.irpc.framework.core.common.cache.CommonServerCache.*;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.*;

/**
 * RPC 框架的服务器端启动类。
 * <p>
 * 主要功能包括：
 * <ul>
 *     <li>初始化并启动 Netty 服务器</li>
 *     <li>配置网络参数和线程模型</li>
 *     <li>注册服务实现类</li>
 *     <li>处理客户端连接和请求</li>
 * </ul>
 *
 * @author cyang
 * @version 1.0
 * @since 2025-03
 */
@Setter
@Slf4j
public class Server {
    private ServerConfig serverConfig;

    public void exportService(Object serviceBean) {
        registerService(serviceBean);

        URL url = new URL();
        url.setServiceName(serviceBean.getClass().getInterfaces()[0].getName());
        url.setApplicationName(serverConfig.getApplicationName());
        // 这里要获取真实的ip, 但是本地电脑的ip只能在局域网中使用, 校园网甚至更少
        url.addParameter(HOST, CommonUtils.getIpAddress());
        url.addParameter(PORT, String.valueOf(serverConfig.getServerPort()));

        // todo: 配置应该移到配置文件
        if (REGISTRY_SERVICE == null) {
            REGISTRY_SERVICE = new ZookeeperRegister(serverConfig.getRegisterAddr());
        }

        // todo 可以去掉么?
        PROVIDER_URL_SET.add(url);
    }

    private void startApplication() throws InterruptedException {
        /*
          Netty 采用主从 Reactor 多线程模型
          bossGroup: 主 Reactor，负责处理连接请求
          - 通常只需要一个线程，因为连接建立是相对简单的操作
          - 负责监听和接受客户端的连接请求
          - 将接受的连接注册到 workerGroup 中
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        /*
         * workerGroup: 从 Reactor，负责处理 I/O 操作
         * - 线程数通常设置为 CPU 核心数的 2 倍
         * - 负责处理已建立连接的读写操作
         * - 处理实际的业务逻辑
         */
        // 默认线程数等于 CPU 核心数
        // 或者显式设置线程数
        // EventLoopGroup workerGroup = new NioEventLoopGroup(16);  // 固定线程数
        // EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);  // CPU核心数*2
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        // 设置主从线程组
        bootstrap.group(bossGroup, workerGroup);
        // 指定服务器使用的通道类型为 NIO 非阻塞模式
        // 使用 NioServerSocketChannel 可以支持高并发连接
        // 相比传统的阻塞式 IO，性能更好，资源利用率更高
        bootstrap.channel(NioServerSocketChannel.class);

        // ServerSocketChannel 配置
        // SO_BACKLOG: 服务器端连接队列大小，当服务器处理请求速度较慢时，可以适当调大
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);

        // SocketChannel 配置
        // SO_RCVBUF: 接收缓冲区大小，影响接收数据的性能
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 1024 * 16);
        // SO_SNDBUF: 发送缓冲区大小，影响发送数据的性能
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 1024 * 16);
        // SO_KEEPALIVE: 启用 TCP keepalive，用于检测连接是否存活
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        // TCP_NODELAY: 禁用 Nagle 算法，减少延迟
        // Nagel 算法会将多个小包合成打包再发送,但是我的rpc一般包不会太小,以后可以估算下, 毕竟一个字符就一个字节了
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                log.info("init channel");
                // 配置处理器链
                // 1. 编码器：将对象转换为字节
                // 2. 解码器：将字节转换为对象
                // 3. 业务处理器：处理具体的业务逻辑
                ch.pipeline().addLast(new RpcEncoder())
                        .addLast(new RpcDecoder())
                        .addLast(new ServerHandler());
            }
        });

        serverConfig = PropertiesBoostrap.loadServerConfig();

        batchExportUrl();

        ChannelFuture future = bootstrap.bind(serverConfig.getServerPort()).sync();
        future.addListener(f -> {
            if (f.isSuccess()) {
                log.info("Server bound to port {} successfully", serverConfig.getServerPort());
            } else {
                log.error("Failed to bind port {}", serverConfig.getServerPort(), f.cause());
            }
        });


        // 获取 workerGroup 的线程数
//        int threadCount = workerGroup.executorCount();
//        System.out.println("Worker group thread count: " + threadCount);
    }

    public void initCache() {
        String serverSerialize = serverConfig.getServerSerialize();
        switch (serverSerialize) {
            case FAST_JSON_SERIALIZE_STRATEGY -> SERVER_SERIALIZER = new FastJsonSerializerFactory();
            case KRYO_SERIALIZE_STRATEGY -> SERVER_SERIALIZER = new KryoSerializeFactory();
            default -> throw new RuntimeException("Invalid server serialize");
        }
    }

    /**
     * 注册服务实现类到缓存映射中。
     * <p>
     * 后续需要暴露服务到zookeeper等注册中心中.
     * </p>
     *
     * @param serviceBean 要注册的服务实现类实例
     * @throws RuntimeException 如果服务类没有实现接口或实现了多个接口
     */
    private void registerService(Object serviceBean) {
        // 检查服务类是否实现了接口
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("Must register service interface");
        }
        Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
        // 检查服务类是否只实现了一个接口
        if (interfaces.length > 1) {
            throw new RuntimeException("Must register only one interface");
        }
        Class<?> serviceClass = interfaces[0];
        log.info("register service: {}", serviceClass.getName());
        // 将服务接口的全限定名作为key，服务实现类实例作为value存入缓存
        PROVIDED_CLASSES_MAP.put(serviceClass.getName(), serviceBean);
    }

    private void batchExportUrl() {
        // todo
        // 更好的写法（推荐）：
        // 如果必须等数据，可以考虑用更优雅的方法，比如：
        // - 用 Future + 线程同步（比如 CompletableFuture）
        // - 加一个状态标志位，检测是否准备完成
        // - 使用 CountDownLatch 等并发工具来同步
        // gpt 说不用单独起一个线程, 可以优化register 如果注册很慢
//        new Thread(() -> {
//            for (URL url : PROVIDER_URL_SET) {
//                registryService.register(url);
//            }
//        }).start();

        for (URL url : PROVIDER_URL_SET) {
            // 会再次保存到本地缓存 PROVIDER_URL_SET
            REGISTRY_SERVICE.register(url);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 配置服务器地址和端口


        // 创建并配置服务器实例
        Server server = new Server();

        // 注册并暴露服务实现类
        server.exportService(new HelloServiceImpl());
        server.exportService(new DataServerImpl());

        // 启动服务器
        server.startApplication();

        // serializer
        // todo 确定顺序
        server.initCache();

//        log.info("Server started successfully on {}:{}", serverConfig.getHost(), serverConfig.getServerPort());
    }
}
