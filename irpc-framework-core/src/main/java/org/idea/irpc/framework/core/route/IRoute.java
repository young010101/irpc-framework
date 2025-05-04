package org.idea.irpc.framework.core.route;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.registy.URL;


/**
 * @author cyang
 */
public interface IRoute {
    /**
     * 刷新路由数组
     * todo: 如何处理服务上下线?
     * todo: 为何是Selector?
     */
    void refreshRouteArr(Selector selector);

    /**
     * 获取一个服务
     */
    ChannelFutureWrapper select(Selector selector);

    /**
     * 更新权重
     * @param url 在注册层定义的. 为何上面用Selector这里用url?
     */
    void update(URL url);
}
