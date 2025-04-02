package plato.gateway.infrastructure.server;

/**
 * TCP 服务器类型
 */
public enum TcpServerType {
    /**
     * 基于 Java NIO 的实现
     */
    NIO,
    
    /**
     * 基于 Netty 的实现
     */
    NETTY
} 