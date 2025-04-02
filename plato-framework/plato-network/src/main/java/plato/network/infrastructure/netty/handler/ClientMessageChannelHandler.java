package plato.network.infrastructure.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.application.service.MessageProcessingService;
import plato.message.domain.entity.HeartbeatMessage;
import plato.message.domain.entity.Message;
import plato.message.domain.factory.MessageFactory;

/**
 * 客户端消息通道处理器
 * <p>
 * 处理客户端的消息接收和心跳事件
 * </p>
 * 
 * 对应Go项目中的客户端消息处理逻辑
 */
@ChannelHandler.Sharable
public class ClientMessageChannelHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessageChannelHandler.class);
    
    /**
     * 消息处理服务
     */
    private final MessageProcessingService messageProcessingService;
    
    /**
     * 构造函数
     *
     * @param messageProcessingService 消息处理服务
     */
    public ClientMessageChannelHandler(MessageProcessingService messageProcessingService) {
        this.messageProcessingService = messageProcessingService;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("连接到服务器: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("与服务器断开连接: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Message)) {
            LOGGER.warn("收到非消息对象: {}", msg.getClass().getName());
            return;
        }
        
        Message message = (Message) msg;
        LOGGER.debug("从服务器收到消息: type={}, id={}", message.getType(), message.getId());
        
        // 异步处理消息
        messageProcessingService.processMessage(message).thenAccept(results -> {
            // 处理响应消息
            if (results != null && !results.isEmpty()) {
                for (Object result : results) {
                    if (result instanceof Message) {
                        // 发送响应消息
                        ctx.channel().writeAndFlush((Message) result);
                    }
                }
            }
        }).exceptionally(ex -> {
            LOGGER.error("处理消息异常: type={}, id={}", message.getType(), message.getId(), ex);
            return null;
        });
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 发送心跳消息
            sendHeartbeat(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("通道异常", cause);
        ctx.close();
    }
    
    /**
     * 发送心跳消息
     *
     * @param ctx 通道上下文
     */
    private void sendHeartbeat(ChannelHandlerContext ctx) {
        try {
            // 创建心跳消息
            HeartbeatMessage heartbeatMessage = MessageFactory.createHeartbeatMessage("unknown", "unknown");
            
            // 发送心跳消息
            ctx.channel().writeAndFlush(heartbeatMessage);
            
            LOGGER.debug("发送心跳消息: id={}", heartbeatMessage.getId());
        } catch (Exception e) {
            LOGGER.error("发送心跳消息失败", e);
        }
    }
} 