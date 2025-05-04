package org.idea.irpc.framework.core.common.event;

///
/// @param <T> T不能省略, 用于在loader的sendEvent中区分不同事件对应的listener, 通过listener的接口泛型. 有无更加标准的做法?
/// <p>
///
/// 1. 方案一 显式注册监听器与类型之间的关系
///
/// ```java
/// Map<Class<T>, List<IRpcListener>> listenerMap = new ConcurrentHashMap<>();
/// ```
///
/// 2. 方案二
/// ```java
/// default Class<T> getType() {
///     return ...
/// }
/// ```
///
/// @author Cheng Yang
/// @author linhao
/// @apiNote gpt说泛型运行时擦除
/// @since created in 11:49 下午 2021/12/13
public interface IRpcListener<T> {

    /**
     * 更新本地的各种缓存
     * <li>Service Update: 更新本地服务对应的</li>
     *
     * @param t 从 event中获取的data
     */
    void callback(Object t);

}
