package org.idea.irpc.framework.core.service;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.RpcProtocol;
import org.idea.irpc.framework.core.common.cache.CommonServerCache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author cyang
 */
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InvocationTargetException, IllegalAccessException {
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        String json = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContentLength());
        log.info(json);
        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
        log.info(rpcInvocation.getClassName());
        Object o = CommonServerCache.PROVIDED_SERVICE.get(rpcInvocation.getClassName());
        log.info(o.getClass().getName());
        Class<?> clazz = o.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        Object result = null;
        for (Method method : methods) {
            if (method.getName().equals(rpcInvocation.getMethod())) {
                if (method.getReturnType().equals(void.class)) {
                    log.info(method.getName());
                    method.invoke(o, rpcInvocation.getParams());
                } else {
                    result = method.invoke(o,rpcInvocation.getParams());
                }
                break;
            }
        }
        log.info(JSON.toJSONString(result));
        rpcInvocation.setResponse(result);
        RpcProtocol out = new RpcProtocol(JSON.toJSONString(rpcInvocation).getBytes());
        ctx.writeAndFlush(out);
    }
}
