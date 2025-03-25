package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommonClientCache {
    public static final List<String> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();
    public static final Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();
    public static final Set<String> SERVER_ADDRESS = new HashSet<>();
    public static final Map<String, Object> REST_MAP = new HashMap<>();
}
