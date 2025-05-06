package org.idea.irpc.framework.core.filter.server;

import io.netty.channel.ChannelHandlerContext;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.filter.IServerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cyang
 */
public class ServerFilterChain {
    private static final List<IServerFilter> FILTERS = new ArrayList<>();

    public void addFilter(IServerFilter filter) {
        FILTERS.add(filter);
    }

    public void doFilter(RpcInvocation invocation) {
        for (IServerFilter filter : FILTERS) {
            filter.doFilter(invocation);
        }
    }
}
