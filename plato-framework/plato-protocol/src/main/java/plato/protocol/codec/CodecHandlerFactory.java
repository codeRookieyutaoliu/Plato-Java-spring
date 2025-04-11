package plato.protocol.codec;

import plato.protocol.ProtocolType;
import plato.protocol.codec.tcp.TcpCodecHandler;
import plato.protocol.codec.udp.UdpCodecHandler;
import plato.protocol.codec.udp.ReliabilityManager;

/**
 * 编解码处理器工厂
 * <p>
 * 用于创建不同类型的编解码器
 * </p>
 */
public class CodecHandlerFactory {
    
    /**
     * 创建TCP编解码处理器
     *
     * @return TCP编解码处理器
     */
    public static CodecHandler createTcpCodecHandler() {
        return new TcpCodecHandler();
    }
    
    /**
     * 创建TCP编解码处理器
     *
     * @param maxFrameLength 最大帧长度
     * @return TCP编解码处理器
     */
    public static CodecHandler createTcpCodecHandler(int maxFrameLength) {
        return new TcpCodecHandler(maxFrameLength);
    }
    
    /**
     * 创建TCP编解码处理器
     *
     * @param maxFrameLength 最大帧长度
     * @param listener 编解码监听器
     * @return TCP编解码处理器
     */
    public static CodecHandler createTcpCodecHandler(int maxFrameLength, CodecHandler.CodecListener listener) {
        return new TcpCodecHandler(maxFrameLength, listener);
    }
    
    /**
     * 创建UDP编解码处理器
     *
     * @return UDP编解码处理器
     */
    public static CodecHandler createUdpCodecHandler() {
        return new UdpCodecHandler();
    }
    
    /**
     * 创建UDP编解码处理器
     *
     * @param reliabilityManager 可靠性管理器
     * @return UDP编解码处理器
     */
    public static CodecHandler createUdpCodecHandler(ReliabilityManager reliabilityManager) {
        return new UdpCodecHandler(reliabilityManager);
    }
    
    /**
     * 创建编解码处理器
     *
     * @param type 协议类型
     * @return 对应类型的编解码处理器
     */
    public static CodecHandler createCodecHandler(ProtocolType type) {
        switch (type) {
            case TCP:
                return createTcpCodecHandler();
            case UDP:
                return createUdpCodecHandler();
            default:
                throw new IllegalArgumentException("不支持的协议类型: " + type);
        }
    }
} 