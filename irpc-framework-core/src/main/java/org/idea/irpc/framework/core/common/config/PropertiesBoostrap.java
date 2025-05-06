package org.idea.irpc.framework.core.common.config;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.*;

/**
 * @author cyang
 */
public class PropertiesBoostrap {
    // todo 从配置文件加载
    public static ClientConfig loadClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        // 1. proxy
        clientConfig.setServerIp("127.0.0.1");
        clientConfig.setServerPort(9999);
        clientConfig.setProxyType(JDK_PROXY);
        // 名字不重要,完全没有影响,作用应该是为了连接使用
        clientConfig.setApplicationName("cyan-client");

        // 2.
        clientConfig.setRegisterAddress("127.0.0.1:2181");

        // 3.
        clientConfig.setRouteStrategy(ROTATE_ROUTE_STRATEGY);

        // 4.
        clientConfig.setClientSerializer(FAST_JSON_SERIALIZE_STRATEGY);
        return clientConfig;
    }
    public static ServerConfig loadServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setServerPort(9999);
        serverConfig.setApplicationName("easy-rpc-server");
        serverConfig.setRegisterAddr("127.0.0.1:2181");
        serverConfig.setServerSerialize(FAST_JSON_SERIALIZE_STRATEGY);
        return serverConfig;
    }
}
