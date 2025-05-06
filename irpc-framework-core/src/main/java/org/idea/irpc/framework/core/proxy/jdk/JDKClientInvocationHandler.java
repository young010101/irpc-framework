package org.idea.irpc.framework.core.proxy.jdk;

import org.idea.irpc.framework.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

/**
 * JDK动态代理
 * <p>
 * 弄明白OBJECT何时被替换
 * </p>
 *
 * @author cyang
 */
public class JDKClientInvocationHandler implements InvocationHandler {
    /// 这个静态常量OBJECT只是在 `RESP_MAP` 占位符. 如果从服务器获得响应, 会被替换成RpcInvocation类型,
    private static final Object OBJECT = new Object();
    private final Class<?> clazz;

    public JDKClientInvocationHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object ignored, Method method, Object[] args) throws Throwable {
        // 使用不同的UUID对每次请求做区分
        RpcInvocation rpcInvocation =
                new RpcInvocation(clazz.getName(), method.getName(), args, UUID.randomUUID().toString());
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        // 将请求放进发送队列, 有异步线程拉去发送到服务器.
        // 详见 Client AsyncSendJob 方法
        SEND_QUEUE.add(rpcInvocation);

        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginTime < 3000) {
            // todo: 在哪里被替换的呢?
            Object obj = RESP_MAP.get(rpcInvocation.getUuid());
            if (obj instanceof RpcInvocation) {
                return ((RpcInvocation) obj).getResponse();
            }
        }

        throw new TimeoutException("Client wait server's response timeout!");
    }
}

