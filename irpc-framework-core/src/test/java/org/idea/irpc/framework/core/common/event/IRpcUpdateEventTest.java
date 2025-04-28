package org.idea.irpc.framework.core.common.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class IRpcUpdateEventTest {

    @Test
    void getData() {
        Object data = "data";
        IRpcUpdateEvent event = new IRpcUpdateEvent(data);
        assertEquals(data, event.getData());
    }

    @Test
    void setData() {
        Object newData = "newData";
        IRpcUpdateEvent event = new IRpcUpdateEvent("initialData");
        event.setData(newData);
        assertEquals(newData, event.getData());
    }

    @Test
    void setDataReturnSelf() {
        IRpcUpdateEvent event = new IRpcUpdateEvent(null);
        IRpcEvent ret = event.setData("some data");
        assertSame(event, ret);
    }
}