package org.idea.irpc.framework.core.common.config;

import lombok.Data;

/**
 * @author cyang
 */
@Data
public class ClientConfig {
    // proxy
    /**
     * 订阅的服务器的地址, 现阶段要从注册中心获得
     */
    private String serverIp;
    private int serverPort;
    private String applicationName;

    private String proxyType;

    // register
    /**
     * zookeeper 等注册中心的地址
     */
    private String registerAddress;

    // route
    private String routeStrategy;

    // serializer
    /**
     * e.g.
     * <li>fastjson</li>
     * <li>kryo</li>
     */
    private String clientSerializer;
}
