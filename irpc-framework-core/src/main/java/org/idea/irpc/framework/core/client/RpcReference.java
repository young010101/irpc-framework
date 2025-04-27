package org.idea.irpc.framework.core.client;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.idea.irpc.framework.core.proxy.ProxyFactory;

/**
 * 主要用于 Client
 * @author cyang
 */
@Setter
@AllArgsConstructor
public class RpcReference {
    private ProxyFactory proxyFactory;

    /**
     * 返回代理对象
     * @param clazz 某种Server的getClass
     * @return 返回代理对象, 可以像本地方法一般调用
     * @param <T> 一般是某种 Server 类
     * @throws Exception proxyFactory 抛出的异常, 目前还不知道是什么异常
     */
    public <T> T getProxy(Class<T> clazz) throws Exception {
        return proxyFactory.getProxy(clazz);
    }
}
