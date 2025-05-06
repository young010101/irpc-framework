package org.idea.irpc.framework.serialize;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.common.User;
import org.idea.irpc.framework.common.UserBootstrap;
import org.idea.irpc.framework.core.serialize.SerializerFactory;
import org.idea.irpc.framework.core.serialize.kryo.KryoSerializeFactory;

/**
 * @author cyang
 */
@Slf4j
public class TestSerializer {
    private static final User USER = UserBootstrap.buildUser();

    public static void main(String[] args) {
        SerializerFactory factory = new KryoSerializeFactory();
        byte[] b = factory.serialize(USER);
        User deserialize = factory.deserialize(b, User.class);
        log.info("{}", deserialize);
    }
}
