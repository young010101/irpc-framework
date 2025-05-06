package org.idea.irpc.framework.core.common.event.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 包装服务名称及对应的提供者 URL 列表。
 * <br>
 * 未来可以按照gpt建议, 每个 URL 通常包含协议、IP 地址、端口号、服务接口名及参数。
 * <br>示例格式: {@code dubbo://ip:port/interfaceName?key=value&key2=value2}
 * <br>例如:    {@code dubbo://127.0.0.1:20880/com.example.DemoService?version=1.0.0}
 *
 * @author cyang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class URLChangeWrapper {

    /// `ip:port` of provider
    ///
    /// `ip:port` 在命名时，取决于上下文用途。以下是几种常见的命名方式和适用场景：
    ///
    /// ---
    ///
    /// ### ✅ 常见命名方式
    ///
    /// | 命名方式                | 适用场景                                     |
    /// | ------------------- | ---------------------------------------- |
    /// | `endpoint`          | 一般用途，强调是一个“终端地址”                         |
    /// | `address`           | 简洁通用，常用于配置项，如 `serverAddress`            |
    /// | `host` / `hostPort` | `host` 可表示 IP，也可带端口，如 `hostPort`         |
    /// | `ipPort`            | 直白表达 IP 和端口的组合，如 `serverIpPort`          |
    /// | `socketAddress`     | 更技术化，常用于底层网络编程（如 Java 的 `SocketAddress`） |
    /// | `target`            | 常用于负载均衡/服务发现中，表示目标服务节点                   |
    /// | `remote`            | 表示远端地址，如 `remoteAddr`、`remoteEndpoint`   |
    /// | `bindAddress`       | 表示监听地址，用于服务器绑定监听本地端口                     |
    ///
    /// ---
    ///
    /// ### 🚫 不建议使用的模糊命名
    ///
    /// * `url`：URL 通常包含协议（如 `http://`），不能准确表达仅为 `ip:port`
    /// * `domain`：域名可能不包含端口，意义不准确
    /// * `location`：含义太泛
    ///
    /// ---
    ///
    /// ### ✅ 示例
    ///
    /// ```java
    /// String endpoint = "127.0.0.1:8080";
    /// String serverAddress = "192.168.1.1:9000";
    /// String remoteEndpoint = "service-node-1:5000";
    /// ```
    ///
    /// ---
    ///
    /// 你可以根据具体语境选一个清晰表达含义的名称。比如客户端发请求就叫 `serverAddress`，服务器监听就叫 `bindAddress`。
    ///
    /// 你现在是在哪个场景下使用这个 ip\:port？是客户端发起请求，还是服务器监听？
    private List<String> providerAddresses;

    /**
     * 服务名 (通常是服务接口的全限定名)。
     * 例如: {@code com.example.DemoService}
     */
    private String serviceName;
}
