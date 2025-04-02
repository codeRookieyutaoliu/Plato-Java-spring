# Gateway模块重构设计文档

## 1. 背景与目标

当前Gateway模块存在代码混乱、结构不清晰的问题，需要完全重写。重构的主要目标是：

- 采用领域驱动设计(DDD)思想，明确各层职责
- 提高代码质量和可维护性
- 优化性能，支持高并发连接
- 增强与其他模块的集成能力
- 符合Java开发规范和最佳实践

## 2. 架构设计

### 2.1 整体架构

采用经典的四层架构：

- **领域层(Domain)**: 核心业务逻辑和实体
- **应用层(Application)**: 协调领域对象完成用户用例
- **基础设施层(Infrastructure)**: 提供技术实现
- **接口层(Interfaces)**: 对外提供服务接口

### 2.2 模块结构

```
plato-gateway
├── domain                  # 领域层
│   ├── model               # 领域模型
│   │   ├── Connection.java # 连接实体
│   │   ├── Message.java    # 消息实体
│   │   └── RouteRule.java  # 路由规则
│   ├── repository          # 仓储接口
│   │   └── ConnectionRepository.java
│   └── service             # 领域服务
│       └── ConnectionDomainService.java
├── application             # 应用层
│   ├── dto                 # 数据传输对象
│   │   ├── ConnectionDTO.java
│   │   └── MessageDTO.java
│   └── service             # 应用服务
│       ├── ConnectionService.java
│       └── MessageService.java
├── infrastructure          # 基础设施层
│   ├── config              # 配置类
│   │   └── GatewayConfig.java
│   ├── connection          # 连接实现
│   │   ├── NettyConnection.java
│   │   └── ConnectionTable.java
│   ├── handler             # 消息处理器
│   │   ├── NettyMessageDecoder.java
│   │   └── NettyMessageEncoder.java
│   ├── metrics             # 性能监控
│   │   └── MetricsCollector.java
│   ├── persistence         # 持久化实现
│   │   └── InMemoryConnectionRepository.java
│   └── server              # 服务器实现
│       └── NettyServer.java
└── interfaces              # 接口层
    ├── rest                # REST API
    │   ├── ConnectionController.java
    │   └── HealthController.java
    └── grpc                # gRPC服务
        └── GatewayGrpcService.java
```

## 3. 核心组件设计

### 3.1 连接管理

#### 3.1.1 连接实体

```java
public class Connection {
    private final long id;
    private final Channel channel;
    private final InetSocketAddress remoteAddress;
    private ConnectionStatus status;
    private long lastActiveTime;
    private Map<String, Object> attributes;
    
    // 方法...
}
```

#### 3.1.2 连接表

```java
public class ConnectionTable {
    private final ConcurrentMap<Long, Connection> connections;
    private final AtomicLong idGenerator;
    
    public Connection register(Channel channel) { ... }
    public void unregister(long connectionId) { ... }
    public Connection get(long connectionId) { ... }
    public List<Connection> getAll() { ... }
}
```

### 3.2 消息处理

#### 3.2.1 消息编解码

```java
public class NettyMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 消息解码逻辑
    }
}

public class NettyMessageEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        // 消息编码逻辑
    }
}
```

#### 3.2.2 消息分发

```java
public class MessageDispatcher {
    private final Map<MessageType, MessageHandler> handlers;
    
    public void dispatch(Connection connection, Message message) {
        MessageHandler handler = handlers.get(message.getType());
        if (handler != null) {
            handler.handle(connection, message);
        }
    }
}
```

### 3.3 Netty服务器

```java
public class NettyServer implements TcpServer {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final ConnectionTable connectionTable;
    private final MessageDispatcher messageDispatcher;
    
    @Override
    public void start(int port) {
        // 启动Netty服务器
    }
    
    @Override
    public void stop() {
        // 停止Netty服务器
    }
}
```

### 3.4 性能监控

```java
public class MetricsCollector {
    private final Counter connectionsCounter;
    private final Counter messagesCounter;
    private final Timer messageProcessingTimer;
    
    public void recordConnection(boolean isConnect) { ... }
    public void recordMessage(MessageType type) { ... }
    public Timer.Context startMessageProcessing() { ... }
}
```

## 4. 关键流程

### 4.1 连接建立流程

1. 客户端发起连接请求
2. Netty服务器接受连接
3. 创建Connection对象并分配ID
4. 将Connection注册到ConnectionTable
5. 触发连接建立事件

### 4.2 消息处理流程

1. 接收到客户端消息
2. 解码消息为Message对象
3. 查找对应的Connection
4. 通过MessageDispatcher分发到对应的MessageHandler
5. 处理消息并返回响应

### 4.3 连接关闭流程

1. 检测到连接断开
2. 从ConnectionTable中移除Connection
3. 触发连接关闭事件
4. 清理相关资源

## 5. 与其他模块的集成

### 5.1 与State模块集成

- 通过gRPC接口向State模块报告连接状态变化
- 从State模块获取用户状态信息

### 5.2 与IPConf模块集成

- 向IPConf模块注册Gateway实例信息
- 从IPConf模块获取负载均衡策略

## 6. 性能优化

### 6.1 连接管理优化

- 使用高效的数据结构存储连接信息
- 定期清理空闲连接
- 使用连接池管理连接资源

### 6.2 消息处理优化

- 使用工作线程池处理消息
- 批量处理消息
- 使用零拷贝技术减少内存使用

### 6.3 网络IO优化

- 使用Netty的NIO模型
- 在Linux系统上使用Epoll优化
- 在Windows系统上使用IOCP优化

## 7. 测试策略

### 7.1 单元测试

- 对各组件进行单元测试，覆盖率达到80%以上
- 使用Mock框架模拟依赖组件

### 7.2 集成测试

- 测试与其他模块的集成
- 测试完整的消息处理流程

### 7.3 性能测试

- 测试高并发连接场景
- 测试大量消息处理场景
- 测试长时间运行稳定性

## 8. 实施计划

### 8.1 第一阶段：基础架构（1周）

- 搭建项目结构
- 实现基本的Netty服务器
- 实现连接管理

### 8.2 第二阶段：核心功能（1周）

- 实现消息编解码
- 实现消息分发机制
- 实现基本的消息处理器

### 8.3 第三阶段：集成与优化（1周）

- 与其他模块集成
- 性能优化
- 完善测试

## 9. 风险与应对措施

### 9.1 性能风险

- **风险**：高并发下性能不足
- **应对**：早期进行性能测试，识别瓶颈并优化

### 9.2 兼容性风险

- **风险**：与现有系统集成困难
- **应对**：保留关键接口，提供适配层

### 9.3 技术风险

- **风险**：Netty使用不当导致内存泄漏
- **应对**：遵循Netty最佳实践，加强代码审查

## 10. 结论

通过本次重构，Gateway模块将采用清晰的分层架构，提高代码质量和可维护性，同时优化性能以支持高并发场景。重构后的Gateway模块将更符合Java开发规范和最佳实践，为整个Plato系统提供可靠的通信基础。