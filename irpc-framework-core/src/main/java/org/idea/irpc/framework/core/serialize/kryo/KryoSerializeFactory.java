package org.idea.irpc.framework.core.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.idea.irpc.framework.core.serialize.SerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author cyang
 */
public class KryoSerializeFactory implements SerializerFactory {
    public static final ThreadLocal<Kryo> KRYO = ThreadLocal.withInitial(Kryo::new);

    @Override
    public <T> byte[] serialize(T obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            Kryo kryo = KRYO.get();
            kryo.writeClassAndObject(output, obj);
            // todo: flush 非常关键
            output.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * kryo 需要 no args constructor
     *
     * @param bytes rpcInvocation 的 content byte数组
     * @param clazz 强转为实际返回对象
     * @param <T> 实际返回对象的类型
     * @return 调用远程方法返回的结果
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             Input input = new Input(bais)) {
            Kryo kryo = KRYO.get();
            Object o = kryo.readClassAndObject(input);
            // gpt 建议的安全强转
            return clazz.cast(o);
        } catch (Exception e) {
            throw new RuntimeException("Kryo deserialization failed", e);
        }
    }
}
