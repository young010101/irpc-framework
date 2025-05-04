package org.idea.irpc.framework.core.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.io.Serial;

/**
 * RPC 调用信息封装类
 * <p>
 * 用于封装 RPC 调用的所有相关信息，包括目标服务、方法、参数等
 * </p>
 * 
 * @author cyang
 * @since 2025-04
 */
@Data
@NoArgsConstructor
public class RpcInvocation implements Serializable {
    @Serial
    private static final long serialVersionUID = 2025042701L;

    /**
     * 请求的目标服务名称，例如：com.idea.user.UserService
     */
    private String targetServiceName;

    /**
     * 请求的目标方法名，例如：findUser
     */
    private String targetMethod;

    /**
     * 请求参数信息
     */
    private Object[] args;

    /**
     * 请求唯一标识
     */
    private String uuid;

    /**
     * 接口响应数据
     * <p>
     * 注意：
     * - 异步调用时可能为空
     * - void 方法返回时为空
     * </p>
     */
    private Object response;

    public RpcInvocation(String targetServiceName, String targetMethod, Object[] args, String uuid) {
        this.targetServiceName = targetServiceName;
        this.targetMethod = targetMethod;
        this.args = args;
        this.uuid = uuid;
    }
}
