package org.idea.irpc.framework.core.registy.zookeeper;

import org.idea.irpc.framework.core.registy.RegistryService;
import org.idea.irpc.framework.core.registy.URL;

import java.util.List;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static org.idea.irpc.framework.core.common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * lesson3 第三个类
 * <p>接下来在注册服务接口的下边，我们可以开始进行具体的注册层设计。
 * </p>
 * <p>
 * 首先我是定义了一个叫做AbstractRegister的抽象类，这个抽象类主要的作用是对一些注册数据做统一的处理，假设日后需要考虑支持多种类型的注册中心，例如redis 、 etcd之类的话，所有基础的记录操作都可以统一放在抽象类里实现。
 * </p>
 * <p>
 * 同时为了考虑到，后续留给子类可以做更多的拓展行为，我在抽象层也封装了一些扩展函数，诸如doAfterSubscribe之类的。
 * </p>
 *
 * @author Cheng Yang
 * @author linhao
 * @since created in 3:57 下午 2021/12/11
 */
public abstract class AbstractRegister implements RegistryService {


    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    /**
     * 在路由层的实现中改为放入url, 原本只用放入类的权限定名
     * @param url 主要用到类的权限定名
     */
    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url);
    }

    /**
     * 留给子类扩展
     *
     * @param url
     */
    public abstract void doAfterSubscribe(URL url);

    /**
     * 留给子类扩展
     *
     * @param url
     */
    public abstract void doBeforeSubscribe(URL url);

    /**
     * 留给子类扩展
     *
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderAddresses(String serviceName);


    @Override
    public void doUnSubscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }
}
