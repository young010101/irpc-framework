package org.idea.irpc.framework.core.serialize.fastjson;

import com.alibaba.fastjson2.JSON;
import org.idea.irpc.framework.core.serialize.SerializerFactory;

/**
 * @author cyang
 */
public class FastJsonSerializerFactory implements SerializerFactory {

    /// `JSON.toJSONString(obj).getBytes(StandardCharsets.UTF_8);`
    @Override
    public <T> byte[] serialize(T obj) {
        return JSON.toJSONString(obj).getBytes();
    }

    /// `JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), clazz);`
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }
}
