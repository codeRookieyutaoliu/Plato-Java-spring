package plato.gateway.infrastructure.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import plato.gateway.domain.repository.ConnectionRepository;
import plato.gateway.infrastructure.config.GatewayConfig;


import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty 连接工厂
 * 用于创建基于 Netty 的连接
 * 用于创建基于 Netty 的连接，并管理连接
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyConnectionFactory implements ConnectionFactory {

    private final ConnectionRepository connectionRepository;
    private final GatewayConfig gatewayConfig;

    // 连接ID到Channel的映射
    private final Map<Long, Channel> idToChannelMap = new ConcurrentHashMap<>();
    // Channel ID到连接ID的映射
    private final Map<ChannelId, Long> channelToIdMap = new ConcurrentHashMap<>();

    /**
     * 创建连接
     *
     * @param connectionId 连接ID
     * @param remoteAddress 远程地址
     * @param connectionData 连接数据（Channel）
     * @return 连接对象
     */
    @Override
    public IConnection createConnection(long connectionId, String remoteAddress, Object connectionData) {
        if (!(connectionData instanceof Channel)) {
            throw new IllegalArgumentException("connectionData must be a Channel");
        }


        Channel channel = (Channel) connectionData;
        return new NettyConnection(connectionId, channel, remoteAddress);

        // 检查连接数限制
        if (connectionRepository.countActiveConnections() >= gatewayConfig.getMaxConnections()) {
            log.warn("达到最大连接数限制: {}", gatewayConfig.getMaxConnections());
            return null;
        }

        // 创建连接对象
        Conn

        // 保存连接
        connectionRepository.save(conn);

        // 更新映射关系
        idToChannelMap.put(conn.getId(), channel);
        channelToIdMap.put(channel.id(), conn.getId());

        log.info("创建新连接, ID: {}, 远程地址: {}", conn.getId(), remoteAddress);

        return conn;
    }

    /**
     * 关闭连接
     *
     * @param connectionId 连接ID
     */
    public void closeConnection(long connectionId) {
        Optional<IConnection> connection = connectionRepository.findById(connectionId);
        if (connection.isPresent()) {
            connection.get().close();
            connectionRepository.delete(connectionId);
            log.info("关闭连接: {}", connectionId);

            // 移除映射关系
            Channel channel = idToChannelMap.remove(connectionId);
            if (channel != null) {
                channelToIdMap.remove(channel.id());
            }
        }
    }

    /**
     * 清理空闲连接
     *
     * @param maxIdleTime 最大空闲时间
     * @return 清理的连接数
     */
    public int cleanupIdleConnections(Duration maxIdleTime) {
        List<IConnection> idleConnections = connectionRepository.findAllActive()
                .stream()
                .filter(conn -> conn.isIdle(maxIdleTime))
                .toList();

        idleConnections.forEach(conn -> {
            conn.close();
            connectionRepository.delete(conn.getId());
            log.info("清理空闲连接: {}", conn.getId());

            // 移除映射关系
            Channel channel = idToChannelMap.remove(conn.getId());
            if (channel != null) {
                channelToIdMap.remove(channel.id());
            }
        });

        return idleConnections.size();
    }

    /**
     * 更新连接最后活动时间
     *
     * @param connectionId 连接ID
     */
    public void updateLastActiveTime(long connectionId) {
        Optional<IConnection> connection = connectionRepository.findById(connectionId);
        if (connection.isPresent()) {
            connection.get().updateLastActiveTime();
            connectionRepository.save(connection.get());
        }
    }

    /**
     * 发送消息
     *
     * @param connectionId 连接ID
     * @param payload 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(long connectionId, byte[] payload) {
        Channel channel = idToChannelMap.get(connectionId);
        if (channel == null) {
            log.warn("连接不存在，ID：{}", connectionId);
            return false;
        }

        if (!channel.isActive()) {
            log.warn("连接不活跃，ID：{}", connectionId);
            return false;
        }

        try {
            channel.writeAndFlush(payload).sync();
            log.debug("发送消息成功，ID：{}，长度：{}", connectionId, payload.length);
            return true;
        } catch (Exception e) {
            log.error("发送消息失败，ID：{}", connectionId, e);
            return false;
        }
    }

    /**
     * 注册连接
     *
     * @param connectionId 连接ID
     * @param channel Netty Channel
     */
    public void registerConnection(long connectionId, Channel channel) {
        idToChannelMap.put(connectionId, channel);
        channelToIdMap.put(channel.id(), connectionId);
        log.info("注册连接，ID：{}，Channel：{}", connectionId, channel.id());
    }

    /**
     * 获取连接ID
     *
     * @param channel Netty Channel
     * @return 连接ID
     */
    public Long getConnectionId(Channel channel) {
        return channelToIdMap.get(channel.id());
    }

    /**
     * 获取Channel
     *
     * @param connectionId 连接ID
     * @return Netty Channel
     */
    public Channel getChannel(long connectionId) {
        return idToChannelMap.get(connectionId);
    }

    /**
     * 移除连接
     *
     * @param channel Netty Channel
     */
    public void removeConnection(Channel channel) {
        Long connectionId = channelToIdMap.remove(channel.id());
        if (connectionId != null) {
            idToChannelMap.remove(connectionId);
            log.info("移除连接，ID：{}，Channel：{}", connectionId, channel.id());
        }
    }
}

