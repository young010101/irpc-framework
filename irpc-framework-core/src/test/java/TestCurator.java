import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

@Slf4j
public class TestCurator {
    public static CuratorFramework curatorFramework;
    public static void main(String[] args) throws Exception {
        // 3次retry, 第一次1s, 第二次2s, 第三次3s
        try (CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3))) {
            client.start();
            // 最简单的形式应该是 client.create().forPath(path, data);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/tmp/cyan/cyan/h", "I love hjq".getBytes());
            String s = new String(client.getData().forPath("/tmp/cyan/cyan/h"));
            log.info("strings: {}", s);
            client.setData().forPath("/tmp/cyan/cyan/h", "520".getBytes());
            log.info("strings: {}", new String(client.getData().forPath("/tmp/cyan/cyan/h")));
        }

    }
    @Test
    public void test() {
        try (CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3))) {
            client.start();
            client.create().orSetData().forPath("/newNew", new byte[0]);
            client.create().orSetData().forPath("/newNew/newNew", new byte[0]);
//            client.delete().deletingChildrenIfNeeded().forPath("/newNew");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
