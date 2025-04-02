# Framework模块重构设计文档

## 1. 背景与目标

当前Framework模块存在职责过重、边界不清晰的问题，需要进行重构。重构的主要目标是：

- 明确职责边界，减少模块间耦合
- 将过重的功能拆分到各个子模块中
- 提高代码质量和可维护性
- 使其更符合Java开发风格和规范
- 保持向后兼容性，减少对其他模块的影响

## 2. 问题分析

### 2.1 当前问题

通过分析当前Framework模块的代码结构，发现以下问题：

1. **职责过重**：包含了太多不同领域的功能，如消息处理、网络连接、缓存服务、配置管理等
2. **边界不清晰**：模块间的依赖关系复杂，职责划分不明确
3. **代码重复**：不同模块中存在类似的代码实现
4. **依赖混乱**：引入了过多的第三方依赖，增加了维护成本
5. **缺乏抽象**：接口设计不够清晰，难以扩展和替换实现
6. **包命名冗长**：当前包命名过长，不符合简洁原则

### 2.2 影响范围

当前Common模块被以下模块依赖：

- Gateway模块
- State模块
- IPConf模块
- Client模块

重构需要考虑对这些模块的影响，确保向后兼容性。

## 3. 重构方案

### 3.1 模块拆分

将Common模块重命名为plato-framework，并拆分为以下子模块，采用更简洁的命名方式：

1. **plato-common**：核心接口和基础类
   - 基础异常类
   - 核心接口定义
   - 基础工具类

2. **plato-message**：消息相关
   - 消息定义
   - 消息编解码
   - 消息工厂

3. **plato-network**：网络通信相关
   - 连接抽象
   - 网络工具

4. **plato-cache**：缓存相关
   - 缓存服务接口
   - Redis实现

5. **plato-discovery**：服务发现相关
   - 服务发现接口
   - Nacos实现

6. **plato-config**：配置管理相关
   - 配置服务接口
   - 配置加载器

### 3.2 模块依赖关系

```
plato-common <-- plato-message
             <-- plato-network
             <-- plato-cache
             <-- plato-discovery
             <-- plato-config
```

其他模块可以根据需要依赖特定的子模块，而不是依赖整个Common模块。

## 4. 详细设计

### 4.1 plato-core

#### 4.1.1 异常体系

```java
// 基础异常类
public class PlatoException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;
    
    // 构造函数和方法...
}

// 业务异常
public class BusinessException extends PlatoException {
    // 构造函数和方法...
}

// 系统异常
public class SystemException extends PlatoException {
    // 构造函数和方法...
}

// 网络异常
public class NetworkException extends SystemException {
    // 构造函数和方法...
}
```

#### 4.1.2 工具类

```java
// 字符串工具类
public class StringUtils {
    public static boolean isEmpty(String str) { ... }
    public static String trim(String str) { ... }
    // 其他方法...
}

// 时间工具类
public class TimeUtils {
    public static long currentTimeMillis() { ... }
    public static String formatTime(long timestamp) { ... }
    // 其他方法...
}

// 并发工具类
public class ConcurrentUtils {
    public static ThreadPoolExecutor createThreadPool(int coreSize, int maxSize, String namePrefix) { ... }
    // 其他方法...
}
```

### 4.2 plato-message

#### 4.2.1 消息接口

```java
public interface Message {
    String getId();
    MessageType getType();
    byte[] serialize();
    void deserialize(byte[] data);
}

public enum MessageType {
    TEXT,
    BINARY,
    HEARTBEAT,
    LOGIN,
    LOGOUT,
    ACK
}
```

#### 4.2.2 消息实现

```java
public abstract class AbstractMessage implements Message {
    protected String id;
    protected MessageType type;
    
    // 通用实现...
}

public class TextMessage extends AbstractMessage {
    private String content;
    
    // 实现方法...
}

public class HeartbeatMessage extends AbstractMessage {
    private long timestamp;
    
    // 实现方法...
}
```

#### 4.2.3 消息编解码

```java
public interface MessageCodec {
    byte[] encode(Message message);
    Message decode(byte[] data);
}

public class ProtobufMessageCodec implements MessageCodec {
    // 实现方法...
}
```

### 4.3 plato-network

#### 4.3.1 连接接口

```java
public interface Connection {
    long getId();
    boolean isActive();
    boolean send(Message message);
    void close();
    void addListener(ConnectionListener listener);
}

public interface ConnectionListener {
    void onMessage(Connection connection, Message message);
    void onClose(Connection connection);
    void onError(Connection connection, Throwable cause);
}
```

#### 4.3.2 连接管理

