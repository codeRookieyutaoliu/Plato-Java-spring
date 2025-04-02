package plato.gateway.infrastructure.connection;

import org.springframework.stereotype.Component;

import java.nio.channels.SocketChannel;

/**
 * NIO 连接工厂
 * 用于创建基于 Java NIO 的连接
 */
@Component
public class NioConnectionFactory implements ConnectionFactory {
    
    /**
     * 创建连接
     *
     * @param connectionId 连接ID
     * @param remoteAddress 远程地址
     * @param connectionData 连接数据（SocketChannel）
     * @return 连接对象
     */
    @Override
    public IConnection createConnection(long connectionId, String remoteAddress, Object connectionData) {
        if (!(connectionData instanceof SocketChannel)) {
            throw new IllegalArgumentException("connectionData must be a SocketChannel");
        }
        
        SocketChannel socketChannel = (SocketChannel) connectionData;
        return new NIOConnection(connectionId, socketChannel, remoteAddress);
    }
} 