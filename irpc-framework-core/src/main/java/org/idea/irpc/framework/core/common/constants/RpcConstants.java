package org.idea.irpc.framework.core.common.constants;

/**
 * @author cyang
 */
public class RpcConstants {
    // proxy
    public static final short MAGIC_NUMBER = 0xCA;

    public static final String JDK_PROXY = "jdk";
    // todo javasist

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
}
