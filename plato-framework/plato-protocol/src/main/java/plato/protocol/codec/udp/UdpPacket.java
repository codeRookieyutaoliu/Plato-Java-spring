package plato.protocol.codec.udp;

import java.nio.ByteBuffer;
import java.net.InetSocketAddress;

/**
 * UDP数据包
 * <p>
 * 用于封装UDP数据包的结构和内容，基于以下格式：
 * [1字节包类型][4字节序列号][4字节消息长度][n字节消息内容]
 * </p>
 */
public class UdpPacket {
    /**
     * 包类型
     */
    private final PacketType packetType;
    
    /**
     * 序列号
     */
    private final int sequenceId;
    
    /**
     * 消息内容
     */
    private final byte[] payload;
    
    /**
     * 远程地址（发送者或接收者）
     */
    private InetSocketAddress remoteAddress;
    
    /**
     * 构造函数
     *
     * @param packetType 包类型
     * @param sequenceId 序列号
     * @param payload 消息内容
     */
    public UdpPacket(PacketType packetType, int sequenceId, byte[] payload) {
        this.packetType = packetType;
        this.sequenceId = sequenceId;
        this.payload = payload != null ? payload : new byte[0];
    }
    
    /**
     * 构造函数
     *
     * @param packetType 包类型
     * @param sequenceId 序列号
     * @param payload 消息内容
     * @param remoteAddress 远程地址
     */
    public UdpPacket(PacketType packetType, int sequenceId, byte[] payload, InetSocketAddress remoteAddress) {
        this(packetType, sequenceId, payload);
        this.remoteAddress = remoteAddress;
    }
    
    /**
     * 获取包类型
     *
     * @return 包类型
     */
    public PacketType getPacketType() {
        return packetType;
    }
    
    /**
     * 获取序列号
     *
     * @return 序列号
     */
    public int getSequenceId() {
        return sequenceId;
    }
    
    /**
     * 获取消息内容
     *
     * @return 消息内容
     */
    public byte[] getPayload() {
        return payload;
    }
    
    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    /**
     * 设置远程地址
     *
     * @param remoteAddress 远程地址
     */
    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    
    /**
     * 序列化为字节数组
     *
     * @return 序列化后的字节数组
     */
    public byte[] marshal() {
        // 计算所需的总字节数 = 1(类型) + 4(序列号) + 4(长度) + n(内容)
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + 4 + payload.length);
        
        // 写入包类型
        buffer.put(packetType.getCode());
        
        // 写入序列号
        buffer.putInt(sequenceId);
        
        // 写入长度
        buffer.putInt(payload.length);
        
        // 写入消息内容
        if (payload.length > 0) {
            buffer.put(payload);
        }
        
        return buffer.array();
    }
    
    /**
     * 从字节数组反序列化
     *
     * @param data 字节数组
     * @return UDP数据包
     */
    public static UdpPacket unmarshal(byte[] data) {
        if (data == null || data.length < 9) { // 至少需要1+4+4字节
            throw new IllegalArgumentException("数据长度不足，无法解析UDP数据包");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(data);
        
        // 读取包类型
        byte typeCode = buffer.get();
        PacketType packetType = PacketType.fromCode(typeCode);
        
        // 读取序列号
        int sequenceId = buffer.getInt();
        
        // 读取长度
        int length = buffer.getInt();
        
        // 验证长度
        if (buffer.remaining() < length) {
            throw new IllegalArgumentException("数据长度不足，期望" + length + "字节，实际只有" + buffer.remaining() + "字节");
        }
        
        // 读取消息内容
        byte[] payload = new byte[length];
        if (length > 0) {
            buffer.get(payload);
        }
        
        return new UdpPacket(packetType, sequenceId, payload);
    }
    
    /**
     * 从字节数组反序列化，并设置远程地址
     *
     * @param data 字节数组
     * @param remoteAddress 远程地址
     * @return UDP数据包
     */
    public static UdpPacket unmarshal(byte[] data, InetSocketAddress remoteAddress) {
        UdpPacket packet = unmarshal(data);
        packet.setRemoteAddress(remoteAddress);
        return packet;
    }
    
    @Override
    public String toString() {
        return "UdpPacket{" +
                "type=" + packetType +
                ", seq=" + sequenceId +
                ", length=" + (payload != null ? payload.length : 0) +
                ", remote=" + (remoteAddress != null ? remoteAddress.toString() : "null") +
                '}';
    }
} 