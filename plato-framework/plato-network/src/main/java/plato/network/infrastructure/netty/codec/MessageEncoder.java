package plato.network.infrastructure.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.domain.entity.Message;
import plato.message.domain.factory.MessageFactory;

/**
 * 消息编码器
 * <p>
 * 将消息对象编码为字节流
 * </p>
 * 
 * 对应Go项目中的消息编码逻辑
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageEncoder.class);
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
        try {
            // 将消息对象转换为字节数组
            byte[] messageBytes = MessageFactory.createProtoBytes(message);
            
            // 写入消息长度
            out.writeInt(messageBytes.length);
            
            // 写入消息内容
            out.writeBytes(messageBytes);
            
            LOGGER.debug("编码消息成功: type={}, id={}, length={}", 
                    message.getType(), message.getId(), messageBytes.length);
        } catch (Exception e) {
            LOGGER.error("编码消息失败: {}", message.getType(), e);
            throw e;
        }
    }
} 