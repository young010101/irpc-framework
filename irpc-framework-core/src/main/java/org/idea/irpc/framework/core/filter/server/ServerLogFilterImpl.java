package org.idea.irpc.framework.core.filter.server;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.filter.IServerFilter;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.C_APP_NAME;

@Slf4j
public class ServerLogFilterImpl implements IServerFilter {
    /**
     * @param invocation
     */
    @Override
    public void doFilter(RpcInvocation invocation) {
        if (invocation.getAttachments() == null) {
            log.error("set invocation attachments first!");
            return;
        }
        log.info("{} do invoke ---> {}#{}",
                invocation.getAttachments().get(C_APP_NAME),
                invocation.getTargetServiceName(),
                invocation.getTargetMethod()
        );
    }
}
