package org.idea.irpc.framework.core.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.RpcProtocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.idea.irpc.framework.core.common.cache.CommonServerCache.PROVIDED_CLASSES_MAP;
import static org.idea.irpc.framework.core.common.cache.CommonServerCache.SERVER_SERIALIZER;

/**
 * RPC服务端处理器
 * <p>
 * 负责处理客户端请求并返回响应
 * </p>
 * @author cyang
 */
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {
    
    /**
     * 处理客户端请求
     * <ul>
     * <li>1. 解析RPC协议</li>
     * <li>2. 通过反射调用目标方法</li>
     * <li>3. 返回执行结果</li>
     * </ul>
     *
     * @param ctx ChannelHandlerContext
     * @param msg 接收到的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InvocationTargetException, IllegalAccessException {
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        byte[] content = rpcProtocol.getContent();
        if (content == null || content.length == 0) {
            log.error("server receive empty content");
            return;
        } else if (content.length != ((RpcProtocol) msg).getContentLength()) {
            log.error("server receive content length error");
            return;
        }
        // deserialize, 获得接口名字
        RpcInvocation rpcInvocation = SERVER_SERIALIZER.deserialize(content, RpcInvocation.class);
        log.info("Target service interface: {}", rpcInvocation.getTargetServiceName());

        // 获得接口对应的bean
        Object targetService = PROVIDED_CLASSES_MAP.get(rpcInvocation.getTargetServiceName());
        log.info("Target service class impl: {}", targetService.getClass().getName());
        
        Class<?> clazz = targetService.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        Object result = null;

        // 匹配方法. 只匹配一个
        for (Method method : methods) {
            if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                if (method.getReturnType().equals(Void.TYPE)) {
                    log.info("Executing void method: {}", method.getName());
                    method.invoke(targetService, rpcInvocation.getArgs());
                } else {
                    result = method.invoke(targetService, rpcInvocation.getArgs());
                }
                break;
            }
        }
        
        log.info("Method execution result: {}", result);
        rpcInvocation.setResponse(result);
        RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZER.serialize(rpcInvocation));
        ctx.writeAndFlush(respRpcProtocol);
    }

    /**
     * 处理异常情况
     * 当发生异常时关闭连接
     *
     * @param ctx ChannelHandlerContext
     * @param cause 异常信息
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Server handler caught exception", cause);
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }
}
