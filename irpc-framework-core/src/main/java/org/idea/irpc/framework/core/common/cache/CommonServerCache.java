package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.filter.server.ServerFilterChain;
import org.idea.irpc.framework.core.registy.RegistryService;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.serialize.SerializerFactory;
import org.idea.irpc.framework.core.service.ServiceBeanWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cyang
 */
public class CommonServerCache {


    // =========proxy, lesson2===========


    /**
     * key是类的权限定名, Object 是服务对象, e.g. DataService
     */
    public static final Map<String, Object> PROVIDED_CLASSES_MAP = new HashMap<>();


    // ==========register, lesson3==============

    /**
     * 服务的URL
     */
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();

    /**
     * e.g. zookeeper
     */
    public static RegistryService REGISTRY_SERVICE;


    // ==========serialize, lesson5===============


    public static SerializerFactory SERVER_SERIALIZER;


    // ===============filter, lesson6===================


    public static ServerFilterChain  SERVER_FILTER_CHAIN;

    /// 过滤链中过滤token时使用
    public static Map<String, ServiceBeanWrapper> SERVICE_BEAN_WRAPPER_MAP = new ConcurrentHashMap<>();
}
