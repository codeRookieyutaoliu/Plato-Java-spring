package plato.protocol.codec.udp;

import java.net.InetSocketAddress;

/**
 * UDP消息包装器
 * <p>
 * 将消息与远程地址关联起来，用于UDP消息处理
 * </p>
 */
public class UdpMessageWrapper {
    
    /**
     * 消息数据
     */
    private final byte[] message;
    
    /**
     * 远程地址
     */
    private final InetSocketAddress remoteAddress;
    
    /**
     * 构造函数
     *
     * @param message 消息数据
     * @param remoteAddress 远程地址
     */
    public UdpMessageWrapper(byte[] message, InetSocketAddress remoteAddress) {
        this.message = message;
        this.remoteAddress = remoteAddress;
    }
    
    /**
     * 获取消息数据
     *
     * @return 消息数据
     */
    public byte[] getMessage() {
        return message;
    }
    
    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
} 