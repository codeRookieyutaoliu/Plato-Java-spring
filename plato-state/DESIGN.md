# State模块设计文档

## 1. 概述

### 1.1 背景

State模块是Plato系统的核心组件之一，负责管理用户状态、连接状态和消息状态，提供状态持久化和查询服务。目前该模块尚未开始开发，需要从零开始实现。

### 1.2 目标

- 设计并实现高性能的状态管理系统
- 提供可靠的状态持久化机制
- 支持分布式部署和水平扩展
- 与Gateway模块无缝集成
- 提供灵活的状态查询和更新接口

## 2. 架构设计

### 2.1 整体架构

采用领域驱动设计(DDD)思想，将State模块分为以下几层：

- **领域层(Domain)**: 包含核心业务逻辑和实体
- **应用层(Application)**: 协调领域对象完成用户用例
- **基础设施层(Infrastructure)**: 提供技术实现
- **接口层(Interfaces)**: 对外提供服务接口

### 2.2 模块结构

```
plato-state
├── domain                  # 领域层
│   ├── model               # 领域模型
│   │   ├── UserState.java  # 用户状态
│   │   ├── ConnState.java  # 连接状态
│   │   ├── MessageState.java # 消息状态
│   │   └── StateMachine.java # 状态机
│   ├── repository          # 仓储接口
│   │   ├── UserStateRepository.java
│   │   ├── ConnStateRepository.java
│   │   └── MessageStateRepository.java
│   └── service             # 领域服务
│       └── StateDomainService.java
├── application             # 应用层
│   ├── dto                 # 数据传输对象
│   │   ├── UserStateDTO.java
│   │   ├── ConnStateDTO.java
│   │   └── MessageStateDTO.java
│   ├── service             # 应用服务
│   │   └── StateService.java
│   └── command             # 命令处理
│       ├── CommandProcessor.java
│       └── CommandContext.java
├── infrastructure          # 基础设施层
│   ├── config              # 配置类
│   │   ├── RedisConfig.java
│   │   └── JpaConfig.java
│   ├── persistence         # 持久化实现
│   │   ├── RedisUserStateRepository.java
│   │   ├── JpaUserStateRepository.java
│   │   └── CacheManager.java
│   ├── timer               # 定时任务
│   │   ├── TimerManager.java
│   │   └── TimingWheel.java
│   └── messaging           # 消息通信
│       ├── RocketMQProducer.java
│       └── RocketMQConsumer.java
└── interfaces              # 接口层
    ├── rest                # REST API
    │   └── StateController.java
    └── grpc                # gRPC服务
        └── StateGrpcService.java
```

## 3. 核心组件设计

### 3.1 状态模型

#### 3.1.1 用户状态(UserState)

```java
public class UserState {
    private String userId;           // 用户ID
    private UserStatus status;       // 用户状态(在线/离线/忙碌等)
    private List<Long> connectionIds; // 关联的连接ID列表
    private Map<String, Object> attributes; // 用户属性
    private long lastActiveTime;    // 最后活跃时间
    
    // 方法...
}
```

#### 3.1.2 连接状态(ConnState)

```java
public class ConnState {
    private long connectionId;       // 连接ID
    private String userId;          // 用户ID
    private String gatewayId;       // 网关ID
    private ConnectionStatus status; // 连接状态
    private InetSocketAddress remoteAddress; // 远程地址
    private long createTime;        // 创建时间
    private long lastActiveTime;    // 最后活跃时间
    
    // 方法...
}
```

#### 3.1.3 消息状态(MessageState)

```java
public class MessageState {
    private String messageId;        // 消息ID
    private String senderId;        // 发送者ID
    private String receiverId;      // 接收者ID
    private MessageType type;       // 消息类型
    private MessageStatus status;   // 消息状态(发送中/已送达/已读等)
    private long createTime;        // 创建时间
    private long updateTime;        // 更新时间
    
    // 方法...
}
```

### 3.2 状态机(StateMachine)

```java
public class StateMachine<T, S> {
    private final Map<S, Map<Event, S>> transitions; // 状态转换表
    
    public S getCurrentState(T entity) { ... }
    public void transition(T entity, Event event) { ... }
    public boolean canTransition(T entity, Event event) { ... }
}
```

### 3.3 命令处理

```java
public interface CommandProcessor {
    void process(Command command, CommandContext context);
}

public class CommandContext {
    private final String userId;
    private final long connectionId;
    private final Map<String, Object> attributes;
    
    // 方法...
}
```

