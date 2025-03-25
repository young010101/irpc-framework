package org.idea.irpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.RpcProtocol;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.REST_MAP;

/**
 * @author cyang
 */
@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcProtocol protocol = (RpcProtocol) msg;
        String json = new String(protocol.getContent(), 0, protocol.getContentLength());
        RpcInvocation o = JSON.parseObject(json, RpcInvocation.class);
        REST_MAP.put(o.getUuid(), o);
        log.info("client receive msg:{}", msg);
    }
}
