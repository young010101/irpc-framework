package org.idea.irpc.framework.core.route;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.interfaces.DataService;

import java.util.ArrayList;
import java.util.List;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.*;

/**
 * @author cyang
 */
@Slf4j
public class RotateRouteImpl implements IRoute {

    @Override
    public void refreshRouteArr(Selector selector) {
        List<ChannelFutureWrapper> wrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        SERVICE_ROUTE_MAP.put(
                selector.getProviderServiceName(),
                wrappers.toArray(new ChannelFutureWrapper[0])
        );
    }

    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getProviderServiceName());
    }

    @Override
    public void update(URL url) {

    }

    public static void main(String[] args) {
        List<ChannelFutureWrapper> wrappers = new ArrayList<>();
        wrappers.add(new ChannelFutureWrapper(null, 2181, 100));
        wrappers.add(new ChannelFutureWrapper(null, 2181, 1000));
        wrappers.add(new ChannelFutureWrapper(null, 2181, 900));
        RotateRouteImpl route = new RotateRouteImpl();
        Selector selector = new Selector(DataService.class.getName());
        String serviceName = selector.getProviderServiceName();
        CONNECT_MAP.put(serviceName,wrappers);

        route.refreshRouteArr(selector);
        ChannelFutureWrapper[] ws = SERVICE_ROUTE_MAP.get(serviceName);
        assert ws.length == 3;
        assert ws[0] == wrappers.get(0);
        for (ChannelFutureWrapper wrapper : ws) {
            log.debug("service route: {}", wrapper);
        }
    }
}