### 3.4 缓存管理

```java
public class CacheManager {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheConfig cacheConfig;
    
    public <T> T get(String key, Class<T> clazz) { ... }
    public void set(String key, Object value, long ttl) { ... }
    public void delete(String key) { ... }
    public boolean exists(String key) { ... }
}
```

### 3.5 定时任务管理

```java
public class TimerManager {
    private final TimingWheel timingWheel;
    
    public void schedule(Runnable task, long delayMs) { ... }
    public void scheduleAtFixedRate(Runnable task, long initialDelayMs, long periodMs) { ... }
    public void cancel(Runnable task) { ... }
}
```

## 4. 关键流程

### 4.1 用户上线流程

1. Gateway模块通知State模块用户连接建立
2. State模块创建或更新ConnState
3. 更新UserState状态为在线
4. 通知相关服务用户状态变更

### 4.2 消息处理流程

1. Gateway模块转发消息到State模块
2. State模块创建MessageState并设置状态为发送中
3. 处理消息并更新状态
4. 如果接收者在线，通过Gateway模块推送消息
5. 如果接收者离线，存储消息等待用户上线

### 4.3 用户下线流程

1. Gateway模块通知State模块连接断开
2. State模块更新ConnState状态
3. 检查用户是否还有其他连接
4. 如果没有其他连接，更新UserState状态为离线
5. 通知相关服务用户状态变更

## 5. 存储设计

### 5.1 Redis存储

用于存储实时状态数据：

- 用户状态: `user:{userId}:state`
- 连接状态: `conn:{connectionId}:state`
- 用户连接映射: `user:{userId}:connections`
- 网关连接映射: `gateway:{gatewayId}:connections`

### 5.2 MySQL存储

用于持久化历史数据：

- 用户状态历史
- 连接记录
- 消息历史

### 5.3 缓存策略

- 采用多级缓存策略
- 热点数据保持在内存中
- 冷数据存储在Redis中
- 历史数据存储在MySQL中

## 6. 与其他模块的集成

### 6.1 与Gateway模块集成

- 通过gRPC接口接收连接状态变更通知
- 通过gRPC接口向Gateway发送命令

### 6.2 与IPConf模块集成

- 获取Gateway实例信息
- 根据负载均衡策略选择Gateway

## 7. 性能优化

### 7.1 缓存优化

- 使用本地缓存减少Redis访问
- 批量操作减少网络开销
- 使用Pipeline提高Redis操作效率

### 7.2 并发处理优化

- 使用线程池处理并发请求
- 使用异步处理提高吞吐量
- 使用读写分离提高查询性能

### 7.3 存储优化

- 合理设计数据分片策略
- 定期清理过期数据
- 使用时间序列数据库存储历史数据

## 8. 测试策略

### 8.1 单元测试

- 对各组件进行单元测试，覆盖率达到80%以上
- 使用Mock框架模拟依赖组件

### 8.2 集成测试

- 测试与其他模块的集成
- 测试完整的状态管理流程

### 8.3 性能测试

- 测试高并发场景下的性能
- 测试大量数据下的查询性能
- 测试长时间运行的稳定性

## 9. 实施计划

### 9.1 第一阶段：基础架构（1周）

- 搭建项目结构
- 实现基本的状态模型
- 实现Redis和MySQL存储

### 9.2 第二阶段：核心功能（1周）

- 实现状态机
- 实现命令处理
- 实现与Gateway的集成

### 9.3 第三阶段：优化与测试（1周）

- 性能优化
- 完善测试
- 文档编写

## 10. 风险与应对措施

### 10.1 性能风险

- **风险**：高并发下状态更新性能不足
- **应对**：使用本地缓存和批量操作，减少Redis访问

### 10.2 数据一致性风险

- **风险**：分布式环境下数据一致性难以保证
- **应对**：使用分布式锁和事务确保一致性

### 10.3 可用性风险

- **风险**：Redis或MySQL故障导致服务不可用
- **应对**：实现多级缓存和故障转移机制

## 11. 结论

State模块作为Plato系统的核心组件，将提供高性能、可靠的状态管理服务。通过采用领域驱动设计和分层架构，State模块将具有良好的可维护性和可扩展性。同时，通过优化存储和缓存策略，State模块将能够支持高并发场景，为整个Plato系统提供稳定的状态管理基础。