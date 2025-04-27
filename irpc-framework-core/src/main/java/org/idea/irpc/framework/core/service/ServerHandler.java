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
        String json = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContentLength());
        log.info("Received request: {}", json);
        
        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
        log.info("Target service: {}", rpcInvocation.getTargetServiceName());
        
        Object targetService = CommonServerCache.PROVIDED_CLASSES_MAP.get(rpcInvocation.getTargetServiceName());
        log.info("Target service class: {}", targetService.getClass().getName());
        
        Class<?> clazz = targetService.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        Object result = null;
        
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
        
        log.info("Method execution result: {}", JSON.toJSONString(result));
        rpcInvocation.setResponse(result);
        RpcProtocol respRpcProtocol = new RpcProtocol(JSON.toJSONString(rpcInvocation).getBytes());
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
