package org.idea.irpc.framework.core.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cyang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerConfig {
//    private String host;

    /**
     * 本地服务的端口号
     */
    private int serverPort;

    /**
     * 不重要
     */
    private String applicationName;

    /**
     * 注册中心的地址
     */
    private String registerAddr;

    private String serverSerialize;
}
