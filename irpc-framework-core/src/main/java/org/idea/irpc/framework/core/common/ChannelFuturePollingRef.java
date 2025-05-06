package org.idea.irpc.framework.core.common;

import io.netty.channel.ChannelFuture;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SERVICE_ROUTE_MAP;

/**
 * 获取这次要使用的远程服务连接
 * @author Cheng Yang
 */
public class ChannelFuturePollingRef {

    /**
     * 原来的设计是long类型的
     */
    private final AtomicInteger refCount = new AtomicInteger(0);

    public ChannelFutureWrapper getChannelFutureWrapper(String serviceName) {
        ChannelFutureWrapper[] channelFutureWrappers = SERVICE_ROUTE_MAP.get(serviceName);

        if (channelFutureWrappers == null) {
            throw new RuntimeException("service not found");
        }

        int i = refCount.getAndIncrement();
        return channelFutureWrappers[i % channelFutureWrappers.length];
    }

    // todo 源代码改成这个
    public ChannelFutureWrapper getChannelFutureWrapper(ChannelFutureWrapper[]  channelFutureWrappers) {
        if (channelFutureWrappers == null) {
            throw new RuntimeException("service not found");
        }

        int i = refCount.getAndIncrement();
        return channelFutureWrappers[i % channelFutureWrappers.length];
    }

    public ChannelFutureWrapper getChannelFutureWrapper(List<ChannelFutureWrapper> channelFutureWrappers) {
        if (channelFutureWrappers == null) {
            throw new RuntimeException("service not found");
        }

        int i = refCount.getAndIncrement();
        return channelFutureWrappers.get(i % channelFutureWrappers.size());
    }
}
