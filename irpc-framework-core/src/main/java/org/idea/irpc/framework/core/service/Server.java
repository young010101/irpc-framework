package org.idea.irpc.framework.core.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcEncoder;
import org.idea.irpc.framework.core.common.RpcDecoder;
import org.idea.irpc.framework.core.common.cache.CommonServerCache;
import org.idea.irpc.framework.impl.DataServerImpl;

/**
 * @author cyang
 */
@Setter
@Slf4j
public class Server {
    private ServerConfig serverConfig;

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setHost("127.0.0.1");
        serverConfig.setPort(9999);
        server.setServerConfig(serverConfig);
        server.registerService(new DataServerImpl());
        server.startApplication();

        log.info("server start success");
    }

    private void startApplication() throws InterruptedException {
        // deal with accept
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // deal with read & write
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();

        // set master thread group and slave
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        // queue length
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 1024 * 16);
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 1024 * 16);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                log.info("init channel");
                ch.pipeline().addLast(new RpcEncoder())
                        .addLast(new RpcDecoder())
                        .addLast(new ServerHandler());
            }
        });

        bootstrap.bind(serverConfig.getPort()).sync();
    }

    private void registerService(Object serviceBean) {
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("Must register service interface");
        }
        Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
        if (interfaces.length > 1) {
            throw new RuntimeException("Must register only one interface");
        }
        Class<?> serviceClass = interfaces[0];
        log.info("register service:{}", serviceClass.getName());
        CommonServerCache.PROVIDED_SERVICE.put(serviceClass.getName(), serviceBean);
    }
}
