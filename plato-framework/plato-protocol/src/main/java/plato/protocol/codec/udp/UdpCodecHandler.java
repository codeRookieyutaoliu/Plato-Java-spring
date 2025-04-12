package plato.protocol.codec.udp;

import plato.protocol.ProtocolType;
import plato.protocol.codec.CodecHandler;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * UDP编解码处理器
 * <p>
 * 负责UDP消息的编码和解码，以及可靠性处理
 * </p>
 */
public class UdpCodecHandler implements CodecHandler {
    
    /**
     * 可靠性管理器
     */
    private final ReliabilityManager reliabilityManager;
    
    /**
     * 消息监听器
     */
    private Consumer<UdpMessageWrapper> messageListener;
    
    /**
     * 发送器
     */
    private Consumer<UdpMessageWrapper> sender;
    
    /**
     * 默认远程地址 (用于简单的编解码场景)
     */
    private InetSocketAddress defaultRemoteAddress;
    
    /**
     * 编解码监听器
     */
    private CodecListener codecListener;
    
    /**
     * 默认构造函数
     */
    public UdpCodecHandler() {
        this(null);
    }
    
    /**
     * 构造函数
     *
     * @param reliabilityManager 可靠性管理器
     */
    public UdpCodecHandler(ReliabilityManager reliabilityManager) {
        this.reliabilityManager = reliabilityManager;
        
        if (reliabilityManager != null) {
            // 设置可靠性管理器的发送器
            reliabilityManager.setSender((data, address) -> {
                if (sender != null) {
                    sender.accept(new UdpMessageWrapper(data, address));
                }
            });
            
            // 启动重传任务
            reliabilityManager.startRetransmissionTask();
        }
    }
    
    /**
     * 设置消息监听器
     *
     * @param listener 消息监听器
     */
    public void setMessageListener(Consumer<UdpMessageWrapper> listener) {
        this.messageListener = listener;
    }
    
    /**
     * 设置发送器
     *
     * @param sender 发送器
     */
    public void setSender(Consumer<UdpMessageWrapper> sender) {
        this.sender = sender;
    }
    
    /**
     * 设置默认远程地址
     *
     * @param address 默认远程地址
     */
    public void setDefaultRemoteAddress(InetSocketAddress address) {
        this.defaultRemoteAddress = address;
    }
    
    /**
     * 编码消息
     *
     * @param message 消息数据
     * @param type 消息类型
     * @param sequenceId 序列号
     * @param address 目标地址
     * @return 编码后的UDP包装消息
     */
    public UdpMessageWrapper encodeWithType(byte[] message, PacketType type, int sequenceId, InetSocketAddress address) {
        // 创建UDP数据包
        UdpPacket packet = new UdpPacket(type, sequenceId, message, address);
        
        // 如果是可靠消息且有可靠性管理器，则添加到重传队列
        if (type.isReliable() && reliabilityManager != null) {
            reliabilityManager.addReliableMessage(packet, address);
        }
        
        // 序列化
        byte[] encodedData = packet.marshal();
        
        // 通知监听器
        if (codecListener != null) {
            codecListener.onEncode(message, encodedData.length);
        }
        
        // 返回包装消息
        return new UdpMessageWrapper(encodedData, address);
    }
    
    /**
     * 编码消息（自动生成序列号）
     *
     * @param message 消息数据
     * @param type 消息类型
     * @param address 目标地址
     * @return 编码后的UDP包装消息
     */
    public UdpMessageWrapper encodeWithType(byte[] message, PacketType type, InetSocketAddress address) {
        int sequenceId = 0;
        
        // 如果有可靠性管理器，使用会话的序列号
        if (reliabilityManager != null) {
            UdpSession session = reliabilityManager.getSession(address);
            if (session == null) {
                // 如果需要可靠传输但会话不存在，则创建会话
                if (type.isReliable()) {
                    session = reliabilityManager.createSession(System.nanoTime(), address);
                }
            }
            
            if (session != null) {
                sequenceId = session.nextSequenceId();
            }
        }
        
        return encodeWithType(message, type, sequenceId, address);
    }
    
