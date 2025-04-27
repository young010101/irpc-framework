package org.idea.irpc.framework.core.proxy.jdk;

import org.idea.irpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author cyang
 */
public class JDKProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<T> clazz) throws Exception {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new JDKClientInvocationHandler(clazz)
        );

    }
}
