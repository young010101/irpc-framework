package org.idea.irpc.framework.core.registy.zookeeper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.event.IRpcEvent;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.idea.irpc.framework.core.common.event.IRpcNodeDataChangeEvent;
import org.idea.irpc.framework.core.common.event.IRpcUpdateEvent;
import org.idea.irpc.framework.core.common.event.data.URLChangeWrapper;
import org.idea.irpc.framework.core.registy.RegistryService;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.interfaces.DataService;

import java.util.List;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.*;

/// Zookeeper 注册结构 (参考dubbo):
/// ```text
/// |--irpc                                     // 根节点
///    |--com.idea.cyan.service.DataService     // 服务名称, 即 serviceName
///    |--consumer                              // 消费者节点 (可选?)
///       |--{applicationName}:{port}
///    |--provider                              // 提供者节点
///       |--192.168.43.227:8183
///       |--192.168.43.227:8184
///       |--{ip}:{port}
///```
/// 从 /irpc/{serviceName}/provider获取ip
///
/// @author cyang
/// @author linhao
/// @since created in 4:44 下午 2021/12/11
@Slf4j
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private static final String ROOT = "/irpc";
    private static final String PROVIDER_NODE = "/provider";
    private static final String CONSUMER_NODE = "/consumer";

    private AbstractZookeeperClient zkClient;

    /**
     * zookeeper 的地址.
     * 踩坑了, client的 register address忘记配置
     *
     * @param address nonnull
     */
    public ZookeeperRegister(@NonNull String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }

    private String getProviderPath(URL url) {
        return ROOT
                + "/" + url.getServiceName()
                + PROVIDER_NODE
                + "/" + url.getParameters().get(HOST) + ":" + url.getParameters().get(PORT);
    }

    // 为何有冒号在末尾?
    private String getConsumerPath(URL url) {
        return ROOT
                + "/" + url.getServiceName()
                + CONSUMER_NODE
                + "/" + url.getApplicationName() + ":" + url.getParameters().get(HOST) + ":";
    }

    /// e.g. `/irpc/{serviceName}/provider`
    ///
    /// @param serviceName e.g. DataService
    /// @return {ip}:{port}, e.g. 127.0.0.1:8080
    @Override
    public List<String> getProviderAddresses(@NonNull String serviceName) {
        log.debug("Fetching providers for service {}", serviceName);
        return zkClient.getChildren(ROOT + "/" + serviceName + PROVIDER_NODE);
    }

    /**
     * server使用的
     *
     * @param url
     */
    @Override
    public void register(URL url) {
        // 1. 创建根节点
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        // 2. 在节点放数据
        //
        // 下面是 providerPath的结构, 参考dubbo
        // |--irpc
        //    |--com.idea.cyan.service.DataService
        //       |--consumer
        //       |--provider
        //          |--192.168.43.227:8183
        //          |--192.168.43.227:8184
        String urlStr = URL.buildProviderUrlStr(url);
        // 3. 如果节点存在
        if (zkClient.existNode(getProviderPath(url))) {
            // todo: 为何要删除节点? 如果是我自己写代码会写上么? 我觉得是避免上次服务的数据还没过期, 抛出异常
            zkClient.deleteNode(getProviderPath(url));
        }
        // 4. 创建临时节点, 服务下线过一会儿会消失
        zkClient.createTemporaryData(getProviderPath(url), urlStr);
        // 5. 将url 放入全局的List中, 在路由层会更新权重以及调用顺序的List
        super.register(url);
    }

    @Override
    public void unRegister(URL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildConsumerUrlStr(url);
        if (zkClient.existNode(getConsumerPath(url))) {
            zkClient.deleteNode(getConsumerPath(url));
        }
        zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        super.subscribe(url);
    }

    @Override
    public void doAfterSubscribe(URL url) {
        // 1. 监听是否有新的服务注册
        // todo 可以写入 this? 这样可能会减少直观性
        String newProviderNodePath = getProviderNodePath(url);
        log.debug("watch provider addresses for service {}", url.getServiceName());
        watchChildNodeList(newProviderNodePath);

        // 2. 监听权重是否发生变化
        // list 里放服务提供者地址(ip:port), see this 的 getProviderIps
        // 在client 订阅服务时, 会放入
        String providerAddressesJson = url.getParameters().get(PROVIDER_ADDRESSES_JSON_STRING);
        log.debug("provider addresses for service {}", providerAddressesJson);
        // todo 这里使用了 fastjson, 可以看看业界最成熟的方案和fastjson2
        List<String> providerAddressList = JSON.parseObject(providerAddressesJson, new TypeReference<>() {
        });
        log.debug("provider addresses for service {}", providerAddressesJson);
        for (String addr :  providerAddressList) {
            watchNodeDataChange(newProviderNodePath + "/" + addr);
        }
    }

    private static String getProviderNodePath(URL url) {
        return String.format("%s/%s%s", ROOT, url.getServiceName(), PROVIDER_NODE);
    }

    /**
     * watch address
     * 服务提供者地址上下线, 获得新的ip和port或者下线
     * @param newProviderNodePath e.g. /{root}/{serviceName}/provider
     */
    public void watchChildNodeList(String newProviderNodePath) {
        zkClient.watchChildNodeList(newProviderNodePath, addrUpdateEvent -> {
            log.debug("watched event: {}", addrUpdateEvent);
            String providerPath = addrUpdateEvent.getPath();
            log.debug("path: {}", providerPath);
            List<String> addresses = zkClient.getChildren(providerPath);
            // todo path的split应该写入url
            URLChangeWrapper addrUpdateWrapper = new URLChangeWrapper(addresses, getServiceName(providerPath));
            log.debug("URLChangeWrapper: {}", addrUpdateWrapper);
            //自定义的一套事件监听组件
            IRpcListenerLoader.sendEvent(new IRpcUpdateEvent(addrUpdateWrapper));
            //收到回调之后再注册一次监听，这样能保证一直都收到消息
            watchChildNodeList(providerPath);
        });
    }

    /**
     *
     * @param providerPath 上面
     * @return 2 是对应 build provider
     */
    private static String getServiceName(String providerPath) {
        return providerPath.split("/")[2];
    }

    /**
     * 获取weight的变化
     *
     * @param newServerNodePath
     */
    public void watchNodeDataChange(String newServerNodePath) {
        zkClient.watchNodeData(newServerNodePath, watchedEvent -> {
            log.debug("watchNodeDataChange: {}", watchedEvent);
            String path = watchedEvent.getPath();
            String nodeData = zkClient.getNodeData(path);
            String replace = nodeData.replace(";", "/");
            ProviderNodeInfo providerNodeInfo = URL.buildUrlFromUrlStr(replace);
            IRpcEvent iRpcEvent = new IRpcNodeDataChangeEvent(providerNodeInfo);
            IRpcListenerLoader.sendEvent(iRpcEvent);
        });
    }

    @Override
    public void doBeforeSubscribe(URL url) {

    }

    @Override
    public void doUnSubscribe(URL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubscribe(url);
    }

    public static void main(String[] args) throws InterruptedException {

        // 初始化测试
        ZookeeperRegister register = new ZookeeperRegister("localhost:2181");
        URL url = new URL();
        url.setServiceName(DataService.class.getName());

        // 测试 provider address
        List<String> addresses = register.getProviderAddresses(url.getServiceName());
        log.info("addresses (port:ip): {}", addresses);

        // 测试 afterSubscribe
        register.doAfterSubscribe(url);

        Thread.sleep(2000000);
    }
}
