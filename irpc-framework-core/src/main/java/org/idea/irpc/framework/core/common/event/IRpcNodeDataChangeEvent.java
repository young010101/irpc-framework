package org.idea.irpc.framework.core.common.event;

/**
 * @author cyang
 */
public class IRpcNodeDataChangeEvent implements IRpcEvent {
    private Object data;

    public IRpcNodeDataChangeEvent(Object data) {
        this.data = data;
    }

    /**
     * @return 
     */
    @Override
    public Object getData() {
        return data;
    }

    /**
     * @param data 
     * @return this
     */
    @Override
    public IRpcEvent setData(Object data) {
        this.data = data;
        return this;
    }
}
