package org.idea.irpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcDecoder;
import org.idea.irpc.framework.core.common.RpcEncoder;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.RpcProtocol;
import org.idea.irpc.framework.core.common.cache.CommonClientCache;
import org.idea.irpc.framework.core.common.config.client.ClientConfig;
import org.idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.irpc.framework.interfaces.DataService;
import org.idea.irpc.framework.interfaces.HelloService;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author cyang
 */
@Slf4j
@Data
public class Client {
    private ClientConfig clientConfig;

    public RpcReference startApplication() throws InterruptedException {

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

        ChannelFuture future = bootstrap.connect(clientConfig.getServerAddress(), clientConfig.getServerPort()).sync();
        log.info("client start success on {}:{}", clientConfig.getServerAddress(), clientConfig.getServerPort());

        this.startSendThread(future);

//        RpcInvocation invocation = new RpcInvocation();
//
//        Class<DataService> clazz = DataService.class;
//        Method method;
//        try {
//            method = clazz.getMethod("hello", String.class);
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
//        log.info(clazz.getSimpleName());
//        invocation.setTargetServiceName(clazz.getName());
//        invocation.setTargetMethod(method.getName());
//        invocation.setArgs(new String[]{"cyan"});
//        invocation.setUuid(UUID.randomUUID().toString());
//        CommonClientCache.RESP_MAP.put(invocation.getUuid(), new Object());
//
//        String json = JSON.toJSONString(invocation);
//
//        RpcProtocol protocol = new RpcProtocol(json.getBytes());
//        future.channel().writeAndFlush(protocol);
////        while (sync.isSuccess()) {
////            sync.channel().writeAndFlush(Unpooled.copiedBuffer("hello", CharsetUtil.UTF_8));
////            try {
////                Thread.sleep(1000);
////            } catch (InterruptedException e) {
////                throw new RuntimeException(e);
////            }
////        }
        return new RpcReference(new JDKProxyFactory());
    }

    /**
     * 异步发送远程调用
     *
     * @author Cheng Yang
     */
    @Setter
    @AllArgsConstructor
    private static class AsyncSentJob implements Runnable {
        private ChannelFuture future;

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Object data = CommonClientCache.SEND_QUEUE.take();
                    String json = JSON.toJSONString(data);
                    RpcProtocol protocol = new RpcProtocol(json.getBytes());
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
    private void startSendThread(ChannelFuture future) {
        new Thread(new AsyncSentJob(future)).start();
    }

    public static void main(String[] args) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setServerAddress("127.0.0.1");
        clientConfig.setServerPort(9999);

        Client client = new Client();
        client.setClientConfig(clientConfig);

        RpcReference rpcReference = client.startApplication();

        HelloService helloService = rpcReference.getProxy(HelloService.class);
        for (int i = 0; i < 10; i++) {
            log.info("helloService: {}", helloService.sayHello("irpc"));
        }
    }
}
