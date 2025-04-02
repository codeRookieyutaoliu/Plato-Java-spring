package plato.network.infrastructure.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.network.domain.entity.Connection;
import plato.network.domain.service.ConnectionManager;
import plato.network.domain.valueobject.ConnectionStatistics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 连接管理器的默认实现
 * <p>
 * 使用ConcurrentHashMap实现高并发连接管理
 * </p>
 * 
 * 对应Go项目中的connectionManager实现
 */
public class DefaultConnectionManager implements ConnectionManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionManager.class);
    
    /**
     * 连接ID到连接对象的映射
     */
    private final Map<Long, Connection> connectionsById = new ConcurrentHashMap<>();
    
    /**
     * 用户ID到连接ID集合的映射
     */
    private final Map<String, List<Long>> connectionsByUser = new ConcurrentHashMap<>();
    
    /**
     * 用户ID和设备ID组合到连接ID的映射
     */
    private final Map<String, Long> connectionsByUserDevice = new ConcurrentHashMap<>();
    
    /**
     * 连接统计信息
     */
    private final ConnectionStatistics statistics;
    
    /**
     * 构造函数
     */
    public DefaultConnectionManager() {
        this(new ConnectionStatistics());
    }
    
    /**
     * 构造函数
     *
     * @param statistics 连接统计信息
     */
    public DefaultConnectionManager(ConnectionStatistics statistics) {
        this.statistics = statistics;
    }
    
    @Override
    public boolean registerConnection(Connection connection) {
        if (connection == null) {
            LOGGER.warn("尝试注册空连接");
            return false;
        }
        
        long connectionId = connection.getId();
        
        // 如果已经存在，则不重复注册
        if (connectionsById.containsKey(connectionId)) {
            LOGGER.warn("连接已存在，ID={}", connectionId);
            return false;
        }
        
        // 注册连接
        connectionsById.put(connectionId, connection);
        
        // 更新统计信息
        statistics.incrementTotalConnections();
        statistics.incrementActiveConnections();
        
        // 如果连接已认证，则更新用户连接映射
        if (connection.isAuthenticated()) {
            String userId = connection.getUserId();
            String deviceId = connection.getDeviceId();
            
            // 更新用户连接映射
            connectionsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(connectionId);
            
            // 更新用户设备连接映射
            String userDeviceKey = getUserDeviceKey(userId, deviceId);
            connectionsByUserDevice.put(userDeviceKey, connectionId);
            
            // 更新统计信息
            statistics.incrementAuthenticatedConnections();
            
            LOGGER.info("注册已认证连接, connectionId={}, userId={}, deviceId={}", connectionId, userId, deviceId);
        } else {
            LOGGER.info("注册未认证连接, connectionId={}", connectionId);
        }
        
        return true;
    }
    
    @Override
    public Optional<Connection> getConnection(long connectionId) {
        return Optional.ofNullable(connectionsById.get(connectionId));
    }
    
    @Override
    public boolean removeConnection(long connectionId) {
        Connection connection = connectionsById.remove(connectionId);
        
        if (connection == null) {
            LOGGER.warn("尝试移除不存在的连接, connectionId={}", connectionId);
            return false;
        }
        
        // 更新统计信息
        statistics.decrementActiveConnections();
        statistics.incrementClosedConnections();
        
        // 如果连接已认证，更新相关映射
        if (connection.isAuthenticated()) {
            String userId = connection.getUserId();
            String deviceId = connection.getDeviceId();
            
            // 从用户连接列表中移除
            List<Long> userConnections = connectionsByUser.get(userId);
            if (userConnections != null) {
                userConnections.remove(connectionId);
                
                // 如果用户没有连接了，移除用户映射
                if (userConnections.isEmpty()) {
                    connectionsByUser.remove(userId);
                }
            }
            
            // 从用户设备映射中移除
            String userDeviceKey = getUserDeviceKey(userId, deviceId);
            connectionsByUserDevice.remove(userDeviceKey);
            
            // 更新统计信息
            statistics.decrementAuthenticatedConnections();
            
            LOGGER.info("移除已认证连接, connectionId={}, userId={}, deviceId={}", connectionId, userId, deviceId);
        } else {
            LOGGER.info("移除未认证连接, connectionId={}", connectionId);
        }
        
        return true;
    }
    
    @Override
    public List<Connection> getUserConnections(String userId) {
        List<Long> connectionIds = connectionsByUser.getOrDefault(userId, new ArrayList<>());
        
        return connectionIds.stream()
                .map(this::getConnection)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Connection> getUserDeviceConnection(String userId, String deviceId) {
        String userDeviceKey = getUserDeviceKey(userId, deviceId);
        Long connectionId = connectionsByUserDevice.get(userDeviceKey);
        
        if (connectionId == null) {
            return Optional.empty();
        }
        
        return getConnection(connectionId);
    }
    
    @Override
    public int countConnections() {
        return connectionsById.size();
    }
    
    @Override
    public int countAuthenticatedConnections() {
        return (int) connectionsById.values().stream()
                .filter(Connection::isAuthenticated)
                .count();
    }
    
    @Override
    public List<Connection> getAllConnections() {
        return new ArrayList<>(connectionsById.values());
    }
    
    @Override
    public int cleanInactiveConnections(long timeoutMillis) {
        long now = Instant.now().toEpochMilli();
        List<Long> inactiveConnectionIds = connectionsById.values().stream()
                .filter(conn -> now - conn.getLastActiveTime() > timeoutMillis)
                .map(Connection::getId)
                .collect(Collectors.toList());
        
        int count = 0;
        for (Long connectionId : inactiveConnectionIds) {
            if (removeConnection(connectionId)) {
                count++;
            }
        }
        
        if (count > 0) {
            LOGGER.info("清理不活跃连接, 共{}个", count);
        }
        
        return count;
    }
    
    /**
     * 获取用户设备键
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 用户设备键
     */
    private String getUserDeviceKey(String userId, String deviceId) {
        return userId + ":" + deviceId;
    }
    
    /**
     * 获取连接统计信息
     *
     * @return 连接统计信息
     */
    public ConnectionStatistics getStatistics() {
        return statistics;
    }
} 