package org.idea.irpc.framework.core.serialize;

/**
 * @author cyang
 */
public interface SerializerFactory {
    <T> byte[] serialize(T obj);
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
