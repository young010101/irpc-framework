package org.idea.irpc.framework.core.common.event;

/**
 * @Author linhao
 * @Date created in 11:49 下午 2021/12/13
 */
public interface IRpcListener<T> {

    void callBack(Object t);

}
