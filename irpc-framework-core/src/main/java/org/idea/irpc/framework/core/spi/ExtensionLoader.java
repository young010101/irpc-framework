package org.idea.irpc.framework.core.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类的作用是实现简单的spi机制
 *
 * <p>根据接口名称读取 {@code META-INF/mini-rpc/{接口权限定名}}的配置文件，
 * 将配置文件下的实现类名->实现类的映射加载到内存，
 * 并缓存起来
 *
 * @author cyang
 */
public class ExtensionLoader {

    /// 配置文件路径
    public static final String DIR_PREFIX = "META-INF/mini-rpc/";

    /// 保留文件中定义的顺序
    ///
    /// 同名实现， 后面加载的会覆盖前面的
    public static final Map<String, LinkedHashMap<String, Class<?>>> EXTENSION_LOADED_CLASS_CACHE
            = new ConcurrentHashMap<>();

    /// todo static or not?
    ///
    /// ## 使用示例
    ///
    /// 假设你有接口：
    ///
    /// ```java
    /// package com.example;
    /// public interface Protocol {
    ///     void connect();
    ///}
    ///```
    ///
    /// 在任意 JAR 的 `META-INF/mini-rpc/com.example.Protocol` 文件中写：
    ///
    /// ```
    ///# 默认实现
    /// default=com.example.impl.DefaultProtocol
    ///# 高性能实现
    /// fast=com.example.impl.FastProtocol
    ///```
    ///
    /// 然后调用：
    ///
    /// ```java
    /// ExtensionLoader loader = new ExtensionLoader();
    /// loader.loadExtension(Protocol.class);
    ///
    /// // 从缓存里拿到映射
    /// Map<String, Class> map = ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE.get(Protocol.class.getName());
    /// Class defaultCls = map.get("default");   // com.example.impl.DefaultProtocol.class
    /// Object instance = defaultCls.newInstance();  // 动态实例化
    ///```
    ///
    /// ---
    ///
    /// ## 注意事项
    ///
    /// 1. **配置文件路径**
    ///    一定要放在 `resources/META-INF/mini-rpc/接口全限定名` 下，且文件名与接口的全类名完全一致。
    ///
    /// 2. **顺序与覆盖**
    ///
    ///    * `LinkedHashMap` 保留文件中定义的顺序。
    ///    * 如果多个 JAR 都提供了同名实现，后加载的会覆盖前面的同名 key。
    ///
    /// 3. **性能与安全**
    ///
    ///    * 一次性加载并缓存，后续直接从内存取，无 I/O 开销。
    ///    * 使用 `Class.forName` 可能触发静态块，需要确保安全。
    ///    * 对并发场景友好，`ConcurrentHashMap` 保证线程安全。
    ///
    /// 4. **扩展性**
    ///    如果想自定义 SPI 加载逻辑（比如支持属性文件、XML、注解等），可以继承或改造 `loadExtension` 方法。
    ///
    /// @param clazz 接口全限定名
    public void loadExtensions(Class<?> clazz) throws IOException, ClassNotFoundException {
        // 1. 参数校验
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        // 2. 定位SPI 文件路径
        String spiFilePath = DIR_PREFIX + clazz.getName();

        // 3. 获取所有URL 资源
        ClassLoader classLoader = clazz.getClassLoader();
        Enumeration<URL> resources = classLoader.getResources(spiFilePath);
        // 加载jar包或classpath下的同名spi， 支持“可插拔” 扩展
        // todo classpath是指？

        // 4. 逐个 URL 处理
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            // 4.1 打开流, 按行读取
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            LinkedHashMap<String, Class<?>> classMap = new LinkedHashMap<>();
            while ((line = bufferedReader.readLine()) != null) {
                // 4.2 忽略 `#` 开头的
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                // 4.3 `=` 分割, key 是properties 指定的关键字， 右边是实现类全名
                String[] split = line.split("=");
                String implClassName = split[0];
                String interfaceName = split[1];
                // 4.4 `Class.forName` 加载对应的类对象 (只会加载类的字节码， 不实例化)
                // Class.forName可能触发静态代码块
                classMap.put(implClassName, Class.forName(interfaceName));
            }

            // 5. 合并到缓存
            if (EXTENSION_LOADED_CLASS_CACHE.containsKey(clazz.getName())) {
                // 5.1 已有缓存， 追加 （开发者自定义配置可以覆盖或补充）
                EXTENSION_LOADED_CLASS_CACHE.get(clazz.getName()).putAll(classMap);
            } else {
                // 5.2 首次加载，直接放入
                EXTENSION_LOADED_CLASS_CACHE.put(clazz.getName(), classMap);
            }
        }
    }
}
