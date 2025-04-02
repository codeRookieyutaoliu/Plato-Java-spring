package plato.gateway.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plato.gateway.infrastructure.connection.NIOConnection;
import plato.gateway.domain.repository.ConnectionRepository;

import java.util.List;

/**
 * 连接领域服务
 * 处理连接相关的业务逻辑
 */
@Slf4j
@Service
public class ConnectionDomainService {
    private final ConnectionRepository connectionRepository;

    public ConnectionDomainService(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    /**
     * 创建新连接
     *
     * @param NIOConnection 连接
     * @return 创建后的连接
     */
    public NIOConnection createConnection(NIOConnection NIOConnection) {
        log.info("Creating new connection: client={}:{}", NIOConnection.getClientIp(), NIOConnection.getClientPort());
        return connectionRepository.save(NIOConnection);
    }

    /**
     * 关闭连接
     *
     * @param connectionId 连接ID
     */
    public void closeConnection(long connectionId) {
        connectionRepository.findById(connectionId).ifPresent(connection -> {
            connection.close();
            connectionRepository.delete(connectionId);
            log.info("Connection closed: id={}", connectionId);
        });
    }

    /**
     * 清理空闲连接
     *
     * @param maxIdleTimeSeconds 最大空闲时间（秒）
     * @return 清理的连接数量
     */
    public int cleanupIdleConnections(int maxIdleTimeSeconds) {
        List<NIOConnection> activeNIOConnections = connectionRepository.findAllActive();
        int closedCount = 0;

        for (NIOConnection NIOConnection : activeNIOConnections) {
            if (NIOConnection.isIdleTimeout(maxIdleTimeSeconds)) {
                NIOConnection.close();
                connectionRepository.delete(NIOConnection.getId());
                closedCount++;
                log.info("Idle connection closed: id={}, client={}:{}", 
                    NIOConnection.getId(), NIOConnection.getClientIp(), NIOConnection.getClientPort());
            }
        }

        return closedCount;
    }

    /**
     * 获取活跃连接数量
     *
     * @return 活跃连接数量
     */
    public long getActiveConnectionCount() {
        return connectionRepository.countActiveConnections();
    }

    /**
     * 获取所有活跃连接
     *
     * @return 活跃连接列表
     */
    public List<NIOConnection> getAllActiveConnections() {
        return connectionRepository.findAllActive();
    }

    /**
     * 根据ID查找连接
     *
     * @param connectionId 连接ID
     * @return 连接
     */
    public NIOConnection findConnection(long connectionId) {
        return connectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));
    }
} 