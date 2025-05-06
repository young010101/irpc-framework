package org.idea.irpc.framework.core.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.RpcProtocol;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CLIENT_SERIALIZE_FACTORY;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;

/**
 * @author cyang
 */
@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 客户端与服务端通过 RpcProtocol 对象作为基本协议进行交互
            RpcProtocol protocol = (RpcProtocol) msg;
            byte[] content = protocol.getContent();
            // 反序列化响应
            RpcInvocation invocation = CLIENT_SERIALIZE_FACTORY.deserialize(content, RpcInvocation.class);
            // 通过发送的的uuid获取响应对象
            if (!RESP_MAP.containsKey(invocation.getUuid())) {
                throw new IllegalArgumentException("Server response is error");
            }
            // uuid在代理类中被放入
            RESP_MAP.put(invocation.getUuid(), invocation);
            log.info("client receive msg:{}", invocation);
        } finally {
            // cursor 提到可以使用 SimpleChannelInboundHandler, 避免手动释放
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 可选，通常可以省略
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            // 关闭异常连接，防止资源泄漏
            ctx.close();
        }
    }
}
