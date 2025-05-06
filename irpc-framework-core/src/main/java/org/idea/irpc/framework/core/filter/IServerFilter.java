package org.idea.irpc.framework.core.filter;

import org.idea.irpc.framework.core.common.RpcInvocation;

/**
 * @author cyang
 */
public interface IServerFilter extends IFilter {
    void doFilter(RpcInvocation invocation);
}
