package org.idea.irpc.framework.core.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cyang
 */
@Slf4j
public class Server {
    public static void main(String[] args) throws InterruptedException {
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
                ch.pipeline().addLast(new StringEncoder())
                        .addLast(new StringDecoder())
                        .addLast(new ServerHandler());
            }
        });
        bootstrap.bind(9999).sync();
        log.info("server start success");
    }
}
