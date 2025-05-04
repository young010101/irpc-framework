package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.common.ChannelFuturePollingRef;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.proxy.jdk.JDKClientInvocationHandler;
import org.idea.irpc.framework.core.registy.URL;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cyang
 */
public class CommonClientCache {


    // ==========proxy, lesson2===========


    /**
     * 代理层
     * <li>key: uuid</li>
     * <li>value: service的return</li>
     * <p>
     * 在 class ClientHandler 的 {@code channelRead} 里, 会替换成真实的响应.
     * 代理对象的invoke方法中有个3000ms的循环不断检查 RESP_MAP的更新,
     * 如果获得 {@code RpcInvocation}, return 里面的 response
     * </p>
     */
    public static final Map<String, Object> RESP_MAP = new ConcurrentHashMap<>();

    /**
     * 发送队列, 代理层用到
     *
     * @see JDKClientInvocationHandler
     */
    public static final BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(100);


    // ===========register, lesson3===========


    /**
     * 已建立的连接的缓存, 如果服务变更(common 里的 update event), 会更新缓存, 所以用到的地方有event callback里
     *
     * <p>
     * 每次远程调用都是从这里选取服务提供者
     * </p>
     */
    public static final Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();

    /**
     * 在路由层中, 将 String改为了URL
     */
    public static final List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();


    // ==========route, lesson4===========


    /**
     * 随机请求的Map, 具体看...
     * 路由层用到
     */
    public static final Map<String, ChannelFutureWrapper[]> SERVICE_ROUTE_MAP = new ConcurrentHashMap<>();

    /**
     * 每次服务拿到的具体的一个ip:port
     *
     * @apiNote 路由层用到
     */
    public static final ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();

    /**
     * 一个冗余的加工好的?
     * todo: 这是干啥的? 和{@code CONNECT_MAP}什么关系?
     */
    public static final Set<String> SERVER_ADDRESS = new HashSet<>();
}
