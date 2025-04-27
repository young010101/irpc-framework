package org.idea.irpc.framework.core.proxy;

public interface ProxyFactory {
    // todo: 为何加入final?
    <T> T getProxy(final Class<T> clazz) throws Exception;
}
