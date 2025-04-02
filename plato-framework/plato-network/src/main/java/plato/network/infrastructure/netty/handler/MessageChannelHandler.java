package plato.network.infrastructure.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.application.service.MessageProcessingService;
import plato.message.domain.entity.Message;
import plato.network.domain.entity.Connection;
import plato.network.domain.service.ConnectionManager;
import plato.network.domain.valueobject.ConnectionStatistics;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 消息通道处理器
 * <p>
 * 处理连接建立、断开和消息接收事件
 * </p>
 * 
 * 对应Go项目中的网络消息处理逻辑
 */
@ChannelHandler.Sharable
public class MessageChannelHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageChannelHandler.class);
    
    /**
     * 连接ID属性键
     */
    private static final AttributeKey<Long> CONNECTION_ID_KEY = AttributeKey.valueOf("connectionId");
    
    /**
     * 连接管理器
     */
    private final ConnectionManager connectionManager;
    
    /**
     * 消息处理服务
     */
    private final MessageProcessingService messageProcessingService;
    
    /**
     * 连接统计信息
     */
    private final ConnectionStatistics statistics;
    
    /**
     * 构造函数
     *
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     * @param statistics 连接统计信息
     */
    public MessageChannelHandler(ConnectionManager connectionManager, 
                               MessageProcessingService messageProcessingService,
                               ConnectionStatistics statistics) {
        this.connectionManager = connectionManager;
        this.messageProcessingService = messageProcessingService;
        this.statistics = statistics;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        
        // 创建连接对象
        Connection connection = new Connection(remoteAddress, localAddress);
        long connectionId = connection.getId();
        
        // 注册连接
        if (connectionManager.registerConnection(connection)) {
            // 将连接ID设置为通道属性
            channel.attr(CONNECTION_ID_KEY).set(connectionId);
            
            LOGGER.info("连接建立: connectionId={}, remote={}", connectionId, remoteAddress);
        } else {
            LOGGER.error("注册连接失败: remote={}", remoteAddress);
            ctx.close();
            return;
        }
        
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        Long connectionId = channel.attr(CONNECTION_ID_KEY).get();
        
        if (connectionId != null) {
            // 移除连接
            if (connectionManager.removeConnection(connectionId)) {
                LOGGER.info("连接断开: connectionId={}, remote={}", connectionId, channel.remoteAddress());
            } else {
                LOGGER.error("移除连接失败: connectionId={}", connectionId);
            }
        } else {
            LOGGER.warn("连接断开但找不到连接ID: remote={}", channel.remoteAddress());
        }
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            // 记录接收消息统计
            statistics.incrementReceivedMessages();
            
            if (!(msg instanceof Message)) {
                LOGGER.error("收到非消息对象: {}", msg.getClass().getName());
                return;
            }
            
            Message message = (Message) msg;
            Channel channel = ctx.channel();
            Long connectionId = channel.attr(CONNECTION_ID_KEY).get();
            
            if (connectionId == null) {
                LOGGER.error("找不到连接ID, 消息: type={}, id={}", message.getType(), message.getId());
                return;
            }
            
            // 获取连接
            Connection connection = connectionManager.getConnection(connectionId);
            if (connection == null) {
                LOGGER.warn("未找到与通道关联的连接: {}", ctx.channel().remoteAddress());
                return;
            }
            
            // 更新连接活动时间
            connection.updateLastActiveTime();
            
            LOGGER.debug("收到消息: connectionId={}, type={}, id={}", connectionId, message.getType(), message.getId());
            
            // 处理消息
            Object response = messageProcessingService.processMessage(message, connection);
            
            // 发送响应
            if (response != null) {
                ctx.writeAndFlush(response);
                
                // 记录发送消息统计
                statistics.incrementSentMessages();
                
                LOGGER.debug("发送响应: connectionId={}, type={}, id={}", 
                        connectionId, response.getClass().getSimpleName(), ((Message) response).getId());
            }
        } catch (Exception e) {
            LOGGER.error("处理消息时发生异常", e);
            
            // 记录失败消息统计
            statistics.incrementFailedMessages();
            
            throw e;
        } finally {
            // 释放消息
            ReferenceCountUtil.release(msg);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        Long connectionId = channel.attr(CONNECTION_ID_KEY).get();
        
        LOGGER.error("通道异常: connectionId={}, remote={}", connectionId, channel.remoteAddress(), cause);
        
        // 更新统计信息
        statistics.incrementFailedMessages();
        
        // 关闭连接
        ctx.close();
    }
} 