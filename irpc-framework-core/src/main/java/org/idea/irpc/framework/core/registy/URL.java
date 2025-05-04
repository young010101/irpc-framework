package org.idea.irpc.framework.core.registy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.registy.zookeeper.ProviderNodeInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.*;

/**
 * lesson3 提到的第二个类.
 * 配置类, 参考dubbo的配置总线(?)
 *
 * @author Cheng Yang
 * @author linhao
 * @since created in 3:48 下午 2021/12/11
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
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
     * <li>host : 服务提供者的地址, ip地址</li>
     * <li>port : 服务提供者的端口</li>
     * <li>weight : 权重, 100的整数倍</li>
     * <li>servicePath : serviceName + "/provider"</li>
     * <li>providerIps : 用JSON.toJSONString将List转为String, 这个list是从register获取的, register又是从</li>
     * <li>分组</li>
     * <li>...</li>
     * </ul>
     */
    private Map<String, String> parameters = new HashMap<>();

    public void addParameter(String key, String value) {
        this.parameters.putIfAbsent(key, value);
    }

    /**
     * 将URL转换为写入zk的provider节点下的一段字符串
     * 可以解析出 providerNodeInfo
     * <p>相当于 zookeeper的 node data</p>
     *
     * @param url split by ";", len == 5;
     * @return node data
     */
    public static String buildProviderUrlStr(URL url) {
        String host = url.getParameters().get(HOST);
        String port = url.getParameters().get(PORT);
        // 如果null, 会添加 "null" 字符串
        String weight = url.getParameters().get(WEIGHT);

        return String.format("%s;%s;%s:%s;%s;%s",
                url.getApplicationName(),
                url.getServiceName(),
                host, port,
                System.currentTimeMillis(),
                weight);
    }

    /**
     * 将URL转换为写入zk的consumer节点下的一段字符串
     *
     * @param url 服务与消费者
     * @return 放入zk consumer 节点的一段字符串 todo: 有什么用?
     */
    public static String buildConsumerUrlStr(URL url) {
        String host = url.getParameters().get(HOST);
        return String.format("%s;%s;%s;%s",
                url.getApplicationName(),
                url.getServiceName(),
                host,
                System.currentTimeMillis());
        // todo 有必要这么写么? 上一个provider的url已经被我改变了
        // 避免一些中文charset的错误转换?
//        return new String(
//                (url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis())
//                        .getBytes(),
//                StandardCharsets.UTF_8);
    }

    /**
     * 将某个节点下的信息转换为一个Provider节点对象
     *
     * @param providerNodeStr 节点的data, 格式请参考{@code buildProviderUrlStr}, 将 ';' 替换为 '/'
     * @return serviceName and address
     */
    public static ProviderNodeInfo buildUrlFromUrlStr(String providerNodeStr) {
        String[] items = providerNodeStr.split("/");
        return new ProviderNodeInfo(items[1], items[2], items[3], Integer.valueOf(items[4]));
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>(Map.of(HOST, "127.0.0.1", PORT, "8080", WEIGHT, "1"));

        URL url = new URL("app", "MyService", params);

        String providerNodeStr = buildProviderUrlStr(url);
        log.debug(providerNodeStr);

        String replace = providerNodeStr.replace(';', '/');
        ProviderNodeInfo providerNodeInfo = buildUrlFromUrlStr(replace);
        log.debug(providerNodeInfo.toString());

        log.debug(buildConsumerUrlStr(url));
    }
}
