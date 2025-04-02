package plato.gateway.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plato.gateway.infrastructure.connection.NIOConnection;
import plato.gateway.domain.service.ConnectionDomainService;
import plato.gateway.infrastructure.adapter.netty.NettyConnectionManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 连接应用服务
 * 处理连接相关的应用层逻辑
 */
@Slf4j
@Service
public class ConnectionService {
    private final ConnectionDomainService connectionDomainService;
    private final NettyConnectionManager nettyConnectionManager;

    public ConnectionService(ConnectionDomainService connectionDomainService,
                           NettyConnectionManager nettyConnectionManager) {
        this.connectionDomainService = connectionDomainService;
        this.nettyConnectionManager = nettyConnectionManager;
    }

    /**
     * 创建新连接
     *
     * @param channel    Netty通道
     * @param clientIp   客户端IP
     * @param clientPort 客户端端口
     * @return 连接ID
     */
    public long createConnection(io.netty.channel.Channel channel, String clientIp, int clientPort) {
        NIOConnection NIOConnection = nettyConnectionManager.createConnection((NIOConnection) channel, clientIp, clientPort);
        NIOConnection savedNIOConnection = connectionDomainService.createConnection(NIOConnection);
        return savedNIOConnection.getId();
    }

    /**
     * 关闭连接
     *
     * @param connectionId 连接ID
     */
    public void closeConnection(long connectionId) {
        connectionDomainService.closeConnection(connectionId);
    }

    /**
     * 发送消息到连接
     *
     * @param connectionId 连接ID
     * @param payload     消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(long connectionId, byte[] payload) {
        NIOConnection NIOConnection = connectionDomainService.findConnection(connectionId);
        return NIOConnection.sendMessage(payload);
    }

    /**
     * 广播消息到所有活跃连接
     *
     * @param payload 消息内容
     * @return 成功发送的连接数量
     */
    public int broadcastMessage(byte[] payload) {
        List<NIOConnection> activeNIOConnections = connectionDomainService.getAllActiveConnections();
        int successCount = 0;

        for (NIOConnection NIOConnection : activeNIOConnections) {
            if (NIOConnection.sendMessage(payload)) {
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * 获取连接统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getConnectionStats() {
        long activeCount = connectionDomainService.getActiveConnectionCount();
        List<NIOConnection> NIOConnections = connectionDomainService.getAllActiveConnections();

        return Map.of(
            "activeConnections", activeCount,
            "connectionsByIp", NIOConnections.stream()
                .collect(Collectors.groupingBy(
                    NIOConnection::getRemoteAddress,
                    Collectors.counting()
                ))
        );
    }

    /**
     * 清理空闲连接
     *
     * @param maxIdleTimeSeconds 最大空闲时间（秒）
     * @return 清理的连接数量
     */
    public int cleanupIdleConnections(int maxIdleTimeSeconds) {
        return connectionDomainService.cleanupIdleConnections(maxIdleTimeSeconds);
    }
} 