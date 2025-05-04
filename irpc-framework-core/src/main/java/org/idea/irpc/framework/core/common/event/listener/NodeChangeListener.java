package org.idea.irpc.framework.core.common.event.listener;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.event.IRpcListener;
import org.idea.irpc.framework.core.common.event.IRpcNodeDataChangeEvent;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.ProviderNodeInfo;

import java.util.List;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.I_ROUTE;

/**
 * @author cyang
 */
public class NodeChangeListener implements IRpcListener<IRpcNodeDataChangeEvent> {
    /**
     * 更新本地的各种缓存
     * <li>Service Update: 更新本地服务对应的</li>
     *
     * @param t 从 event中获取的data
     */
    @Override
    public void callback(Object t) {
        ProviderNodeInfo providerNodeInfo = (ProviderNodeInfo) t;
        String serviceName = providerNodeInfo.getServiceName();
        List<ChannelFutureWrapper> wrappers = CONNECT_MAP.get(serviceName);
        for  (ChannelFutureWrapper wrapper : wrappers) {
            String addr = wrapper.getHost() + ":" + wrapper.getPort();
            if (providerNodeInfo.getAddress().equals(addr)) {
                wrapper.setWeight(providerNodeInfo.getWeight());
                URL url = new URL();
                url.setServiceName(serviceName);
                I_ROUTE.update(url);
                break;
            }
        }
    }
}
