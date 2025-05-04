package org.idea.irpc.framework.core.registy;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.registy.zookeeper.ProviderNodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.HOST;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.PORT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class URLTest {
    URL url = new URL();
    @BeforeEach
    void setUp() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(HOST, "127.0.0.1");
        parameters.put(PORT, "8888");
        parameters.put("key3", "value3");

        url.setApplicationName("irpc-app");
        url.setServiceName("irpc-service");
        url.setParameters(parameters);

    }

    @Test
    void buildProviderUrlStr() {
        String providerNodeStr = URL.buildProviderUrlStr(url);
        String[] split = providerNodeStr.split(";");
        assertEquals(5, split.length);
        assertEquals("irpc-app", split[0]);
        assertEquals("irpc-service", split[1]);
        assertEquals("127.0.0.1:8888", split[2]);

        // 检验时间
        assertDoesNotThrow(() -> Long.parseLong(split[3]));
        assertEquals("null", split[4]);
    }

    @Test
    void buildConsumerUrlStr() {
        String consumerNodeStr = URL.buildConsumerUrlStr(url);
        String[] split = consumerNodeStr.split(";");
        assertEquals(4, split.length);
        assertEquals("irpc-app", split[0]);
        assertEquals("irpc-service", split[1]);
        assertEquals("127.0.0.1", split[2]);
        assertDoesNotThrow(() -> Long.parseLong(split[3]));
    }

    @Test
    void buildUrlFromUrlStr() {
        String providerNodeStr = URL.buildProviderUrlStr(url);
        String replace = providerNodeStr.replace(";", "/");
        log.debug(replace);
        ProviderNodeInfo providerNodeInfo = URL.buildUrlFromUrlStr(replace);
        log.debug(providerNodeInfo.toString());
        assertEquals("127.0.0.1:8888", providerNodeInfo.getAddress());
    }
}