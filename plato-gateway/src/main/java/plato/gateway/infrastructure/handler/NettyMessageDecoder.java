package plato.gateway.infrastructure.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import plato.common.handler.MessageDispatcher;
import plato.gateway.domain.model.Message;
import plato.gateway.infrastructure.connection.NettyConnection;

import java.util.List;

/**
 * Netty 消息解码器
 * 将 ByteBuf 解码为 Message 对象，并分发到消息处理器
 */
@Slf4j
public class NettyMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    // 消息分发器
    private final MessageDispatcher messageDispatcher;

    /**
     * 构造函数
     *
     * @param messageDispatcher 消息分发器
     */
    public NettyMessageDecoder(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    /**
     * 解码消息
     *
     * @param ctx 通道处理上下文
     * @param in 输入的 ByteBuf
     * @param out 输出的消息列表
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            // 读取消息内容
            byte[] payload = new byte[in.readableBytes()];
            in.readBytes(payload);
            
            // 创建消息对象
            Message message = new Message();
            message.setPayload(payload);
            
            // 添加到输出列表
            out.add(message);
            
            // 获取连接ID
            Long connectionId = ctx.channel().attr(NettyConnection.CONNECTION_ID_KEY).get();
            if (connectionId != null) {
                // 分发消息到处理器
                messageDispatcher.dispatch(connectionId, payload);
            } else {
                log.warn("连接ID为空，无法分发消息");
            }
            
            log.debug("解码消息: length={}", payload.length);
        } catch (Exception e) {
            log.error("解码消息失败", e);
        }
    }
} 