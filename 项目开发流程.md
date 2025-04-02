# 更新日志

本文件记录项目所有版本的重要变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased] - 开发中

### 新增
- 添加Git与CI/CD规范
- 添加代码风格检查配置
- 添加Docker构建配置
- 添加GitHub Actions工作流程

## [0.1.0] - 项目初始化

### 新增
- 项目基础架构搭建
- 添加plato-common模块
- 添加plato-client模块
- 添加plato-gateway模块
- 添加plato-ipconf模块
- 添加plato-state模块 


### 重构common模块

```
plato-framework/
│
├── plato-common/                   # 通用基础设施
│   ├── src/main/java/plato/common/
│   │   ├── exception/              # 异常定义
│   │   ├── utils/                  # 工具类
│   │   ├── constants/              # 常量定义
│   │   └── model/                  # 基础模型
│   └── pom.xml
│
├── plato-message/                  # 消息领域
│   ├── src/main/java/plato/message/
│   │   ├── domain/                 # 消息领域模型
│   │   │   ├── entity/             # 消息实体
│   │   │   ├── valueobject/        # 消息值对象
│   │   │   └── service/            # 领域服务
│   │   ├── application/            # 应用层
│   │   │   ├── service/            # 应用服务
│   │   │   └── dto/                # 数据传输对象
│   │   ├── infrastructure/         # 基础设施层
│   │   │   ├── repository/         # 仓储实现
│   │   │   ├── codec/              # 编解码实现
│   │   │   └── proto/              # Protobuf定义
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-network/                  # 网络领域
│   ├── src/main/java/plato/network/
│   │   ├── domain/                 # 网络领域模型
│   │   │   ├── entity/             # 连接实体
│   │   │   ├── valueobject/        # 网络值对象
│   │   │   └── service/            # 领域服务
│   │   ├── application/            # 应用层
│   │   ├── infrastructure/         # 基础设施层
│   │   │   ├── netty/              # Netty实现
│   │   │   └── tcp/                # TCP处理
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-cache/                    # 缓存领域
│   ├── src/main/java/plato/cache/
│   │   ├── domain/                 # 缓存领域模型
│   │   ├── application/            # 应用层
│   │   ├── infrastructure/         # 基础设施层
│   │   │   ├── redis/              # Redis实现
│   │   │   └── local/              # 本地缓存实现
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-discovery/                # 服务发现领域
│   ├── src/main/java/plato/discovery/
│   │   ├── domain/                 # 领域模型
│   │   │   ├── entity/             # 服务实体
│   │   │   └── service/            # 领域服务
│   │   ├── application/            # 应用层
│   │   ├── infrastructure/         # 基础设施层
│   │   │   └── nacos/              # Nacos实现
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-config/                   # 配置领域
│   ├── src/main/java/plato/config/
│   │   ├── domain/                 # 领域模型
│   │   ├── application/            # 应用层
│   │   ├── infrastructure/         # 基础设施层
│   │   │   └── yaml/               # YAML配置支持
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-bus/                      # 事件总线领域
│   ├── src/main/java/plato/bus/
│   │   ├── domain/                 # 领域模型
│   │   │   ├── event/              # 事件定义
│   │   │   └── service/            # 领域服务
│   │   ├── application/            # 应用层
│   │   ├── infrastructure/         # 基础设施层
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-bizflow/                  # 业务流程领域
│   ├── src/main/java/plato/bizflow/
│   │   ├── domain/                 # 领域模型
│   │   │   ├── entity/             # 流程实体
│   │   │   ├── valueobject/        # 流程值对象
│   │   │   └── service/            # 领域服务
│   │   ├── application/            # 应用层
│   │   │   ├── service/            # 引擎服务
│   │   │   └── dto/                # 数据传输对象
│   │   ├── infrastructure/         # 基础设施层
│   │   │   └── graph/              # DAG图实现
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-router/                   # 路由领域
│   ├── src/main/java/plato/router/
│   │   ├── domain/                 # 领域模型
│   │   ├── application/            # 应用层
│   │   ├── infrastructure/         # 基础设施层
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-rpc/                      # RPC领域
│   ├── src/main/java/plato/rpc/
│   │   ├── domain/                 # 领域模型
│   │   ├── application/            # 应用层
│   │   ├── infrastructure/         # 基础设施层
│   │   │   ├── server/             # 服务端实现
│   │   │   ├── client/             # 客户端实现
│   │   │   ├── interceptor/        # 拦截器
│   │   │   └── lb/                 # 负载均衡
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-timingwheel/              # 时间轮领域
│   ├── src/main/java/plato/timingwheel/
│   │   ├── domain/                 # 领域模型
│   │   │   ├── entity/             # 时间轮实体
│   │   │   ├── valueobject/        # 值对象
│   │   │   └── service/            # 领域服务
│   │   ├── application/            # 应用层
│   │   ├── infrastructure/         # 基础设施层
│   │   │   ├── wheel/              # 时间轮实现
│   │   │   ├── bucket/             # 任务桶实现
│   │   │   └── queue/              # 延迟队列实现
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
├── plato-logger/                   # 日志领域
│   ├── src/main/java/plato/logger/
│   │   ├── domain/                 # 领域模型
│   │   ├── application/            # 应用层
│   │   │   └── service/            # 日志服务
│   │   ├── infrastructure/         # 基础设施层
│   │   │   ├── slf4j/              # SLF4J实现
│   │   │   └── trace/              # 链路跟踪实现
│   │   └── interfaces/             # 接口层
│   └── pom.xml
│
└── pom.xml                         # 父pom配置

```
