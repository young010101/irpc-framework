package org.idea.irpc.framework.core.common.event;

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
public class IRpcListenerLoader {

    /**
     * 如果 LISTENERS不为空, 线程池
     */
    private static final List<IRpcListener> LISTENERS = new ArrayList<>();

    private static final ExecutorService EVENT_THREAD_POOL = Executors.newFixedThreadPool(2);

    public static void registerListener(IRpcListener iRpcListener) {
        LISTENERS.add(iRpcListener);
    }

    public void init() {
        registerListener(new ServiceUpdateListener());
    }

    /**
     * 获取接口上的泛型T
     *
     * @param o     接口
     */
    public static Class<?> getInterfaceT(Object o) {
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    /**
     * 使用线程池异步执行 Listener的callBack
     * @param iRpcEvent
     */
    public static void sendEvent(IRpcEvent iRpcEvent) {
        // sendEvent前应该先 init loader, 注册一个 Listener
        if(CommonUtils.isEmptyList(LISTENERS)){
            // 或许可以用更加好的异常
            throw new RuntimeException("init error");
        }
        for (IRpcListener<?> iRpcListener : LISTENERS) {
            Class<?> type = getInterfaceT(iRpcListener);
            assert type != null;
            if(type.equals(iRpcEvent.getClass())){
                EVENT_THREAD_POOL.execute(() -> {
                    try {
                        iRpcListener.callback(iRpcEvent.getData());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }
        }
    }

}
