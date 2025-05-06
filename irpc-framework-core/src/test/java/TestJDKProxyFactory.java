import org.idea.irpc.framework.core.proxy.ProxyFactory;
import org.idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.irpc.framework.interfaces.HelloService;
import org.junit.jupiter.api.Test;

public class TestJDKProxyFactory {
    @Test
    public void testJDKProxyFactory() throws Exception {
        ProxyFactory proxyFactory = new JDKProxyFactory();
        HelloService helloService = proxyFactory.getProxy(HelloService.class);
        helloService.sayHello("irpc");
    }
}
