package org.idea.irpc.framework.core.filter;

import io.netty.channel.ChannelFuture;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.RpcInvocation;

import java.util.List;

/**
 * @author cyang
 */
public interface IClientFilter extends IFilter {
    /**
     *
     * @param src todo
     * @param invocation
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation invocation);
}
