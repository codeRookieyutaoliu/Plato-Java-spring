package plato.network.infrastructure.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.domain.entity.Message;
import plato.message.domain.factory.MessageFactory;

import java.util.List;

/**
 * 消息解码器
 * <p>
 * 将字节流解析为消息对象
 * </p>
 * 
 * 对应Go项目中的消息解码逻辑
 */
public class MessageDecoder extends ByteToMessageDecoder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDecoder.class);
    
    /**
     * 消息头长度（4字节表示消息长度）
     */
    private static final int HEADER_LENGTH = 4;
    
    /**
     * 最大消息长度（5MB）
     */
    private static final int MAX_MESSAGE_LENGTH = 5 * 1024 * 1024;
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 如果可读字节数小于消息头长度，则等待更多数据
        if (in.readableBytes() < HEADER_LENGTH) {
            return;
        }
        
        // 标记当前读取位置
        in.markReaderIndex();
        
        // 读取消息长度
        int messageLength = in.readInt();
        
        // 检查消息长度是否合法
        if (messageLength <= 0 || messageLength > MAX_MESSAGE_LENGTH) {
            LOGGER.error("非法的消息长度: {}", messageLength);
            ctx.close();
            return;
        }
        
        // 如果可读字节数小于消息长度，则重置读取位置，等待更多数据
        if (in.readableBytes() < messageLength) {
            in.resetReaderIndex();
            return;
        }
        
        try {
            // 读取消息内容
            byte[] bytes = new byte[messageLength];
            in.readBytes(bytes);
            
            // 解析消息
            Message message = MessageFactory.createFromProtoBytes(bytes);
            
            // 添加到输出列表
            out.add(message);
            
            LOGGER.debug("解码消息成功: type={}, id={}, timestamp={}", 
                    message.getType(), message.getId(), message.getTimestamp());
        } catch (Exception e) {
            LOGGER.error("解码消息失败", e);
            ctx.fireExceptionCaught(e);
        }
    }
} 