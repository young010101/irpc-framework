## 代理层设计

```java
public static void main(String[] args) {
    Server server = new Server(localhost, port);
    server.doConnect();
    Object sendResponse = server.doRef("sendMail", "这是一条消息");
    System.out.println(sendResponse);
}
```

```mermaid
flowchart LR
    Request --> Proxy
    Proxy --> Provider
    Request[Request]
    Proxy[Proxy]
    Provider[Provider]
    subgraph Client
        Request
        Proxy
    end

```

## 路由层设计

```mermaid
flowchart LR
    Request --> Proxy
    Proxy --> Router
    Router --> Provider01
    Router --> Provider02
    Router --> Provider03

    Request([Request])
    Proxy([Proxy])
    Router([Router])
    Provider01([Provider01])
    Provider02([Provider02])
    Provider03([Provider03])

    subgraph Client
        Request
        Proxy
        Router
    end

    style Request fill:#f9c74f,stroke:#333,stroke-width:2px
    style Proxy fill:#f9844a,stroke:#333,stroke-width:2px
    style Router fill:#90be6d,stroke:#333,stroke-width:2px
    style Provider01 fill:#43aa8b,stroke:#333,stroke-width:2px
    style Provider02 fill:#43aa8b,stroke:#333,stroke-width:2px
    style Provider03 fill:#43aa8b,stroke:#333,stroke-width:2px

```

## 代理层实现

开启客户端开启新的线程发送数据包给服务器, 解耦

代理对象会检查是否得到 RpcInvocation 对象, 并返回 RpcInvocation 的 response object.

