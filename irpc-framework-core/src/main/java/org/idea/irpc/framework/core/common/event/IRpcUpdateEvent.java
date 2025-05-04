package org.idea.irpc.framework.core.common.event;

/**
 * 服务更新事件, 即服务地址从zookeeper的上线下线
 * @author Cheng Yang
 * @author linhao
 * @since created in 10:33 下午 2021/12/18
 * @version 1.0-SNAPSHOT
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
