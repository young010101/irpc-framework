package org.idea.irpc.framework.core.filter.server;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.filter.IServerFilter;
import org.idea.irpc.framework.core.service.ServiceBeanWrapper;

import static org.idea.irpc.framework.core.common.cache.CommonServerCache.SERVICE_BEAN_WRAPPER_MAP;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.SERVICE_TOKEN;

/**
 * @author cyang
 */
@Slf4j
public class ServiceTokenFilterImpl implements IServerFilter {

    @Override
    public void doFilter(RpcInvocation invocation) {
        ServiceBeanWrapper serviceBeanWrapper = SERVICE_BEAN_WRAPPER_MAP.get(invocation.getTargetServiceName());
        if (serviceBeanWrapper == null) {
            log.error("can not find service bean for {}", invocation.getTargetServiceName());
            return;
        }
        String matchedToken = serviceBeanWrapper.getToken();
        if (CommonUtils.isEmpty(matchedToken)) {
            return;
        }
        // valueOf 和 toString 的区别
        String token = invocation.getAttachments().get(SERVICE_TOKEN).toString();
        if (!CommonUtils.isEmpty(token) && matchedToken.equals(token)) {
            return;
        }
        throw new RuntimeException("token is " + token + ", verify result is false!");
    }
}
