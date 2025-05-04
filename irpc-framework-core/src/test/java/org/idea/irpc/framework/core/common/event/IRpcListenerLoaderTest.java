package org.idea.irpc.framework.core.common.event;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.event.listener.ServiceUpdateListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class IRpcListenerLoaderTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void registerListener() {
    }

    @Test
    void init() throws NoSuchFieldException, IllegalAccessException {
        IRpcListenerLoader loader = new IRpcListenerLoader();
        loader.init();
        boolean hasListener = false;

        //获取listener list
        Class<IRpcListenerLoader>  clazz = IRpcListenerLoader.class;
        Field iRpcListenerListField = clazz.getDeclaredField("LISTENERS");
        iRpcListenerListField.setAccessible(true);
        List<?> listenerList = (List<?>) iRpcListenerListField.get(null);

        for (var listen : listenerList) {
            if (listen instanceof IRpcListener) {
                hasListener = true;
                break;
            }
        }
        assertTrue(hasListener);
        assertEquals(1, listenerList.size());
    }

    @Test
    void getInterfaceT() {
        ServiceUpdateListener listener = new ServiceUpdateListener();
        Class<?> eventClass = IRpcListenerLoader.getInterfaceT(listener);
        assertEquals(IRpcUpdateEvent.class, eventClass);
    }

    @Test
    void sendEvent() throws InterruptedException {
        new IRpcListenerLoader().init();
        IRpcUpdateEvent event = new IRpcUpdateEvent("test send event");
        IRpcListenerLoader.sendEvent(event);
        Thread.sleep(1000);
        log.info("check console manually");
    }
}