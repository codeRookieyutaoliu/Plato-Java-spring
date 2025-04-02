package plato.common.net;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 默认连接管理器实现
 * <p>
 * 提供ConnectionManager接口的标准实现
 * 使用ConcurrentHashMap存储和管理连接
 * </p>
 */
@Slf4j
public class DefaultConnectionManager implements ConnectionManager {
    
    /**
     * 连接存储映射表
     */
    private final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    
    /**
     * 连接事件监听器列表
     */
    private final List<ConnectionListener> listeners = new ArrayList<>();
    
    /**
     * 注册连接事件监听器
     * 
     * @param listener 监听器对象
     */
    public void addConnectionListener(ConnectionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    /**
     * 移除连接事件监听器
     * 
     * @param listener 监听器对象
     * @return 是否成功移除
     */
    public boolean removeConnectionListener(ConnectionListener listener) {
        return listeners.remove(listener);
    }
    
    /**
     * 注册连接
     *
     * @param connection 要注册的连接
     * @return 是否注册成功
     */
    @Override
    public boolean registerConnection(Connection connection) {
        if (connection == null || connection.getId() == null) {
            return false;
        }
        
        Connection oldConnection = connectionMap.put(connection.getId(), connection);
        if (oldConnection != null && oldConnection != connection) {
            log.warn("连接ID冲突，旧连接被替换: connectionId={}", connection.getId());
            try {
                oldConnection.close();
            } catch (Exception e) {
                log.error("关闭冲突连接时发生异常: connectionId={}", connection.getId(), e);
            }
        }
        
        log.info("注册新连接: connectionId={}, remoteAddress={}", 
                connection.getId(), connection.getRemoteAddress());
        
        // 通知监听器
        for (ConnectionListener listener : listeners) {
            try {
                listener.onConnectionEstablished(connection);
            } catch (Exception e) {
                log.error("通知连接建立事件时发生异常", e);
            }
        }
        
        // 设置连接事件处理器
        connection.setEventHandler(new ConnectionEventProcessor(connection));
        
        return true;
    }
    
    /**
     * 根据ID查找连接
     *
     * @param connectionId 连接ID
     * @return 连接对象，可能不存在
     */
    @Override
    public Optional<Connection> findConnection(String connectionId) {
        return Optional.ofNullable(connectionMap.get(connectionId));
    }
    
    /**
     * 移除连接
     *
     * @param connectionId 连接ID
     * @return 是否成功移除
     */
    @Override
    public boolean removeConnection(String connectionId) {
        Connection connection = connectionMap.remove(connectionId);
        if (connection != null) {
            log.info("连接已移除: connectionId={}, remoteAddress={}", 
                    connectionId, connection.getRemoteAddress());
            return true;
        }
        return false;
    }
    
    /**
     * 关闭所有连接
     *
     * @return 关闭的连接数量
     */
    @Override
    public int closeAllConnections() {
        int count = 0;
        List<Connection> connections = new ArrayList<>(connectionMap.values());
        
        for (Connection connection : connections) {
            try {
                connection.close();
                count++;
            } catch (Exception e) {
                log.error("关闭连接时发生异常: connectionId={}", connection.getId(), e);
            }
        }
        
        connectionMap.clear();
        log.info("已关闭所有连接: count={}", count);
        return count;
    }
    
    /**
     * 获取符合条件的连接列表
     *
     * @param filter 过滤条件
     * @return 符合条件的连接列表
     */
    @Override
    public List<Connection> findConnections(Predicate<Connection> filter) {
        if (filter == null) {
            return new ArrayList<>(connectionMap.values());
        }
        return connectionMap.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取连接总数
     *
     * @return 当前管理的连接总数
     */
    @Override
    public int getConnectionCount() {
        return connectionMap.size();
    }
    
    /**
     * 获取活跃连接总数
     *
     * @return 当前活跃的连接总数
     */
    @Override
    public int getActiveConnectionCount() {
        return (int) connectionMap.values().stream()
                .filter(Connection::isActive)
                .count();
    }
    
    /**
     * 发送消息到指定连接
     *
     * @param connectionId 连接ID
     * @param message 要发送的消息
     * @return 是否发送成功
     */
    @Override
    public boolean sendMessage(String connectionId, byte[] message) {
        Optional<Connection> connectionOpt = findConnection(connectionId);
        if (connectionOpt.isPresent()) {
            Connection connection = connectionOpt.get();
            if (connection.isActive()) {
                ByteBuffer buffer = ByteBuffer.wrap(message);
                return connection.send(buffer);
            } else {
                log.warn("连接已关闭，无法发送消息: connectionId={}", connectionId);
                removeConnection(connectionId);
            }
        } else {
            log.warn("连接不存在，无法发送消息: connectionId={}", connectionId);
        }
        return false;
    }
    
    /**
     * 广播消息到所有连接
     *
     * @param message 要广播的消息
     * @return 成功发送的连接数量
     */
    @Override
    public int broadcastMessage(byte[] message) {
        return broadcastMessage(message, c -> true);
    }
    
    /**
     * 广播消息到符合条件的连接
     *
     * @param message 要广播的消息
     * @param filter 连接过滤条件
     * @return 成功发送的连接数量
     */
    @Override
    public int broadcastMessage(byte[] message, Predicate<Connection> filter) {
        if (message == null || message.length == 0) {
            return 0;
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(message);
        int successCount = 0;
        
        for (Connection connection : connectionMap.values()) {
            if (connection.isActive() && (filter == null || filter.test(connection))) {
                try {
                    // 对每个连接使用副本，避免共享同一个buffer的position
                    ByteBuffer copy = buffer.duplicate();
                    if (connection.send(copy)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("广播消息时发生异常: connectionId={}", connection.getId(), e);
                }
            }
        }
        
        return successCount;
    }
    
    /**
     * 连接事件处理器实现
     */
    private class ConnectionEventProcessor implements Connection.ConnectionEventHandler {
        private final Connection connection;
        
        public ConnectionEventProcessor(Connection connection) {
            this.connection = connection;
        }
        
        @Override
        public void onReceive(Connection conn, ByteBuffer data) {
            for (ConnectionListener listener : listeners) {
                try {
                    // 创建数据副本，因为不同的监听器可能会修改buffer的position
                    byte[] messageBytes = new byte[data.remaining()];
                    data.duplicate().get(messageBytes);
                    listener.onMessageReceived(conn, messageBytes);
                } catch (Exception e) {
                    log.error("通知消息接收事件时发生异常", e);
                }
            }
        }
        
        @Override
        public void onClose(Connection conn) {
            removeConnection(conn.getId());
            
            for (ConnectionListener listener : listeners) {
                try {
                    listener.onConnectionClosed(conn);
                } catch (Exception e) {
                    log.error("通知连接关闭事件时发生异常", e);
                }
            }
        }
        
        @Override
        public void onError(Connection conn, Throwable error) {
            for (ConnectionListener listener : listeners) {
                try {
                    listener.onConnectionError(conn, error);
                } catch (Exception e) {
                    log.error("通知连接错误事件时发生异常", e);
                }
            }
        }
    }
} 