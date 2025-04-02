package plato.common.net;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 连接管理器接口
 * <p>
 * 用于管理连接的生命周期，包括连接的注册、查找、关闭等操作
 * 抽象出统一的连接管理接口，支持不同的连接实现
 * </p>
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
     * 根据ID查找连接
     * 
     * @param connectionId 连接ID
     * @return 连接对象，可能不存在
     */
    Optional<Connection> findConnection(String connectionId);
    
    /**
     * 移除连接
     * 
     * @param connectionId 连接ID
     * @return 是否成功移除
     */
    boolean removeConnection(String connectionId);
    
    /**
     * 关闭所有连接
     * 
     * @return 关闭的连接数量
     */
    int closeAllConnections();
    
    /**
     * 获取符合条件的连接列表
     * 
     * @param filter 过滤条件
     * @return 符合条件的连接列表
     */
    List<Connection> findConnections(Predicate<Connection> filter);
    
    /**
     * 获取连接总数
     * 
     * @return 当前管理的连接总数
     */
    int getConnectionCount();
    
    /**
     * 获取活跃连接总数
     * 
     * @return 当前活跃的连接总数
     */
    int getActiveConnectionCount();
    
    /**
     * 发送消息到指定连接
     * 
     * @param connectionId 连接ID
     * @param message 要发送的消息
     * @return 是否发送成功
     */
    boolean sendMessage(String connectionId, byte[] message);
    
    /**
     * 广播消息到所有连接
     * 
     * @param message 要广播的消息
     * @return 成功发送的连接数量
     */
    int broadcastMessage(byte[] message);
    
    /**
     * 广播消息到符合条件的连接
     * 
     * @param message 要广播的消息
     * @param filter 连接过滤条件
     * @return 成功发送的连接数量
     */
    int broadcastMessage(byte[] message, Predicate<Connection> filter);
} 