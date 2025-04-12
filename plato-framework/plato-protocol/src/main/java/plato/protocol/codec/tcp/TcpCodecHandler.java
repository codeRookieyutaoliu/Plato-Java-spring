package plato.protocol.codec.tcp;

import plato.protocol.ProtocolType;
import plato.protocol.codec.CodecHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * TCP编解码处理器
 * <p>
 * 整合TCP消息的编码和解码功能，处理TCP粘包/拆包问题
 * </p>
 */
public class TcpCodecHandler implements CodecHandler {
    
    /**
     * 长度字段的长度（字节数）
     */
    private static final int LENGTH_FIELD_LENGTH = 4;
    
    /**
     * 最大帧长度
     */
    private final int maxFrameLength;
    
    /**
     * 解码缓冲区
     */
    private ByteBuffer decodeBuffer;
    
    /**
     * 编解码监听器
     */
    private CodecListener codecListener;
    
    /**
     * 构造函数
     *
     * @param maxFrameLength 最大帧长度
     * @param codecListener 编解码监听器，可以为null
     */
    public TcpCodecHandler(int maxFrameLength, CodecListener codecListener) {
        this.maxFrameLength = maxFrameLength;
        this.codecListener = codecListener;
        this.decodeBuffer = ByteBuffer.allocate(0);
    }
    
    /**
     * 构造函数
     *
     * @param maxFrameLength 最大帧长度
     */
    public TcpCodecHandler(int maxFrameLength) {
        this(maxFrameLength, null);
    }
    
    /**
     * 默认构造函数，使用默认最大帧长度（5MB）
     */
    public TcpCodecHandler() {
        this(5 * 1024 * 1024);
    }
    
    @Override
    public byte[] encode(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        try {
            // 创建数据包并序列化
            DataPacket packet = new DataPacket(data);
            byte[] encodedData = packet.marshal();
            
            // 通知监听器
            if (codecListener != null) {
                codecListener.onEncode(data, encodedData.length);
            }
            
            return encodedData;
        } catch (Exception e) {
            throw new RuntimeException("编码消息失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] decode(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        
        // 将新数据追加到缓冲区
        ByteBuffer newBuffer = ByteBuffer.allocate(decodeBuffer.remaining() + data.length);
        newBuffer.put((ByteBuffer) decodeBuffer.duplicate().flip());
        newBuffer.put(data);
        newBuffer.flip();
        decodeBuffer = newBuffer;
        
        // 检查是否有完整的消息
        if (decodeBuffer.remaining() < LENGTH_FIELD_LENGTH) {
            return null;
        }
        
        // 标记当前位置以便回退
        decodeBuffer.mark();
        
        // 读取消息长度
        int messageLength = decodeBuffer.getInt();
        
        // 检查长度是否合法
        if (messageLength <= 0 || messageLength > maxFrameLength) {
            // 长度非法，重置并清空缓冲区
            decodeBuffer = ByteBuffer.allocate(0);
            throw new IllegalArgumentException("非法的消息长度: " + messageLength);
        }
        
        // 检查是否有完整的消息内容
        if (decodeBuffer.remaining() < messageLength) {
            // 消息不完整，回退到标记处
            decodeBuffer.reset();
            return null;
        }
        
        // 读取消息内容
        byte[] messageBytes = new byte[messageLength];
        decodeBuffer.get(messageBytes);
        
        // 更新缓冲区，保留剩余数据
        ByteBuffer remainingBuffer = ByteBuffer.allocate(decodeBuffer.remaining());
        remainingBuffer.put(decodeBuffer);
        remainingBuffer.flip();
        decodeBuffer = remainingBuffer;
        
        // 通知监听器
        if (codecListener != null) {
            codecListener.onDecode(messageBytes, messageLength);
        }
        
        return messageBytes;
    }
    
    /**
     * 解码多个消息
     *
     * @param data 接收到的数据
     * @return 解码出的所有消息
     */
    public List<byte[]> decodeMultiple(byte[] data) {
        List<byte[]> messages = new ArrayList<>();
        
        // 添加数据
        byte[] message = null;
        do {
            message = decode(data);
            if (message != null) {
                messages.add(message);
                // 只在第一次使用传入的数据，后续使用内部缓冲区
                data = new byte[0];
            }
        } while (message != null);
        
        return messages;
    }
    
    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }
    
    @Override
    public void setCodecListener(CodecListener listener) {
        this.codecListener = listener;
    }
    
    @Override
    public CodecListener getCodecListener() {
        return this.codecListener;
    }
    
    /**
     * 清空解码缓冲区
     */
    public void clearDecodeBuffer() {
        this.decodeBuffer = ByteBuffer.allocate(0);
    }
    
    /**
     * 获取解码缓冲区中剩余的字节数
     *
     * @return 剩余字节数
     */
    public int getRemainingBytes() {
        return decodeBuffer.remaining();
    }
} 