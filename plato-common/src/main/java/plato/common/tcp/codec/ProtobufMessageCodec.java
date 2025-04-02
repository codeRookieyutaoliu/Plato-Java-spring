package plato.common.tcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import plato.common.message.MsgCmd;

import java.util.List;

/**
 * Protobuf消息编解码器
 * 负责将Protobuf消息编码为ByteBuf，以及将ByteBuf解码为Protobuf消息
 */
public class ProtobufMessageCodec extends MessageToMessageCodec<ByteBuf, MsgCmd> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MsgCmd msg, List<Object> out) {
        // 将Protobuf消息编码为ByteBuf
        ByteBuf encoded = ctx.alloc().buffer();
        byte[] bytes = msg.toByteArray();
        encoded.writeBytes(bytes);
        out.add(encoded);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        // 将ByteBuf解码为Protobuf消息
        byte[] array;
        int offset;
        int length = msg.readableBytes();

        if (msg.hasArray()) {
            array = msg.array();
            offset = msg.arrayOffset() + msg.readerIndex();
        } else {
            array = new byte[length];
            msg.getBytes(msg.readerIndex(), array, 0, length);
            offset = 0;
        }

        try {
            byte[] messageBytes = new byte[length];
            msg.getBytes(msg.readerIndex(), messageBytes, 0, length);
            MsgCmd decoded = MsgCmd.parseFrom(messageBytes);
            out.add(decoded);
        } catch (Exception e) {
            ctx.fireExceptionCaught(e);
        }
    }
}