package org.idea.irpc.framework.core.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.GROUP_STRING;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.SERVICE_TOKEN;

/**
 * @author cyang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcReferenceWrapper<T> {
    private Class<T> aimClass;
    private Map<String, Object> attachments = new ConcurrentHashMap<>();

    public String getServiceToken() {
        return attachments.get(SERVICE_TOKEN).toString();
    }

    public void setServiceToken(String serviceToken) {
        attachments.put(SERVICE_TOKEN, serviceToken);
    }

    public String getGroup() {
        return attachments.get(GROUP_STRING).toString();
    }

    public void setGroup(String group) {
        attachments.put(GROUP_STRING, group);
    }
}
