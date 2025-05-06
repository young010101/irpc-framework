package org.idea.irpc.framework.core.proxy;

import org.idea.irpc.framework.core.client.RpcReferenceWrapper;

/**
 * @author cyang
 */
public interface ProxyFactory {
    // todo: 为何加入final?

    /**
     *
     * @param clazz e.g. DataService.class
     * @return e.g. DataService
     * @param <T> e.g. DataService
     * @throws Exception todo
     */
    <T> T getProxy(final RpcReferenceWrapper<T> clazz) throws Exception;
}
