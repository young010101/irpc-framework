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
import org.idea.irpc.framework.core.common.config.client.ClientConfig;
import org.idea.irpc.framework.interfaces.DataService;

import java.lang.reflect.Method;

/**
 * @author cyang
 */
@Slf4j
@Data
public class Client {
    private ClientConfig clientConfig;

    public void startApplication() throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

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

        ChannelFuture sync = bootstrap.connect(clientConfig.getServerAddress(), clientConfig.getServerPort()).sync();

        RpcInvocation invocation = new RpcInvocation();

        Class<DataService> clazz = DataService.class;
        Method method;
        try {
            method = clazz.getMethod("hello", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        log.info(clazz.getSimpleName());
        invocation.setTargetServiceName(clazz.getName());
        invocation.setTargetMethod(method.getName());
        invocation.setArgs(new String[]{"cyan"});

        String json = JSON.toJSONString(invocation);

        RpcProtocol protocol = new RpcProtocol(json.getBytes());
        sync.channel().writeAndFlush(protocol);
//        while (sync.isSuccess()) {
//            sync.channel().writeAndFlush(Unpooled.copiedBuffer("hello", CharsetUtil.UTF_8));
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    public static void main(String[] args) throws InterruptedException {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setServerAddress("127.0.0.1");
        clientConfig.setServerPort(9999);
        Client client = new Client();
        client.setClientConfig(clientConfig);
        client.startApplication();
    }
}
