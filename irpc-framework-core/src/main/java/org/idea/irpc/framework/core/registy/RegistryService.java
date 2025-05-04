package org.idea.irpc.framework.core.registy;

/**
 * 提供4个接口.
 * lesson 3 注册层设计 提到的一个类
 *
 * @author Cheng Yang
 * @author Lin Hao
 * @since created in 11:26 上午 2021/12/11
 */
public interface RegistryService {


    /**
     * 注册url
     * <p>
     * 将irpc服务写入注册中心节点
     * 当出现网络抖动的时候需要进行适当的重试做法
     * 注册服务url的时候需要写入持久化文件中
     * </p>
     *
     * @param url
     */
    void register(URL url);

    /**
     * 服务下线
     * <p>
     * 持久化节点是无法进行服务下线操作的
     * 下线的服务必须保证url是完整匹配的
     * 移除持久化文件中的一些内容信息
     *
     * @param url
     */
    void unRegister(URL url);

    /**
     * 消费方订阅服务
     * <p>
     * 订阅某个服务，通常是客户端在启动阶段需要调用的接口。客户端在启动过程中需要调用该函数，从注册中心中提取现有的服务提供者地址，从而实现服务订阅功能。
     * </p>
     *
     * @param url
     */
    void subscribe(URL url);


    /**
     * 执行取消订阅内部的逻辑
     *
     * @param url
     */
    void doUnSubscribe(URL url);
}

