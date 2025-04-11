package plato.network.infrastructure.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.protocol.codec.tcp.TcpFrameDecoder;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * TCP帧解码器适配器
 * <p>
 * 将自定义的TcpFrameDecoder适配为Netty的ChannelHandler
 * </p>
 */
public class TcpFrameDecoderAdapter extends ByteToMessageDecoder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpFrameDecoderAdapter.class);
    
    /**
     * 内部的TCP帧解码器
     */
    private final TcpFrameDecoder decoder;
    
    /**
     * 构造函数
     *
     * @param maxFrameLength 最大帧长度
     * @param lengthFieldOffset 长度字段的偏移量
     * @param lengthFieldLength 长度字段的长度
     * @param lengthAdjustment 长度调整值
     * @param initialBytesToStrip 需要跳过的初始字节数
     */
    public TcpFrameDecoderAdapter(
            int maxFrameLength,
            int lengthFieldOffset,
            int lengthFieldLength,
            int lengthAdjustment,
            int initialBytesToStrip) {
        this.decoder = new TcpFrameDecoder(
                maxFrameLength,
                lengthFieldOffset,
                lengthFieldLength,
                lengthAdjustment,
                initialBytesToStrip
        );
    }
    
    /**
     * 为Plato协议创建一个解码器适配器
     * <p>
     * 使用默认的参数：
     * - 长度字段偏移量: 0
     * - 长度字段长度: 4 (int)
     * - 长度调整值: 0
     * - 初始跳过字节数: 4 (跳过长度字段)
     * </p>
     *
     * @param maxFrameLength 最大帧长度
     * @return 适用于Plato协议的解码器适配器
     */
    public static TcpFrameDecoderAdapter forPlatoProtocol(int maxFrameLength) {
        return new TcpFrameDecoderAdapter(
                maxFrameLength,  // 最大帧长度
                0,               // 长度字段偏移量
                4,               // 长度字段占4字节
                0,               // 长度调整值
                4                // 跳过长度字段
        );
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 如果没有足够的数据，不做任何处理
        if (in.readableBytes() < 4) {
            return;
        }
        
        try {
            // 将ByteBuf数据转为字节数组
            byte[] data = new byte[in.readableBytes()];
            in.getBytes(in.readerIndex(), data);
            
            // 添加数据到解码器
            decoder.addData(data);
            
            // 解码所有完整的帧
            java.util.List<byte[]> frames = decoder.decodeAll();
            
            // 如果解码出了帧，更新ByteBuf的读取位置
            if (!frames.isEmpty()) {
                in.skipBytes(data.length - decoder.readableBytes());
                
                // 将解码出的帧添加到输出列表
                for (byte[] frame : frames) {
                    out.add(frame);
                }
                
                LOGGER.debug("解码完成: 解码{}个完整帧", frames.size());
            }
        } catch (Exception e) {
            LOGGER.error("解码异常: {}", e.getMessage(), e);
            ctx.close();
        }
    }
} 