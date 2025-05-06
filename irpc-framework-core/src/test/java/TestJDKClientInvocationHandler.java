import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.RpcInvocation;
import org.idea.irpc.framework.core.common.cache.CommonClientCache;
import org.idea.irpc.framework.core.proxy.ProxyFactory;
import org.idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.irpc.framework.interfaces.HelloService;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestJDKClientInvocationHandler {
    @Test
    public void test() throws Exception {
        ProxyFactory proxyFactory = new JDKProxyFactory();
        HelloService helloService = proxyFactory.getProxy(HelloService.class);

        // 启动一个异步线程模拟服务器处理
        new Thread(() -> {
            while (true) {
                try {
                    RpcInvocation invocation = CommonClientCache.SEND_QUEUE.take();
                    System.out.println("服务器模拟收到请求: " + invocation.getTargetMethod());

                    // 模拟处理，比如简单返回"Hello " + 参数
                    invocation.setResponse("Hello " + invocation.getArgs()[0]);

                    // 更新 RESP_MAP，把 OBJECT 替换成真正的结果
                    CommonClientCache.RESP_MAP.put(invocation.getUuid(), invocation);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).start();

        // 通过代理调用
        String result = helloService.sayHello("world");
        System.out.println("调用结果: " + result);
    }
}
