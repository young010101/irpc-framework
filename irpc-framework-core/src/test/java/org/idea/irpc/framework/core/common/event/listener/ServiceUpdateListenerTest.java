package org.idea.irpc.framework.core.common.event.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import org.idea.irpc.framework.core.client.ConnectionHandler;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.event.data.URLChangeWrapper;
import org.idea.irpc.framework.interfaces.HelloService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

class ServiceUpdateListenerTest {

    public static ServiceUpdateListener updateListener = new ServiceUpdateListener();

    @BeforeEach
    void setUp() {
        // 初始化, 清空状态
        CONNECT_MAP.clear();

        URLChangeWrapper url = new URLChangeWrapper();
        url.setServiceName(HelloService.class.getName());

        // 一系列的 ChannelFutureWrapper, 在本地缓存 CONNECT_MAP 里
        // 到时候 listener 会便利, host和port,
        ChannelFutureWrapper wrapper1 = new ChannelFutureWrapper("127.0.0.1", 8080);
        ChannelFutureWrapper wrapper2 = new ChannelFutureWrapper("127.0.0.1", 8081);

        CONNECT_MAP.put(url.getServiceName(), new ArrayList<>(Arrays.asList(wrapper1, wrapper2)));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void callBack() {
        URLChangeWrapper url = new URLChangeWrapper();
        url.setServiceName(HelloService.class.getName());
        url.setProviderUrl(List.of("127.0.0.1:8080", "127.0.0.1:8082"));


        try (MockedStatic<ConnectionHandler> mocked = Mockito.mockStatic(ConnectionHandler.class)) {
            ChannelFuture fakeChanelFuture = new EmbeddedChannel().newSucceededFuture();
            mocked.when(() -> ConnectionHandler.createChannelFuture(anyString(), anyInt())).thenReturn(fakeChanelFuture);
            updateListener.callBack(url);
        }

        List<ChannelFutureWrapper> wrappers = CONNECT_MAP.get(url.getServiceName());
        Assertions.assertEquals(2, wrappers.size());

        Set<String> set = new HashSet<>();
        for (ChannelFutureWrapper wrapper : wrappers) {
            set.add(wrapper.getHost() + ":" + wrapper.getPort());
        }
        Assertions.assertTrue(set.contains("127.0.0.1:8080"));
        Assertions.assertFalse(set.contains("127.0.0.1:8081"));
        Assertions.assertTrue(set.contains("127.0.0.1:8082"));
    }
}