package org.idea.irpc.framework.core.spi.jdk;

import lombok.extern.slf4j.Slf4j;

/**
 * @author cyang
 */
@Slf4j
public class DefaultSpiTest implements ISpiTest {

    @Override
    public void doTest() {
        log.info("doTest");
    }
}
