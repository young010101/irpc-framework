package org.idea.irpc.framework.core.registy;

import lombok.Data;
import org.idea.irpc.framework.core.registy.zookeeper.ProviderNodeInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * lesson3 提到的第二个类
 * 
 * @author Cheng Yang
 * @author linhao
 * @since created in 3:48 下午 2021/12/11
 */
@Data
public class URL {

    /**
     * 服务应用名称
     */
    private String applicationName;

    /**
     * 注册到节点到服务名称，例如：com.sise.test.UserService
     */
    private String serviceName;

    /**
     * 这里面可以自定义不限进行扩展
     * <ul>
     * <li>分组</li>
     * <li>权重</li>
     * <li>服务提供者的地址</li>
     * <li>服务提供者的端口</li>
     * </ul>
     */
    private Map<String, String> parameters = new HashMap<>();

    public void addParameter(String key, String value) {
        this.parameters.putIfAbsent(key, value);
    }

    /**
     * 将URL转换为写入zk的provider节点下的一段字符串
     *
     * @param url
     * @return
     */
    public static String buildProviderUrlStr(URL url) {
        String host = url.getParameters().get("host");
        String port = url.getParameters().get("port");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ":" + port + ";"
                + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将URL转换为写入zk的consumer节点下的一段字符串
     *
     * @param url
     * @return
     */
    public static String buildConsumerUrlStr(URL url) {
        String host = url.getParameters().get("host");
        return new String(
                (url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis())
                        .getBytes(),
                StandardCharsets.UTF_8);
    }

    /**
     * 将某个节点下的信息转换为一个Provider节点对象
     *
     * @param providerNodeStr
     * @return
     */
    public static ProviderNodeInfo buildUrlFromUrlStr(String providerNodeStr) {
        String[] items = providerNodeStr.split("/");
        ProviderNodeInfo providerNodeInfo = new ProviderNodeInfo();
        providerNodeInfo.setServiceName(items[2]);
        providerNodeInfo.setAddress(items[4]);
        return providerNodeInfo;
    }

    public static void main(String[] args) {
        URL url = new URL();
        url.setApplicationName("irpc-app");
        url.setServiceName("irpc-service");
        url.setParameters(new HashMap<>());
        url.getParameters().put("host", "localhost");
        url.getParameters().put("port", "8888");
        url.getParameters().put("key3", "value3");
        url.getParameters().put("key4", "value4");

        String providerNodeStr = buildProviderUrlStr(url);
        System.out.println(providerNodeStr);
        String consumerNodeStr = buildConsumerUrlStr(url);
        System.out.println(consumerNodeStr);
        // ProviderNodeInfo providerNodeInfo = buildURLFromUrlStr(providerNodeStr);
        // System.out.println(providerNodeInfo);
    }
}
