package org.idea.irpc.framework.impl;

import org.idea.irpc.framework.interfaces.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }
}
