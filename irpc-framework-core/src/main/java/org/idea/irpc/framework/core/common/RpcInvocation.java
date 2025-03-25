package org.idea.irpc.framework.core.common;

import lombok.Data;

/**
 * @author cyang
 */
@Data
public class RpcInvocation {
    String method;
    String className;
    Object[] params;
    String uuid;
    Object response;
}
