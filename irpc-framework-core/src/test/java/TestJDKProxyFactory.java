import org.idea.irpc.framework.core.client.RpcReferenceWrapper;
import org.idea.irpc.framework.core.proxy.ProxyFactory;
import org.idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.irpc.framework.impl.HelloServiceImpl;
import org.idea.irpc.framework.interfaces.HelloService;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestJDKProxyFactory {
    @Test
    public void testJDKProxyFactory() throws Exception {
        ProxyFactory proxyFactory = new JDKProxyFactory();
        HelloService helloService = proxyFactory.getProxy(new RpcReferenceWrapper<>(HelloService.class, Map.of()));
        helloService.sayHello("irpc");
    }
}
