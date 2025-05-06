package org.idea.irpc.framework.core.spi.jdk;

import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

/**
 * @author cyang
 */
@Slf4j
public class Demo {
    public static void demo(ISpiTest spiTest) {
        log.info("begin");
        spiTest.doTest();
        log.info("end");
    }

    public static void main(String[] args) {
        ServiceLoader<ISpiTest> loader = ServiceLoader.load(ISpiTest.class);
        for (ISpiTest spiTest : loader) {
            demo(spiTest);
        }
    }
}
