package org.idea.irpc.framework.core.common.event;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.event.listener.ServiceUpdateListener;
import org.idea.irpc.framework.core.common.utils.CommonUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author linhao
 * @Date created in 11:50 下午 2021/12/13
 */
@Slf4j
public class IRpcListenerLoader {

    /**
     * 如果 LISTENERS不为空, 线程池
     */
    private static final List<IRpcListener<?>> LISTENERS = new ArrayList<>();

    private static final ExecutorService EVENT_THREAD_POOL = Executors.newFixedThreadPool(2);

    public static void registerListener(IRpcListener<?> iRpcListener) {
        LISTENERS.add(iRpcListener);
    }

    public void init() {
        registerListener(new ServiceUpdateListener());
    }

    /**
     * 获取接口上的泛型T
     *
     * @param listener 接口
     */
    public static Class<?> getInterfaceT(Object listener) {
        Type[] types = listener.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    /**
     * sendEvent前应该先 init loader, 注册各种 Listener
     * 使用线程池异步执行 Listener的callBack
     *
     * @param iRpcEvent e.g. 服务的上线下线, 权重改变
     */
    public static void sendEvent(IRpcEvent iRpcEvent) {
        if (CommonUtils.isEmptyList(LISTENERS)) {
            // 异常 or log?
            log.error("listeners is empty, should execute listenerLoader.init() to add listener first!");
            return;
        }

        // 匹配 listener 和对应的 event, 使用先把listener add到全局list中
        for (IRpcListener<?> iRpcListener : LISTENERS) {
            Class<?> type = getInterfaceT(iRpcListener);
            assert type != null;
            if (type.equals(iRpcEvent.getClass())) {
                EVENT_THREAD_POOL.execute(() -> {
                    try {
                        iRpcListener.callback(iRpcEvent.getData());
                    } catch (Exception e) {
                        log.error("callback failed", e);
                    }
                });
            }
        }
    }

}
