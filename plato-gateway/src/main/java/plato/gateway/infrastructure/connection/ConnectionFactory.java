package plato.gateway.infrastructure.connection;

/**
 * 连接工厂接口
 * 用于创建不同类型的连接
 */
public interface ConnectionFactory {
    
    /**
     * 创建连接
     *
     * @param connectionId 连接ID
     * @param remoteAddress 远程地址
     * @param connectionData 连接数据（根据不同实现可能是不同类型）
     * @return 连接对象
     */
    IConnection createConnection(long connectionId, String remoteAddress, Object connectionData);
} 