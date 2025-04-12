package plato.protocol.transport;

import plato.protocol.ProtocolType;
import java.net.InetSocketAddress;

/**
 * 传输层工厂接口
 * <p>
 * 用于创建不同协议类型的传输层实现
 * </p>
 */
public interface TransportFactory {
    
    /**
     * 创建传输层客户端
     *
     * @param type 协议类型
     * @return 传输层实例
     */
    Transport createClient(ProtocolType type);
    
    /**
     * 创建传输层客户端并绑定本地地址
     *
     * @param type 协议类型
     * @param localAddress 本地地址
     * @return 传输层实例
     */
    Transport createClient(ProtocolType type, InetSocketAddress localAddress);
    
    /**
     * 创建传输层服务端
     *
     * @param type 协议类型
     * @param bindAddress 绑定地址
     * @return 传输层实例
     */
    Transport createServer(ProtocolType type, InetSocketAddress bindAddress);
} 