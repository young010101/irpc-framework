package org.idea.irpc.framework.core.filter.client;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.filter.IClientFilter;

import java.util.List;

/**
 * 基于分组的过滤器
 *
 * @author cyang
 */
public class GroupFilterImpl implements IClientFilter {
    private static final String GROUP_STRING = "group";

    /**
     * @param src        todo
     * @param invocation invocation
     */
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation invocation) {
        String group = String.valueOf(invocation.getAttachments().get(GROUP_STRING));
        src.removeIf(w -> !w.getGroup().equals(group));

        if (CommonUtils.isEmptyList(src)) {
            throw new RuntimeException("not provider match for group");
        }
    }
}
