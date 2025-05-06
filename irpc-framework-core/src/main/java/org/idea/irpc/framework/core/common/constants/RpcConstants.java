package org.idea.irpc.framework.core.common.constants;

/**
 * @author cyang
 */
public class RpcConstants {
    // proxy
    public static final short MAGIC_NUMBER = 0xCA;

    public static final String JDK_PROXY = "jdk";
    // todo javassist

    // register
    /// used by `URL`
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String WEIGHT = "weight";
    @Deprecated
    public static final String SERVICE_PATH = "servicePath";
    public static final String PROVIDER_ADDRESSES_JSON_STRING = "providerIps";

    // route
    public static final String RANDOM_ROUTE_STRATEGY = "random";
    public static final String ROTATE_ROUTE_STRATEGY = "rotate";

    // serializer
    public static final String FAST_JSON_SERIALIZE_STRATEGY = "fastJson";
    public static final String KRYO_SERIALIZE_STRATEGY = "kryo";

    // filter
    public static final String C_APP_NAME = "c_app_name";
    public static final String SERVICE_TOKEN = "service_token";
    public static final String DEFAULT_GROUP = "default_group";
    public static final String DEFAULT_TOKEN = "";
    public static final Integer DEFAULT_LIMIT = -1;
    public static final String GROUP_STRING = "group";
    public static final String LIMIT_STRING = "limit";

    // dev
    public static final String DEV_STRING = "dev";
    public static final String DEV_TOKEN = "token-a";
}
