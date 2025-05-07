package org.idea.irpc.framework.core.spi;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.serialize.SerializerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ExtensionLoaderTest {

    @Test
    void loadExtensions() throws IOException, ClassNotFoundException {
        ExtensionLoader extensionLoader = new ExtensionLoader();
        extensionLoader.loadExtensions(SerializerFactory.class);
        log.info(String.valueOf(extensionLoader.EXTENSION_LOADED_CLASS_CACHE.get(SerializerFactory.class.getName())));;
    }
}