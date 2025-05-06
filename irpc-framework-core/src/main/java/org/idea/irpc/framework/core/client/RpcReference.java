package org.idea.irpc.framework.core.client;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.idea.irpc.framework.core.proxy.ProxyFactory;

/**
 * 主要用于 Client, 对proxyFactory的封装
 * @author cyang
 */
@Setter
@AllArgsConstructor
public class RpcReference {
    private ProxyFactory proxyFactory;

    /**
     * 根据接口类型返回代理对象
     * @param clazz 某种Server的getClass, 是接口, e.g. DataService.class
     * @return 返回代理对象, 可以像本地方法一般调用, e.g. DataService
     * @param <T> 一般是某种 Server 类, e.g. DataService
     * @throws Exception proxyFactory 抛出的异常, 目前还不知道是什么异常
     */
    public <T> T getProxy(RpcReferenceWrapper<T> clazz) throws Exception {
        return proxyFactory.getProxy(clazz);
    }
}
