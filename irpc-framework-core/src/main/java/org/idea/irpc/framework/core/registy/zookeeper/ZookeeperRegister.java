package org.idea.irpc.framework.core.registy.zookeeper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.event.IRpcEvent;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
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
/// ```
/// 从 /irpc/{serviceName}/provider获取ip
/// @author cyang
/// @author linhao
/// @since created in 4:44 下午 2021/12/11
@Slf4j
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private static final String ROOT = "/irpc";
    private static final String PROVIDER_NODE = "/provider";
    private static final String CONSUMER_NODE = "/consumer";

    private AbstractZookeeperClient zkClient;


    private String getProviderPath(URL url) {
        return ROOT
                + "/" + url.getServiceName()
                + PROVIDER_NODE
                +"/" + url.getParameters().get(HOST) + ":" + url.getParameters().get(PORT);
    }

    private String getConsumerPath(URL url) {
        return ROOT
                + "/" + url.getServiceName()
                + CONSUMER_NODE
                + "/" + url.getApplicationName() + ":" + url.getParameters().get(HOST) + ":";
    }

    /**
     * 踩坑了, client的 register address忘记配置
     *
     * @param address nonnull
     */
    public ZookeeperRegister(@NonNull String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }


    /// `/irpc/{serviceName}/provider`
    @Override
    public List<String> getProviderIps(@NonNull String serviceName) {
        log.debug("Fetching providers for service {}", serviceName);
        return zkClient.getChildrenData(ROOT + "/" + serviceName + PROVIDER_NODE);
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
        //监听是否有新的服务注册
        String newServerNodePath = String.format("%s/%s%s", ROOT, url.getServiceName(), PROVIDER_NODE);
        // see this 的 getProviderIps
        String providerIpsJson = url.getParameters().get(PROVIDER_IPS);
        List<String> list = JSON.parseObject(providerIpsJson, new TypeReference<List<String>>() {});
        watchChildNodeList(newServerNodePath);
    }

    public void watchChildNodeList(String newServerNodePath) {
        zkClient.watchChildNodeList(newServerNodePath, watchedEvent -> {
            System.out.println(watchedEvent);
            String path = watchedEvent.getPath();
            List<String> childrenDataList = zkClient.getChildrenData(path);
            URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
            urlChangeWrapper.setProviderUrl(childrenDataList);
            urlChangeWrapper.setServiceName(path.split("/")[2]);
            //自定义的一套事件监听组件
            IRpcEvent iRpcEvent = new IRpcUpdateEvent(urlChangeWrapper);
            IRpcListenerLoader.sendEvent(iRpcEvent);
            //收到回调之后在注册一次监听，这样能保证一直都收到消息
            watchChildNodeList(path);
        });
    }

    /**
     * 获取weight的变化
     * @param newServerNodePath
     */
    public void watchNodeDataChange(String newServerNodePath) {
        zkClient.watchNodeData(newServerNodePath, watchedEvent -> {
            log.debug("watchNodeDataChange: {}", watchedEvent);
            String path = watchedEvent.getPath();
            String nodeData = zkClient.getNodeData(path);
            String replace = nodeData.replace(";", "/");

            ProviderNodeInfo providerNodeInfo = URL.buildUrlFromUrlStr(replace);

            IRpcEvent iRpcEvent = new IRpcUpdateEvent(providerNodeInfo);
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
        ZookeeperRegister zookeeperRegister = new ZookeeperRegister("localhost:2181");
        List<String> urls = zookeeperRegister.getProviderIps(DataService.class.getName());
        System.out.println(urls);
        Thread.sleep(2000000);
    }
}
