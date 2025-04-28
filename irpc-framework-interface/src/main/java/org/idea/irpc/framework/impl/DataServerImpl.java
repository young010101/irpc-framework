package org.idea.irpc.framework.impl;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.interfaces.DataService;

/**
 * @author cyang
 */
@Slf4j
public class DataServerImpl  implements DataService {
    @Override
    public String hello(String test) {
        log.info("hello data service");
        return "[dataServer]: hello, " + test;
    }
}
