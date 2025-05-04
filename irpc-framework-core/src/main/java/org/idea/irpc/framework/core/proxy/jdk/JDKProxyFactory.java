package org.idea.irpc.framework.core.proxy.jdk;

import org.idea.irpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * @author cyang
 */
public class JDKProxyFactory implements ProxyFactory {
    /**
     * 基于JDK 动态代理机制生成目标接口的代理对象.
     *
     * @param clazz 接口类 e.g. DataService
     * @return      实现该接口的代理对象 e.g. DataService
     * @param <T>   接口类型 interface type, e.g. DataService
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProxy(Class<T> clazz) {
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("JDK dynamic proxy only supports interfaces: " + clazz.getName());
        }

        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new JDKClientInvocationHandler(clazz)
        );

    }
}
