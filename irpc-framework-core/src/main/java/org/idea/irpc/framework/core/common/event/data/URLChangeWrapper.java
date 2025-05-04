package org.idea.irpc.framework.core.common.event.data;

import lombok.Data;

import java.util.List;

/**
 * 包装服务名称及对应的提供者 URL 列表。
 * <p>
 * 示例:
 * <ul>
 *   <li>providerUrl: ["dubbo://127.0.0.1:20880/com.example.DemoService?version=1.0.0",
 *                      "dubbo://127.0.0.2:20880/com.example.DemoService?version=1.0.0"]</li>
 *   <li>serviceName: "com.example.DemoService"</li>
 * </ul>
 * </p>
 * @apiNote 注意: 当前的url不长这个样子
 * @author cyang
 */
@Data
public class URLChangeWrapper {

    /**
     * 提供者 URL 列表。
     * <p>
     * 每个 URL 通常包含协议、IP 地址、端口号、服务接口名及参数。
     * 示例格式: {@code dubbo://ip:port/interfaceName?key=value&key2=value2}
     * <br>例如: {@code dubbo://127.0.0.1:20880/com.example.DemoService?version=1.0.0}
     * </p>
     */
    private List<String> providerUrl;

    /**
     * 服务名 (通常是服务接口的全限定名)。
     * 例如: {@code com.example.DemoService}
     */
    private String serviceName;
}
