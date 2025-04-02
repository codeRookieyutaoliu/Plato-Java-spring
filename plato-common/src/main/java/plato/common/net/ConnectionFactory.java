package plato.common.net;

import java.util.Map;

/**
 * 连接工厂接口
 * <p>
 * 定义创建和管理连接的通用接口，可用于创建不同类型的连接
 * 如TCP连接、WebSocket连接等
 * </p>
 */
public interface ConnectionFactory<T> {
    
    /**
     * 创建新连接
     * 
     * @param connectionId 连接ID
     * @param remoteAddress 远程地址
     * @param connectionData 原始连接数据，根据实现类型而定
     * @return 创建的连接对象
     */
    Connection createConnection(String connectionId, String remoteAddress, T connectionData);
    
    /**
     * 获取连接
     * 
     * @param connectionId 连接ID
     * @return 对应的连接对象，如果不存在则返回null
     */
    Connection getConnection(String connectionId);
    
    /**
     * 关闭连接
     * 
     * @param connectionId 连接ID
     * @return 是否成功关闭
     */
    boolean closeConnection(String connectionId);
    
    /**
     * 获取当前所有活跃连接
     * 
     * @return 连接ID到连接对象的映射
     */
    Map<String, Connection> getActiveConnections();
    
    /**
     * 获取当前活跃连接数
     * 
     * @return 活跃连接数量
     */
    int getActiveConnectionCount();
} 