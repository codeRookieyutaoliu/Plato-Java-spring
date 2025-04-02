package plato.gateway.infrastructure.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import plato.gateway.domain.model.Message;

import java.util.List;

/**
 * Netty 消息编码器
 * 将 Message 对象编码为 ByteBuf
 */
@Slf4j
public class NettyMessageEncoder extends MessageToMessageEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, List<Object> out) {
        try {
            // 获取消息内容
            byte[] payload = message.getPayload();
            
            // 创建 ByteBuf
            ByteBuf byteBuf = Unpooled.wrappedBuffer(payload);
            
            // 添加到输出列表
            out.add(byteBuf);
            
            log.debug("编码消息: length={}", payload.length);
        } catch (Exception e) {
            log.error("编码消息失败", e);
        }
    }
} 