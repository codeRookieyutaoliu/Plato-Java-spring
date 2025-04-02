/**
 * 网络通信模块
 * <p>
 * 本模块定义了Plato系统中网络通信的核心接口和基础实现。
 * 主要功能包括：
 * <ul>
 *   <li>连接管理：创建、维护和关闭网络连接</li>
 *   <li>消息传递：发送和接收网络消息</li>
 *   <li>事件处理：监听并处理连接和消息事件</li>
 * </ul>
 * </p>
 * <p>
 * 核心接口和类：
 * <ul>
 *   <li>{@link plato.common.net.Connection}：网络连接接口</li>
 *   <li>{@link plato.common.net.ConnectionManager}：连接管理器接口</li>
 *   <li>{@link plato.common.net.ConnectionFactory}：连接工厂接口</li>
 *   <li>{@link plato.common.net.ConnectionListener}：连接事件监听器接口</li>
 *   <li>{@link plato.common.net.ConnectionConfig}：连接配置类</li>
 *   <li>{@link plato.common.net.AbstractConnection}：抽象连接基类</li>
 *   <li>{@link plato.common.net.DefaultConnectionManager}：默认连接管理器实现</li>
 * </ul>
 * </p>
 * <p>
 * 设计思想：
 * <ul>
 *   <li>接口抽象：通过接口定义核心功能，提供清晰的扩展点</li>
 *   <li>依赖倒置：上层模块依赖接口而非实现，降低耦合</li>
 *   <li>单一职责：每个类和接口只负责一项职责</li>
 *   <li>开闭原则：对扩展开放，对修改关闭</li>
 * </ul>
 * </p>
 */
package plato.common.net; 