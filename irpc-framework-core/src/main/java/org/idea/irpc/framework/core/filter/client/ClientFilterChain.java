package org.idea.irpc.framework.core.filter.client;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.filter.IClientFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 过滤链实例需要在服务启动时有个统一存储的内存区域.
 * @author cyang
 */
public class ClientFilterChain {
    private static final List<IClientFilter> FILTERS = new ArrayList<>();

    public void addFilter(IClientFilter filter) {
        FILTERS.add(filter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation invocation) {
        for (IClientFilter filter : FILTERS) {
            filter.doFilter(src, invocation);
        }
    }
}
