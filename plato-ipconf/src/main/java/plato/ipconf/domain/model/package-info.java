/**
 * IP配置服务的领域模型包
 * <p>
 * 本包包含IP配置服务的核心领域模型类，用于表示和管理IP配置相关的业务实体。
 * 主要包含以下模型：
 * <ul>
 *   <li>{@link plato.ipconf.domain.model.ClientInfo} - 客户端信息，包含客户端的IP、位置、类型等信息</li>
 *   <li>{@link plato.ipconf.domain.model.GeoLocation} - 地理位置信息，包含经纬度和区域信息</li>
 *   <li>{@link plato.ipconf.domain.model.GatewayEndpoint} - 网关端点信息，表示网关服务实例的连接信息</li>
 *   <li>{@link plato.ipconf.domain.model.EndpointScore} - 端点评分信息，用于网关节点的负载均衡和选择</li>
 * </ul>
 * <p>
 * 这些模型类与Go版本中的对应结构体保持一致，确保了系统的跨语言兼容性。
 */
package plato.ipconf.domain.model;