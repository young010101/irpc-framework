package org.idea.irpc.framework.core.common.event;

/**
 * @author Cheng Yang
 * @author linhao
 * @since created in 10:33 下午 2021/12/18
 */
public class IRpcUpdateEvent implements IRpcEvent {

    private Object data;

    public IRpcUpdateEvent(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    /**
     * 链式返回
     * @param data 数据
     * @return this
     */
    @Override
    public IRpcEvent setData(Object data) {
        this.data = data;
        return this;
    }
}
