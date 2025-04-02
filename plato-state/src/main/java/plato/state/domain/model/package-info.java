/**
 * 状态服务的领域模型包
 * <p>
 * 本包包含状态服务的核心领域模型类，用于表示和管理用户状态和连接信息。
 * 主要包含以下模型：
 * <ul>
 *   <li>{@link plato.state.domain.model.UserState} - 用户状态信息，包含用户的在线状态和连接信息</li>
 *   <li>{@link plato.state.domain.model.ConnectionInfo} - 连接信息，表示用户的一个连接</li>
 *   <li>{@link plato.state.domain.model.Message} - 消息实体，表示用户之间的消息</li>
 * </ul>
 * <p>
 * 这些模型类与Go版本中的对应结构体保持一致，确保了系统的跨语言兼容性。
 */
package plato.state.domain.model;