package zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@Slf4j
public class TestWatch {
    public static void main(String[] args) {
        try (CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3))) {
            client.start();
            tryWatch(client);
            // 防止 main 线程退出得太早
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void tryWatch(CuratorFramework client) {
        try {
            client.getData().usingWatcher((Watcher) event -> {
                log.info("线程: {}", Thread.currentThread().getName());
                log.info("event: {}", event);
                String eventPath = event.getPath();
                log.info("event.getPath(): {}", eventPath);
                try {
                    log.info("new data: {}", new String(client.getData().forPath(eventPath)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                tryWatch(client);
            }).forPath("/cyang");
        } catch (Exception e) {
            log.error("<UNK>", e);
        }
    }

    @Test
    public void test() throws Exception {
        try (CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3))) {
            client.start();
            client.setData().forPath("/cyang", LocalDateTime.now().toString().getBytes());
        }
    }
}
