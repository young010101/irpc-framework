package org.idea.irpc.framework.core.filter.client;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.filter.IClientFilter;

import java.util.List;

/**
 * @author cyang
 */
public class DirectInvokeFilterImpl implements IClientFilter {

    private static final String URL_STRING = "url";

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation invocation) {
        String url = (String) invocation.getAttachments().get(URL_STRING);
        if (CommonUtils.isEmpty(url)) {
            return;
        }
        src.removeIf(i -> (i.getHost() + i.getPort()).equals(url));
        if (CommonUtils.isEmptyList(src)) {
            throw new RuntimeException("not matched provider url for " + url);
        }
    }
}
