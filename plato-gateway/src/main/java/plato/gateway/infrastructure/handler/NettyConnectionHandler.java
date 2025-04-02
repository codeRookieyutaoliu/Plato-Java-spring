package plato.gateway.infrastructure.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import plato.gateway.infrastructure.connection.NettyConnection;
import plato.gateway.infrastructure.connection.ConnectionIdGenerator;
import plato.gateway.infrastructure.connection.ConnectionTable;
import plato.gateway.infrastructure.workpool.WorkPool;

import java.net.InetSocketAddress;

/**
 * Netty 连接处理器
 * 处理 Netty 连接事件
 */
@Slf4j
public class NettyConnectionHandler extends ChannelInboundHandlerAdapter {

    // 连接表
    private final ConnectionTable connectionTable;
    
    // 连接ID生成器
    private final ConnectionIdGenerator connectionIdGenerator;
    
    // 工作池
    private final WorkPool workPool;

    /**
     * 构造函数
     *
     * @param connectionTable 连接表
     * @param connectionIdGenerator 连接ID生成器
     * @param workPool 工作池
     */
    public NettyConnectionHandler(ConnectionTable connectionTable, ConnectionIdGenerator connectionIdGenerator, WorkPool workPool) {
        this.connectionTable = connectionTable;
        this.connectionIdGenerator = connectionIdGenerator;
        this.workPool = workPool;
    }

    /**
     * 通道激活事件
     * 当连接建立时调用
     *
     * @param ctx 通道处理上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        
        // 生成连接ID
        long connectionId = connectionIdGenerator.nextId();
        
        // 创建连接对象
        NettyConnection connection = new NettyConnection(connectionId, channel, remoteAddress.toString());
        
        // 添加到连接表
        connectionTable.add(connection);
        
        log.info("新连接: id={}, remoteAddress={}", connectionId, remoteAddress);
        
        // 提交到工作池处理
        workPool.submit(() -> processConnection(connection));
        
        // 将连接ID保存到通道属性中
        channel.attr(NettyConnection.CONNECTION_ID_KEY).set(connectionId);
        
        // 调用父类方法
        ctx.fireChannelActive();
    }

    /**
     * 通道不活跃事件
     * 当连接关闭时调用
     *
     * @param ctx 通道处理上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Long connectionId = channel.attr(NettyConnection.CONNECTION_ID_KEY).get();
        
        if (connectionId != null) {
            log.info("连接关闭: id={}, remoteAddress={}", connectionId, channel.remoteAddress());
            
            // 从连接表中删除
            connectionTable.remove(connectionId);
        }
        
        // 调用父类方法
        ctx.fireChannelInactive();
    }

    /**
     * 用户事件触发
     * 处理空闲连接检测
     *
     * @param ctx 通道处理上下文
     * @param evt 事件对象
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                Long connectionId = channel.attr(NettyConnection.CONNECTION_ID_KEY).get();
                
                if (connectionId != null) {
                    log.info("关闭空闲连接: id={}, remoteAddress={}", connectionId, channel.remoteAddress());
                    
                    // 从连接表中删除
                    connectionTable.remove(connectionId);
                    
                    // 关闭连接
                    ctx.close();
                }
            }
        } else {
            // 调用父类方法
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * 异常捕获
     *
     * @param ctx 通道处理上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        Long connectionId = channel.attr(NettyConnection.CONNECTION_ID_KEY).get();
        
        if (connectionId != null) {
            log.error("连接异常: id={}, remoteAddress={}, cause={}", connectionId, channel.remoteAddress(), cause.getMessage());
        } else {
            log.error("连接异常: remoteAddress={}, cause={}", channel.remoteAddress(), cause.getMessage());
        }
        
        // 关闭连接
        ctx.close();
    }

    /**
     * 处理连接
     * 可以在这里添加连接处理逻辑，例如认证、协议解析等
     *
     * @param connection 连接对象
     */
    private void processConnection(NettyConnection connection) {
        log.debug("处理连接: id={}", connection.getId());
        
        // 这里可以添加连接处理逻辑
        // 例如：认证、协议解析等
    }
} 