package org.idea.irpc.framework.core.proxy;

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
    <T> T getProxy(final Class<T> clazz) throws Exception;
}
