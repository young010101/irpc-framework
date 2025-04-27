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