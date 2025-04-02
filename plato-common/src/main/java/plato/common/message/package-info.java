/**
 * 消息处理模块
 * <p>
 * 本包定义了Plato系统中的所有消息类型及其处理逻辑。
 * 消息系统采用了双层设计模式：
 * <ol>
 *   <li><b>传输协议层</b>：基于Protocol Buffers(Protobuf)定义的消息格式，用于网络传输和跨语言通信</li>
 *   <li><b>应用模型层</b>：基于Java面向对象设计的消息类型体系，用于提供类型安全和开发便利性</li>
 * </ol>
 * </p>
 * 
 * <h2>传输协议层(Protobuf)</h2>
 * <p>
 * 在`src/main/proto/message.proto`中定义了消息的协议结构，主要特点：
 * <ul>
 *   <li>使用Protocol Buffers作为IDL(接口定义语言)描述消息格式</li>
 *   <li>定义了CmdType枚举、MsgCmd基本消息容器等结构</li>
 *   <li>提供高效的二进制序列化和反序列化</li>
 *   <li>确保与Go等其他语言实现的兼容性</li>
 * </ul>
 * </p>
 * 
 * <h2>应用模型层(Java)</h2>
 * <p>
 * 在Java应用内部，采用面向对象的消息模型，主要特点：
 * <ul>
 *   <li>定义统一的消息接口和基础抽象类</li>
 *   <li>提供类型安全的消息实现类</li>
 *   <li>利用Java的继承和多态特性</li>
 *   <li>通过工厂模式简化消息创建</li>
 *   <li>支持验证、克隆等高级功能</li>
 * </ul>
 * </p>
 * 
 * <h2>两层设计的桥接</h2>
 * <p>
 * 两层设计通过转换器(MessageConverter)进行桥接：
 * <ul>
 *   <li><b>发送消息流程</b>：Java对象 → Protobuf对象 → 二进制数据</li>
 *   <li><b>接收消息流程</b>：二进制数据 → Protobuf对象 → Java对象</li>
 * </ul>
 * 这种设计模式结合了两种技术的优点：
 * <ul>
 *   <li>Protobuf的高效序列化和跨语言兼容性</li>
 *   <li>Java的面向对象设计和类型安全</li>
 * </ul>
 * </p>
 * 
 * <h2>主要功能</h2>
 * <p>
 * 主要功能包括：
 * <ul>
 *   <li>消息接口和抽象基类：定义统一的消息接口和基本实现</li>
 *   <li>消息类型定义：提供所有支持的消息类型枚举</li>
 *   <li>具体消息实现：文本消息、心跳消息、登录消息等各种具体类型</li>
 *   <li>消息工厂：提供创建各种类型消息的统一接口</li>
 *   <li>消息序列化：处理消息的序列化和反序列化</li>
 * </ul>
 * </p>
 * 
 * <h2>主要类和接口</h2>
 * <p>
 * <ul>
 *   <li>{@link plato.common.message.Message}：消息接口，定义所有消息的通用行为</li>
 *   <li>{@link plato.common.message.MessageType}：消息类型枚举，定义所有支持的消息类型</li>
 *   <li>{@link plato.common.message.AbstractMessage}：抽象消息基类，提供通用实现</li>
 *   <li>{@link plato.common.message.TextMessage}：文本消息实现</li>
 *   <li>{@link plato.common.message.HeartbeatMessage}：心跳消息实现</li>
 *   <li>{@link plato.common.message.LoginMessage}：登录消息实现</li>
 *   <li>{@link plato.common.message.AckMessage}：确认消息实现</li>
 *   <li>{@link plato.common.message.ReconnectMessage}：重连消息实现</li>
 *   <li>{@link plato.common.message.MessageFactory}：消息工厂，创建消息实例</li>
 * </ul>
 * </p>
 * 
 * <h2>设计原则</h2>
 * <p>
 * <ul>
 *   <li><b>兼容性</b>：保持与原有Protobuf协议和Go实现的兼容</li>
 *   <li><b>类型安全</b>：利用Java强类型特性，提供类型安全的消息处理</li>
 *   <li><b>灵活扩展</b>：可以方便地添加新的消息类型和行为</li>
 *   <li><b>高内聚低耦合</b>：每种消息类型职责单一，减少相互依赖</li>
 *   <li><b>工厂模式</b>：使用工厂统一创建消息，简化客户端代码</li>
 * </ul>
 * </p>
 */
package plato.common.message; 