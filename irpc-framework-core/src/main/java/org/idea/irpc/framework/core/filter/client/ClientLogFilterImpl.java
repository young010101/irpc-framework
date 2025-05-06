package org.idea.irpc.framework.core.filter.client;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.filter.IClientFilter;

import java.util.List;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.C_APP_NAME;

/**
 * 客户端调用日志记录
 * @author cyang
 */
@Slf4j
public class ClientLogFilterImpl implements IClientFilter {

    /**
     * @param src        todo
     * @param invocation
     */
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation invocation) {
        invocation.getAttachments().put(C_APP_NAME, CLIENT_CONFIG.getApplicationName());
        log.info("{} do invoke -------> {}",
                invocation.getAttachments().get(C_APP_NAME),
                invocation.getTargetServiceName());
    }
}
