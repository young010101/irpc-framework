package org.idea.irpc.framework.core.common.config.client;

import lombok.Data;

/**
 * @author cyang
 */
@Data
public class ClientConfig {
    /**
     * 我想要订阅的服务器的地址, 现阶段要从注册中心获得
     */
    private String serverAddress;
    private int serverPort;
    /**
     * zookeeper 等注册中心的地址
     */
    private String registerAddress;
    private String applicationName;
}
