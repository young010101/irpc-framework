package org.idea.irpc.framework.core.common.event.listener;

import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.client.ConnectionHandler;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.event.IRpcListener;
import org.idea.irpc.framework.core.common.event.IRpcUpdateEvent;
import org.idea.irpc.framework.core.common.event.data.URLChangeWrapper;
import org.idea.irpc.framework.core.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;

/**
 * @author Cheng Yang
 * @author linhao
 * @since created in 10:35 下午 2021/12/18
 */
@Slf4j
public class ServiceUpdateListener implements IRpcListener<IRpcUpdateEvent> {

    @Override
    public void callback(Object t) {
        // 1. 获取到字节点的数据信息
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) t;
        // 2. 获取本地连接缓存, 当前已有的连接列表
        List<ChannelFutureWrapper> cacheWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());
        // 3. 校验当前连接列表
        if (CommonUtils.isEmptyList(cacheWrappers)) {
            log.error("[ServiceUpdateListener] cacheWrappers is empty");
            return;
        }

        // 4. 遍历旧连接, 筛选还有效的
        List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();
        Set<String> finalUrl = new HashSet<>();
        List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();
        for (ChannelFutureWrapper w : cacheWrappers) {
            String oldServerAddress = w.getHost() + ":" + w.getPort();
            //如果老的url没有，说明已经被移除了
            if (!matchProviderUrl.contains(oldServerAddress)) {
                // 直接continue, 老的连接会自动释放么? boostrap.connect
                continue;
            }
            finalChannelFutureWrappers.add(w);
            finalUrl.add(oldServerAddress);
        }
        // 5. 检查有没有新的provider, 建立新连接
        //此时老的url已经被移除了，开始检查是否有新的url
        //ChannelFutureWrapper其实是一个自定义的包装类，将netty建立好的ChannelFuture做了一些封装
        List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();
        for (String newProviderUrl : matchProviderUrl) {
            if (!finalUrl.contains(newProviderUrl)) {
                // 前半部是host, 后半部分是 port
                String[] splitUrl = newProviderUrl.split(":");
                String host = splitUrl[0];
                int port = Integer.parseInt(splitUrl[1]);
                ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper(host, port, 100);
                ChannelFuture channelFuture;
                try {
                    channelFuture = ConnectionHandler.createChannelFuture(host, port);
                    channelFutureWrapper.setChannelFuture(channelFuture);
                    newChannelFutureWrapper.add(channelFutureWrapper);
                    finalUrl.add(newProviderUrl);
                } catch (InterruptedException e) {
                    log.error("An error occurred while doing something", e);
                }
            }
        }

        // 6. 合并新老连接
        finalChannelFutureWrappers.addAll(newChannelFutureWrapper);

        // 7. 更新全局缓存
        //最终更新服务在这里
        CONNECT_MAP.put(urlChangeWrapper.getServiceName(), finalChannelFutureWrappers);
    }
}
