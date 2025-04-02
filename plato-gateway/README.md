# Plato Gateway Service

Gateway服务是Plato平台的核心组件之一，负责管理客户端的长连接，并将消息转发到状态服务。

## 功能特性

- 基于Netty的高性能TCP服务器
- 支持大量并发长连接
- 连接生命周期管理
- 消息转发到状态服务
- 支持从状态服务接收命令并执行
- 提供HTTP API进行连接管理和监控
- 健康检查和就绪检查

## 最近更新

- 重构了消息处理逻辑，提高了性能和稳定性
- 优化了连接管理，支持更高的并发连接数
- 添加了健康检查和就绪检查API
- 统一了配置管理，使用Spring Boot配置属性
- 修复了旧版消息处理器的兼容性问题
- 优化了gRPC服务和客户端的实现

## 架构设计

Gateway服务主要包含以下组件：

1. **TCP服务器**：基于Netty实现的TCP服务器，用于接收和处理客户端连接。
2. **连接管理器**：管理所有TCP连接，包括创建、关闭和清理空闲连接。
3. **消息处理服务**：处理接收到的消息，并将其转发到状态服务。
4. **命令处理器**：处理来自状态服务的命令，如关闭连接和推送消息。
5. **gRPC服务**：提供gRPC接口，供状态服务调用。
6. **gRPC客户端**：调用状态服务的gRPC接口。
7. **HTTP API**：提供REST API进行连接管理和监控。

## 配置说明

Gateway服务的配置在`application.yml`文件中，主要包括：

```yaml
# TCP服务器配置
tcp:
  server:
    port: 8888                # TCP服务器端口
    boss-thread-count: 1      # Boss线程数
    worker-thread-count: 4    # Worker线程数
    use-epoll: false          # 是否使用Epoll（Linux系统）
    idle-timeout-seconds: 300 # 空闲超时时间（秒）
    max-frame-length: 65536   # 最大帧长度

# gRPC服务器配置
grpc:
  server:
    port: 8901                # gRPC服务器端口
  client:
    state-service:
      address: 127.0.0.1      # 状态服务地址
      port: 8902              # 状态服务端口

# 连接管理配置
NIOConnection:
  manager:
    cleanup-interval-seconds: 60  # 清理间隔（秒）
    max-idle-time-seconds: 300    # 最大空闲时间（秒）

# 工作池配置
work:
  pool:
    core-pool-size: 10        # 核心线程数
    max-pool-size: 50         # 最大线程数
    queue-capacity: 1000      # 队列容量
    keep-alive-seconds: 60    # 线程保持活动时间（秒）
```

## API接口

### HTTP API

Gateway服务提供以下HTTP API：

#### 连接管理

- `GET /api/NIOConnections` - 获取所有连接
- `GET /api/NIOConnections/{connectionId}` - 获取指定连接
- `DELETE /api/NIOConnections/{connectionId}` - 关闭指定连接
- `DELETE /api/NIOConnections` - 关闭所有连接
- `GET /api/NIOConnections/stats` - 获取连接统计信息

#### 健康检查

- `GET /api/health` - 健康检查
- `GET /api/health/liveness` - 存活检查
- `GET /api/health/readiness` - 就绪检查

### gRPC API

Gateway服务提供以下gRPC API：

#### 接收命令

- `DelConn` - 关闭连接
- `Push` - 推送消息

#### 发送消息

- `SendMsg` - 发送消息到状态服务
- `CancelConn` - 通知状态服务连接关闭

## 使用示例

### 启动服务

```bash
java -jar plato-gateway.jar
```

### 查看连接统计

```bash
curl http://localhost:8080/api/NIOConnections/stats
```

### 查看所有连接

```bash
curl http://localhost:8080/api/NIOConnections
```

### 关闭指定连接

```bash
curl -X DELETE http://localhost:8080/api/NIOConnections/123456789
```

### 健康检查

```bash
curl http://localhost:8080/api/health
```

## 开发指南

### 构建项目

```bash
mvn clean package
```

### 运行测试

```bash
mvn test
```

### 本地运行

```bash
mvn spring-boot:run
```

## 依赖关系

Gateway服务依赖以下组件：

- Spring Boot
- Spring Cloud
- Netty
- gRPC
- Protobuf

## 性能指标

Gateway服务在标准配置下可以支持：

- 10,000+ 并发连接
- 1,000+ TPS的消息处理能力

## 故障排除

### 常见问题

1. **连接数过多**：增加系统文件描述符限制，调整TCP服务器配置。
2. **消息处理延迟**：增加工作池线程数和队列容量。
3. **内存使用过高**：调整JVM内存参数，减少每个连接的缓冲区大小。
4. **gRPC通信失败**：检查状态服务是否正常运行，检查网络连接。
5. **旧版客户端兼容性问题**：使用LegacyMessageHandler处理旧版客户端消息。

### 日志说明

Gateway服务的日志位于`logs/plato-gateway.log`，日志级别可在`application.yml`中配置。

## 贡献指南

欢迎贡献代码和提出建议，请遵循以下步骤：

1. Fork项目
2. 创建特性分支
3. 提交变更
4. 推送到分支
5. 创建Pull Request

## 许可证

本项目采用MIT许可证。 