import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.CuratorZookeeperClient;
import org.idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.idea.irpc.framework.interfaces.DataService;
import org.junit.Test;

import java.util.List;

@Slf4j
public class TestZookeeperRegister {
    @Test
    public void test() throws InterruptedException {
        ZookeeperRegister zookeeperRegister = new ZookeeperRegister("localhost:2181");
        List<String> urls = zookeeperRegister.getProviderIps(DataService.class.getName());
        System.out.println(urls);
        Thread.sleep(2000000);
    }


    @Test
    public void testGetFromURL() {
        URL url = new URL();
        url.setApplicationName("test-application");
        url.setServiceName("test-service");
        url.addParameter("host", "localhost");
        url.addParameter("port", "2181");
        log.info("{}", url.toString());
        ZookeeperRegister zookeeperRegister = new ZookeeperRegister("localhost:2181");
        zookeeperRegister.register(url);
        log.info("{}", zookeeperRegister.getProviderIps(DataService.class.getName()));
    }

    @Test
    public void testURL() {
        URL url = new URL();
        url.setApplicationName("test-application");
        url.setServiceName("test-service");
        url.addParameter("host", "localhost");
        url.addParameter("port", "2181");
        log.info("{}", url.toString());
    }

    @Test
    public void testCurator() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        curatorFramework.start();
        byte[] res = curatorFramework.getData().forPath("/irpc");
        String o = new String(res);
        System.out.println(o);
    }

    @Test
    public void testCuratorClient() {
        CuratorZookeeperClient client = new CuratorZookeeperClient("localhost:2181");
        if (client.existNode("/demo")) {
            client.deleteNode("/demo");
        }
        client.createPersistentData("/demo", "hello");
        String data = client.getNodeData("/demo");
        System.out.println(data);
        client.deleteNode("/demo");
        assert !client.existNode("/demo");
//        client.createPersistentWithSeqData("/demo2", "hello");
        client.createTemporarySeqData("/demo3", "hello");
    }
}
