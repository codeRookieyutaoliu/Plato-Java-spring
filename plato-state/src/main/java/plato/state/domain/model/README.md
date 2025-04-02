# 消息状态机实现说明

## 概述

消息状态机（MessageStateMachine）是Plato IM系统中保证消息可靠性的核心组件，负责处理上行消息和下行消息的可靠性保证。消息可靠性包括四个方面：不漏、不重、有序、及时。

## 技术约束

- 高可用：提供5个9以上的SLA
- 低延迟：毫秒级发送消息
- 高吞吐：应对极端群聊场景

## 消息可靠性模型

### 上行消息可靠性

上行消息可靠性通过以下机制保证：

1. 客户端生成并维护client_id，每发送一条消息递增
2. 登录时将max_client_id记录到state server
3. 断线重连时max_client_id可复用
4. state server通过比较client_id保证消息不重复

### 下行消息可靠性

下行消息可靠性通过以下机制保证：

1. 服务端分配msg_id
2. 客户端通过msg_id排序
3. 客户端检测到消息缺失时主动拉取
4. 服务端消息重传机制

## 核心类说明

### MessageStateMachine

消息状态机，负责处理消息的可靠性，包括上行消息和下行消息的可靠性保证。

主要功能：
- 生成消息ID
- 记录和管理客户端ID
- 比较并递增客户端ID，确保消息不重复
- 处理上行消息和下行消息
- 设置消息重传定时器
- 确认消息

### MessageConfig

消息配置类，配置消息状态机相关参数。

主要配置：
- 消息重传间隔
- 消息最大重传次数
- 消息过期时间

## 消息处理流程

### 上行消息处理流程

1. 客户端生成client_id并发送消息
2. state server接收消息并比较client_id
3. 如果client_id有效，则处理消息并更新记录的client_id
4. 如果client_id无效（重复或过期），则丢弃消息

### 下行消息处理流程

1. 服务端生成msg_id并发送消息
2. 设置消息重传定时器
3. 客户端接收消息并发送确认
4. 服务端接收确认并取消重传
5. 如果未收到确认，则重传消息，直到达到最大重传次数

## 配置说明

在application.properties中配置：

```properties
# 消息状态机配置
state.message.retry-interval=3000    # 消息重传间隔（毫秒）
state.message.max-retry-times=3      # 消息最大重传次数
state.message.expire-time=604800     # 消息过期时间（秒）
```

## 与Go版本的对应关系

| Java类/方法                                | Go对应                           |
|-------------------------------------------|----------------------------------|
| MessageStateMachine                       | msgStateMachine结构体             |
| generateMsgId()                          | generateMsgId函数                |
| recordClientId()                         | recordClientId函数               |
| getClientId()                            | getClientId函数                  |
| compareAndIncrClientId()                 | compareAndIncrClientId函数       |
| deleteConnClientId()                     | deleteConnClientId函数           |
| handleUpMsg()                            | handleUpMsg函数                  |
| handleDownMsg()                          | handleDownMsg函数                |
| setMsgRetryTimer()                       | setMsgRetryTimer函数             |
| isMessageAcknowledged()                  | isMessageAcked函数               |
| ackMessage()                             | ackMessage函数                   |
``` 