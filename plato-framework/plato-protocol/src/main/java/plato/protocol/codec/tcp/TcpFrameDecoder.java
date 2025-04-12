package plato.protocol.codec.tcp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * TCP帧解码器
 * <p>
 * 用于处理TCP粘包/拆包问题，基于长度字段前缀。
 * 在Go项目中，类似功能是在common/tcp/read.go的readFixedData实现的。
 * </p>
 */
public class TcpFrameDecoder {
    
    /**
     * 长度字段的偏移量
     */
    private final int lengthFieldOffset;
    
    /**
     * 长度字段的长度（字节数）
     */
    private final int lengthFieldLength;
    
    /**
     * 长度调整值
     */
    private final int lengthAdjustment;
    
    /**
     * 需要跳过的初始字节数
     */
    private final int initialBytesToStrip;
    
    /**
     * 最大帧长度
     */
    private final int maxFrameLength;
    
    /**
     * 当前累积的缓冲区
     */
    private ByteBuffer cumulation;
    
    /**
     * 构造函数
     *
     * @param maxFrameLength 最大帧长度
     * @param lengthFieldOffset 长度字段的偏移量
     * @param lengthFieldLength 长度字段的长度
     * @param lengthAdjustment 长度调整值
     * @param initialBytesToStrip 需要跳过的初始字节数
     */
    public TcpFrameDecoder(
            int maxFrameLength,
            int lengthFieldOffset,
            int lengthFieldLength,
            int lengthAdjustment,
            int initialBytesToStrip) {
        this.maxFrameLength = maxFrameLength;
        this.lengthFieldOffset = lengthFieldOffset;
        this.lengthFieldLength = lengthFieldLength;
        this.lengthAdjustment = lengthAdjustment;
        this.initialBytesToStrip = initialBytesToStrip;
        this.cumulation = ByteBuffer.allocate(0);
    }
    
    /**
     * 为Plato协议创建一个解码器
     * <p>
     * 使用默认的参数：
     * - 长度字段偏移量: 0
     * - 长度字段长度: 4 (int)
     * - 长度调整值: 0
     * - 初始跳过字节数: 4 (跳过长度字段)
     * </p>
     *
     * @param maxFrameLength 最大帧长度
     * @return 适用于Plato协议的解码器
     */
    public static TcpFrameDecoder forPlatoProtocol(int maxFrameLength) {
        return new TcpFrameDecoder(
                maxFrameLength,  // 最大帧长度
                0,               // 长度字段偏移量
                4,               // 长度字段占4字节
                0,               // 长度调整值
                4                // 跳过长度字段
        );
    }
    
    /**
     * 添加数据到累积缓冲区
     *
     * @param data 新接收的数据
     */
    public void addData(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        
        // 创建新的累积缓冲区
        ByteBuffer newCumulation = ByteBuffer.allocate(cumulation.remaining() + data.length);
        
        // 复制当前累积缓冲区的内容
        newCumulation.put((ByteBuffer) cumulation.duplicate().flip());
        
        // 添加新数据
        newCumulation.put(data);
        
        // 切换到读模式
        newCumulation.flip();
        
        // 更新累积缓冲区
        cumulation = newCumulation;
    }
    
    /**
     * 解码出完整的帧
     *
     * @return 解码出的完整帧，如果没有完整帧则返回null
     */
    public byte[] decode() {
        if (cumulation.remaining() < lengthFieldOffset + lengthFieldLength) {
            return null;  // 不足以读取长度字段
        }
        
        // 标记当前位置
        cumulation.mark();
        int markPosition = cumulation.position();
        
        // 跳过长度字段偏移量
        cumulation.position(cumulation.position() + lengthFieldOffset);
        
        // 读取长度字段
        int frameLength;
        if (lengthFieldLength == 4) {
            frameLength = cumulation.getInt();
        } else if (lengthFieldLength == 2) {
            frameLength = cumulation.getShort() & 0xFFFF;
        } else if (lengthFieldLength == 8) {
            frameLength = (int) cumulation.getLong();
        } else {
            throw new IllegalStateException("不支持的长度字段长度: " + lengthFieldLength);
        }
        
        // 应用长度调整
        frameLength += lengthAdjustment;
        
        // 检查长度是否超过最大值
        if (frameLength > maxFrameLength) {
            cumulation.reset();
            throw new RuntimeException("帧长度超过最大值: " + frameLength + " > " + maxFrameLength);
        }
        
        // 检查是否有完整的帧
        int actualFrameLength = frameLength + lengthFieldOffset + lengthFieldLength - initialBytesToStrip;
        int availableBytes = cumulation.remaining() + (cumulation.position() - markPosition);
        if (availableBytes < actualFrameLength) {
            cumulation.reset();
            return null;  // 帧不完整
        }
        
        // 重置位置并跳过不需要的字节
        cumulation.reset();
        cumulation.position(cumulation.position() + initialBytesToStrip);
        
        // 读取帧数据
        byte[] frame = new byte[frameLength];
        cumulation.get(frame);
        
        // 更新累积缓冲区
        ByteBuffer newCumulation = ByteBuffer.allocate(cumulation.remaining());
        newCumulation.put(cumulation);
        newCumulation.flip();
        cumulation = newCumulation;
        
        return frame;
    }
    
    /**
     * 解码出所有完整的帧
     *
     * @return 解码出的所有完整帧列表
     */
    public List<byte[]> decodeAll() {
        List<byte[]> frames = new ArrayList<>();
        byte[] frame;
        while ((frame = decode()) != null) {
            frames.add(frame);
        }
        return frames;
    }
    
    /**
     * 获取当前累积缓冲区的可读字节数
     *
     * @return 可读字节数
     */
    public int readableBytes() {
        return cumulation.remaining();
    }
    
    /**
     * 清空累积缓冲区
     */
    public void clear() {
        cumulation = ByteBuffer.allocate(0);
    }
} 