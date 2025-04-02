package plato.gateway.infrastructure.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * TCP 服务器工厂
 * 用于创建不同类型的服务器
 */
@Component
public class TcpServerFactory {
    
    // NIO TCP 服务器
    private final ITcpServer nioTcpServer;
    
    // Netty TCP 服务器
    private final ITcpServer nettyTcpServer;
    
    /**
     * 构造函数
     *
     * @param nioTcpServer NIO TCP 服务器
     * @param nettyTcpServer Netty TCP 服务器
     */
    @Autowired
    public TcpServerFactory(@Qualifier("nioTcpServer") ITcpServer nioTcpServer,
                           @Qualifier("nettyTcpServer") ITcpServer nettyTcpServer) {
        this.nioTcpServer = nioTcpServer;
        this.nettyTcpServer = nettyTcpServer;
    }
    
    /**
     * 获取 NIO TCP 服务器
     *
     * @return NIO TCP 服务器
     */
    public ITcpServer getNioTcpServer() {
        return nioTcpServer;
    }
    
    /**
     * 获取 Netty TCP 服务器
     *
     * @return Netty TCP 服务器
     */
    public ITcpServer getNettyTcpServer() {
        return nettyTcpServer;
    }
    
    /**
     * 根据类型获取 TCP 服务器
     *
     * @param type 服务器类型
     * @return TCP 服务器
     */
    public ITcpServer getTcpServer(TcpServerType type) {
        switch (type) {
            case NIO:
                return nioTcpServer;
            case NETTY:
                return nettyTcpServer;
            default:
                throw new IllegalArgumentException("Unknown TCP server type: " + type);
        }
    }
} 