    @Override
    public byte[] encode(byte[] data) {
        if (defaultRemoteAddress == null) {
            throw new IllegalStateException("默认远程地址未设置，无法进行简单编码");
        }
        
        // 使用默认地址和可靠消息类型进行编码
        UdpMessageWrapper wrapper = encodeWithType(data, PacketType.RELIABLE, defaultRemoteAddress);
        return wrapper.getMessage();
    }
    
    /**
     * 解码消息
     *
     * @param data 消息数据
     * @param address 源地址
     * @return 解码后的UDP数据包
     */
    public UdpPacket decodeToPacket(byte[] data, InetSocketAddress address) {
        try {
            // 解析UDP数据包
            UdpPacket packet = UdpPacket.unmarshal(data, address);
            
            // 如果有可靠性管理器，处理可靠传输
            if (reliabilityManager != null) {
                // 处理接收到的包
                boolean isNewPacket = reliabilityManager.processReceivedPacket(packet);
                
                // 如果是确认包或过期包，不继续处理
                if (!isNewPacket || packet.getPacketType() == PacketType.ACK) {
                    return null;
                }
            }
            
            return packet;
        } catch (Exception e) {
            // 解析失败，记录错误
            System.err.println("解析UDP数据包失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public byte[] decode(byte[] data) {
        if (defaultRemoteAddress == null) {
            throw new IllegalStateException("默认远程地址未设置，无法进行简单解码");
        }
        
        // 使用默认地址解码
        UdpPacket packet = decodeToPacket(data, defaultRemoteAddress);
        if (packet == null) {
            return null;
        }
        
        byte[] decodedData = packet.getPayload();
        
        // 通知监听器
        if (codecListener != null && decodedData != null) {
            codecListener.onDecode(decodedData, decodedData.length);
        }
        
        return decodedData;
    }
    
    /**
     * 处理接收到的消息
     *
     * @param data 消息数据
     * @param address 源地址
     */
    public void handleReceived(byte[] data, InetSocketAddress address) {
        // 解码消息
        UdpPacket packet = decodeToPacket(data, address);
        
        // 如果解码成功且有监听器，则调用监听器
        if (packet != null && messageListener != null) {
            messageListener.accept(new UdpMessageWrapper(packet.getPayload(), address));
        }
    }
    
    /**
     * 发送消息
     *
     * @param message 消息数据
     * @param type 消息类型
     * @param address 目标地址
     */
    public void send(byte[] message, PacketType type, InetSocketAddress address) {
        if (sender != null) {
            UdpMessageWrapper wrapper = encodeWithType(message, type, address);
            sender.accept(wrapper);
        }
    }
    
    /**
     * 发送不可靠消息
     *
     * @param message 消息数据
     * @param address 目标地址
     */
    public void sendUnreliable(byte[] message, InetSocketAddress address) {
        send(message, PacketType.UNRELIABLE, address);
    }
    
    /**
     * 发送可靠消息
     *
     * @param message 消息数据
     * @param address 目标地址
     */
    public void sendReliable(byte[] message, InetSocketAddress address) {
        send(message, PacketType.RELIABLE, address);
    }
    
    /**
     * 发送心跳包
     *
     * @param address 目标地址
     */
    public void sendHeartbeat(InetSocketAddress address) {
        send(new byte[0], PacketType.HEARTBEAT, address);
    }
    
    /**
     * 关闭处理器
     */
    public void close() {
        if (reliabilityManager != null) {
            reliabilityManager.stopRetransmissionTask();
        }
    }
    
    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.UDP;
    }
    
    @Override
    public void setCodecListener(CodecListener listener) {
        this.codecListener = listener;
    }
    
    @Override
    public CodecListener getCodecListener() {
        return this.codecListener;
    }
} 