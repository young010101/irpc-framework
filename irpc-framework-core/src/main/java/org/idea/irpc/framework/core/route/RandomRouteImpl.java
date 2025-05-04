package org.idea.irpc.framework.core.route;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.registy.URL;

import java.util.*;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.*;


/**
 * 随机筛选, 权重 + 随机
 *
 * @author Cheng Yang
 * @since 5/3
 */
@Slf4j
public class RandomRouteImpl implements IRoute {

    private static final int C_100 = 100;
    private final Random random = new Random();

    /**
     * 刷新路由数组
     * todo: 如何处理服务上下线?
     *
     * @param selector 服务名称 com.idea.cyan.service.DataService
     */
    @Override
    public void refreshRouteArr(Selector selector) {
        // 获取服务提供者们
        List<ChannelFutureWrapper> wrappers = CONNECT_MAP.get(selector.getProviderServiceName());

        // gpt 补充
        if (wrappers == null || wrappers.isEmpty()) {
            return;
        }

        // 获取提前算好的随机数组
        int len = wrappers.size();
        List<Integer> shuffledIndices = createShuffledIndices(len);
        ChannelFutureWrapper[] shuffledWrappers = new ChannelFutureWrapper[len];
        for (int i = 0; i < len; i++) {
            shuffledWrappers[i] = wrappers.get(shuffledIndices.get(i));
        }

        SERVICE_ROUTE_MAP.put(selector.getProviderServiceName(), shuffledWrappers);
    }

    /**
     * 获取一个服务
     *
     * @param selector 获取服务名, 为何要多包装一个? 有何设计?
     */
    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getProviderServiceName());
    }

    /**
     * 更新权重
     *
     * @param url 在注册层定义的. 为何上面用Selector这里用url?
     */
    @Override
    public void update(URL url) {
        List<ChannelFutureWrapper> wrappers = CONNECT_MAP.get(url.getServiceName());
        if (wrappers == null || wrappers.isEmpty()) {
            return;
        }

        List<Integer> weightIndexList = createWeightIndexList(wrappers);
        Collections.shuffle(weightIndexList, random);
        ChannelFutureWrapper[] finalWrappers = new ChannelFutureWrapper[weightIndexList.size()];
        for (int i = 0; i < weightIndexList.size(); i++) {
            finalWrappers[i] = wrappers.get(weightIndexList.get(i));
        }
        SERVICE_ROUTE_MAP.put(url.getServiceName(), finalWrappers);
    }

    /**
     * 权重是100的整数倍
     * 100 -> 重复 1次, 200 -> 重复2次, 900 -> 重复 9次
     * @param wrappers 已经建立的连接
     * @return 一个size == 100的List
     */
    private List<Integer> createWeightIndexList(List<ChannelFutureWrapper> wrappers) {
        List<Integer> result = new ArrayList<>(100);
        for (int i = 0; i < wrappers.size(); i++) {
            int count = wrappers.get(i).getWeight() / C_100;
            for (int j = 0; j < count; j++) {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * 标准的 Fisher–Yates 洗牌，不存在概率偏差
     * @param len len
     * @return shuffled indices
     */
    private List<Integer> createShuffledIndices(int len) {
        List<Integer> indices = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            indices.add(i);
        }
        // 为何要传入random? gpt建议
        Collections.shuffle(indices, random);
        return indices;
    }

    public static void main(String[] args) {
        List<ChannelFutureWrapper> wrappers = new ArrayList<>();
        wrappers.add(new ChannelFutureWrapper(null, 2181, 100));
        wrappers.add(new ChannelFutureWrapper(null, 2181, 1000));
        wrappers.add(new ChannelFutureWrapper(null, 2181, 8000));
        wrappers.add(new ChannelFutureWrapper(null, 2181, 900));

        RandomRouteImpl randomRouteImpl = new RandomRouteImpl();
        List<Integer> weightList= randomRouteImpl.createWeightIndexList(wrappers);
        log.info("weightList size:{}", weightList.size());
        log.info("weightList:{}", weightList);
    }
}