```java
public interface ConnectionManager {
    Connection getConnection(long connectionId);
    List<Connection> getAllConnections();
    void closeConnection(long connectionId);
    void closeAllConnections();
}

public class DefaultConnectionManager implements ConnectionManager {
    private final ConcurrentMap<Long, Connection> connections;
    
    // 实现方法...
}
```

### 4.4 plato-cache

#### 4.4.1 缓存接口

```java
public interface CacheService {
    <T> T get(String key, Class<T> clazz);
    void set(String key, Object value);
    void set(String key, Object value, long ttl);
    void delete(String key);
    boolean exists(String key);
}
```

#### 4.4.2 Redis实现

```java
public class RedisCacheService implements CacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 实现方法...
}
```

### 4.5 plato-discovery

#### 4.5.1 服务发现接口

```java
public interface ServiceDiscovery {
    List<ServiceInstance> getInstances(String serviceId);
    ServiceInstance getInstance(String serviceId, String instanceId);
    void registerInstance(ServiceInstance instance);
    void deregisterInstance(ServiceInstance instance);
}

public class ServiceInstance {
    private String id;
    private String serviceId;
    private String host;
    private int port;
    private Map<String, String> metadata;
    
    // 构造函数和方法...
}
```

#### 4.5.2 Nacos实现

```java
public class NacosServiceDiscovery implements ServiceDiscovery {
    private final NamingService namingService;
    
    // 实现方法...
}
```

### 4.6 plato-config

#### 4.6.1 配置接口

```java
public interface ConfigService {
    <T> T getConfig(String key, Class<T> clazz);
    <T> T getConfig(String key, Class<T> clazz, T defaultValue);
    void setConfig(String key, Object value);
    void removeConfig(String key);
    void addListener(String key, ConfigChangeListener listener);
}

public interface ConfigChangeListener {
    void onChange(String key, Object oldValue, Object newValue);
}
```

#### 4.6.2 Nacos实现

```java
public class NacosConfigService implements ConfigService {
    private final ConfigService configService;
    
    // 实现方法...
}
```

## 5. 实施步骤

### 5.1 准备阶段

1. 创建新的子模块结构
2. 设计核心接口和基础类
3. 编写单元测试

### 5.2 迁移阶段

1. 将现有代码按功能迁移到对应子模块
2. 调整依赖关系
3. 确保单元测试通过

### 5.3 过渡阶段

1. 创建适配层，保持向后兼容性
2. 逐步调整其他模块的依赖
3. 进行集成测试

### 5.4 完成阶段

1. 移除适配层
2. 完善文档
3. 进行性能测试和优化

## 6. 兼容性策略

为了确保重构过程中不影响其他模块的正常运行，采取以下兼容性策略：

1. **保留原接口**：在过渡阶段保留原有接口，内部实现调用新接口
2. **适配器模式**：为新旧接口之间提供适配器
3. **分阶段迁移**：按照依赖关系，分阶段迁移各个模块
4. **版本共存**：允许新旧版本共存，逐步切换

## 7. 测试策略

### 7.1 单元测试

- 为每个子模块编写单元测试
- 测试覆盖率达到80%以上
- 使用Mock框架模拟依赖组件

### 7.2 集成测试

- 测试子模块间的集成
- 测试与其他模块的集成
- 测试完整的业务流程

### 7.3 性能测试

- 测试重构前后的性能对比
- 识别性能瓶颈并优化

## 8. 风险与应对措施

### 8.1 功能遗漏风险

- **风险**：重构过程中可能遗漏某些功能
- **应对**：详细梳理现有功能，编写完善的测试用例

### 8.2 兼容性风险

- **风险**：重构后的接口与原接口不兼容
- **应对**：采用适配器模式，保留原接口

### 8.3 性能风险

- **风险**：重构后性能下降
- **应对**：进行性能测试，确保性能不降低

### 8.4 进度风险

- **风险**：重构工作量大，影响开发进度
- **应对**：分阶段实施，优先重构核心功能

## 9. 时间规划

### 9.1 第一阶段（1周）

- 设计核心接口和基础类
- 创建plato-core模块
- 编写单元测试

### 9.2 第二阶段（1周）

- 创建plato-message和plato-network模块
- 迁移相关代码
- 调整依赖关系

### 9.3 第三阶段（1周）

- 创建plato-cache、plato-discovery和plato-config模块
- 迁移相关代码
- 进行集成测试

### 9.4 第四阶段（1周）

- 调整其他模块的依赖
- 完善文档
- 性能测试和优化

## 10. 结论

通过本次重构，Framework模块将被拆分为多个职责明确的子模块，减少模块间耦合，提高代码质量和可维护性。重构后的模块命名更加简洁，职责更加清晰，更符合Java开发风格和规范，为整个Plato系统提供可靠的基础支持。