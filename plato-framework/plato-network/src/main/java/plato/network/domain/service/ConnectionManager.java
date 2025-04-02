package plato.network.domain.service;

import plato.network.domain.entity.Connection;

import java.util.List;
import java.util.Optional;

/**
 * 连接管理器接口
 * <p>
 * 定义连接管理的核心功能，包括连接的注册、获取、移除等
 * </p>
 * 
 * 对应Go项目中的连接管理功能
 */
public interface ConnectionManager {
    
    /**
     * 注册连接
     *
     * @param connection 要注册的连接
     * @return 是否注册成功
     */
    boolean registerConnection(Connection connection);
    
    /**
     * 根据连接ID获取连接
     *
     * @param connectionId 连接ID
     * @return 找到的连接，如果不存在则返回空
     */
    Optional<Connection> getConnection(long connectionId);
    
    /**
     * 移除连接
     *
     * @param connectionId 要移除的连接ID
     * @return 是否移除成功
     */
    boolean removeConnection(long connectionId);
    
    /**
     * 获取用户的所有连接
     *
     * @param userId 用户ID
     * @return 该用户的所有连接列表
     */
    List<Connection> getUserConnections(String userId);
    
    /**
     * 获取用户在特定设备上的连接
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 找到的连接，如果不存在则返回空
     */
    Optional<Connection> getUserDeviceConnection(String userId, String deviceId);
    
    /**
     * 统计当前连接数
     *
     * @return 当前连接数
     */
    int countConnections();
    
    /**
     * 统计已认证的连接数
     *
     * @return 已认证的连接数
     */
    int countAuthenticatedConnections();
    
    /**
     * 获取所有连接
     *
     * @return 所有连接列表
     */
    List<Connection> getAllConnections();
    
    /**
     * 清理长时间不活跃的连接
     *
     * @param timeoutMillis 超时时间（毫秒）
     * @return 被清理的连接数
     */
    int cleanInactiveConnections(long timeoutMillis);
} 