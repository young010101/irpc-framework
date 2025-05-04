package zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.idea.irpc.framework.interfaces.DataService;

@Slf4j
public class ServerZookeeper {
    public static void main(String[] args) {
        URL url = new URL();
        url.setApplicationName("test-application");
        url.setServiceName(DataService.class.getName());
        url.addParameter("host", "localhost");
        url.addParameter("port", "2181");
        log.info("{}", url);

        ZookeeperRegister zookeeperRegister = new ZookeeperRegister("localhost:2181");
        // 创建temporary Node, 保证服务下线, zookeeper的节点就会消失, 但是不会马上消失
        log.info("{}", zookeeperRegister.getProviderAddresses(url.getServiceName()));
        zookeeperRegister.register(url);
        log.info("{}", zookeeperRegister.getProviderAddresses(url.getServiceName()));

        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
