package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.proxy.jdk.JDKClientInvocationHandler;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cyang
 */
public class CommonClientCache {
    public static final List<String> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();
    public static final Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();
    public static final Set<String> SERVER_ADDRESS = new HashSet<>();
    /**
     * todo: 响应, 有了从服务器的响应会怎么做呢?
     */
    public static final Map<String, Object> RESP_MAP = new ConcurrentHashMap<>();
    /**
     * 发送队列
     * @see JDKClientInvocationHandler
     */
    public static final BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(100);
}
