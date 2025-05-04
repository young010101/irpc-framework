package org.idea.irpc.framework.core.route;

import lombok.Data;

/**
 * @author Cheng Yang
 */
@Data
public class Selector {
    /**
     * 服务名称, 类的权限定名, 如 {@code com.idea.cyan.service.DataService}
     */
    private String providerServiceName;
}
