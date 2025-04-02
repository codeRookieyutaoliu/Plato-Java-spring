package plato.client.infrastructure.adapter;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import plato.common.message.AckMessage;
import plato.common.message.CmdType;
import plato.common.message.MsgCmd;
import plato.common.sdk.ChatConnection;
import plato.common.sdk.Message;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Netty实现的聊天连接
 * 使用Netty框架进行网络通信
 */
@Slf4j
public class NettyChatConnection implements ChatConnection {
    private final String host;
    private final int port;
    private final EventLoopGroup group;
    private Channel channel;
    private final AtomicLong connectionId = new AtomicLong(0);
    private final ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();
    private Consumer<Message> messageConsumer;
    private volatile boolean connected = false;

    /**
     * 构造函数
     *
     * @param host 服务器主机名
     * @param port 服务器端口
     */
    public NettyChatConnection(String host, int port) {
        this.host = host;
        this.port = port;
        this.group = new NioEventLoopGroup();
    }

    /**
     * 连接到服务器
     *
     * @return 是否连接成功
     */
    public boolean connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加心跳检测处理器
                            pipeline.addLast(new IdleStateHandler(30, 10, 0, TimeUnit.SECONDS));
                            // 添加长度字段解码器
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 4));
                            // 添加长度字段编码器
                            pipeline.addLast(new LengthFieldPrepender(4));
                            // 添加消息处理器
                            pipeline.addLast(new MessageHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            if (future.isSuccess()) {
                channel = future.channel();
                connected = true;
                log.info("连接到服务器 {}:{} 成功", host, port);
                return true;
            } else {
                log.error("连接到服务器 {}:{} 失败", host, port);
                return false;
            }
        } catch (Exception e) {
            log.error("连接到服务器 {}:{} 异常", host, port, e);
            return false;
        }
    }

    @Override
    public boolean send(Message message) {
        if (!isConnected()) {
            log.warn("未连接到服务器，无法发送消息");
            return false;
        }

        try {
            // 将SDK消息转换为Protobuf消息
            MsgCmd msgCmd = MessageConverter.toProtobuf(message);
            channel.writeAndFlush(msgCmd);
            log.debug("发送消息: {}", message);
            return true;
        } catch (Exception e) {
            log.error("发送消息异常", e);
            return false;
        }
    }

    @Override
    public void receive(Consumer<Message> messageConsumer) {
        this.messageConsumer = messageConsumer;
        // 处理已经接收到但尚未处理的消息
        Message message;
        while ((message = messageQueue.poll()) != null) {
            messageConsumer.accept(message);
        }
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
        connected = false;
        log.info("关闭连接");
    }

    @Override
    public long getConnectionId() {
        return connectionId.get();
    }

    @Override
    public void setConnectionId(long connectionId) {
        this.connectionId.set(connectionId);
    }

    @Override
    public boolean isConnected() {
        return connected && channel != null && channel.isActive();
    }

    /**
     * Netty消息处理器
     */
    private class MessageHandler extends SimpleChannelInboundHandler<MsgCmd> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MsgCmd msg) {
            // 检查是否需要更新连接ID
            long connId = MessageConverter.getConnectionIdFromAck(msg);
            if (connId > 0) {
                setConnectionId(connId);
            }
            
            // 将Protobuf消息转换为SDK消息
            Message message = MessageConverter.fromProtobuf(msg);
            log.debug("接收到消息: {}", message);
            
            // 如果有消息消费者，直接处理消息
            if (messageConsumer != null) {
                messageConsumer.accept(message);
            } else {
                // 否则，将消息加入队列，等待消费者设置后处理
                messageQueue.offer(message);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            log.info("通道激活: {}", ctx.channel().remoteAddress());
            connected = true;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            log.info("通道关闭: {}", ctx.channel().remoteAddress());
            connected = false;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("通道异常", cause);
            ctx.close();
            connected = false;
        }
    }
} 