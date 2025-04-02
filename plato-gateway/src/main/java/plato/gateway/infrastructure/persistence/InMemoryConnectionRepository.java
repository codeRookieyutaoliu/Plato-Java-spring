package plato.gateway.infrastructure.persistence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import plato.gateway.infrastructure.connection.NIOConnection;
import plato.gateway.domain.repository.ConnectionRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 基于内存的连接仓储实现
 * 使用 ConcurrentHashMap 存储连接信息，确保线程安全
 */
@Slf4j
@Repository
public class InMemoryConnectionRepository implements ConnectionRepository {
    private final ConcurrentHashMap<Long, NIOConnection> connections = new ConcurrentHashMap<>();
    private final AtomicLong connectionIdGenerator = new AtomicLong(0);

    @Override
    public NIOConnection save(NIOConnection NIOConnection) {
        connections.put(NIOConnection.getId(), NIOConnection);
        log.debug("Saved connection: id={}, client={}:{}", 
            NIOConnection.getId(), NIOConnection.getClientIp(), NIOConnection.getClientPort());
        return NIOConnection;
    }

    @Override
    public Optional<NIOConnection> findById(long id) {
        return Optional.ofNullable(connections.get(id));
    }

    @Override
    public List<NIOConnection> findAllActive() {
        return connections.values().stream()
            .filter(NIOConnection::isActive)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(long id) {
        NIOConnection NIOConnection = connections.remove(id);
        if (NIOConnection != null) {
            log.debug("Deleted connection: id={}, client={}:{}", 
                id, NIOConnection.getClientIp(), NIOConnection.getClientPort());
        }
    }

    @Override
    public int deleteIdleConnections(int maxIdleTimeSeconds) {
        List<Long> idleConnectionIds = connections.values().stream()
            .filter(connection -> connection.isIdleTimeout(maxIdleTimeSeconds))
            .map(NIOConnection::getId)
            .collect(Collectors.toList());

        idleConnectionIds.forEach(this::delete);
        return idleConnectionIds.size();
    }

    @Override
    public long countActiveConnections() {
        return connections.values().stream()
            .filter(NIOConnection::isActive)
            .count();
    }

    /**
     * 生成新的连接ID
     *
     * @return 连接ID
     */
    public long generateConnectionId() {
        return connectionIdGenerator.incrementAndGet();
    }

    /**
     * 获取所有连接数量（包括非活跃连接）
     *
     * @return 连接总数
     */
    public long getTotalConnectionCount() {
        return connections.size();
    }

    /**
     * 获取指定IP的连接数量
     *
     * @param clientIp 客户端IP
     * @return 连接数量
     */
    public long getConnectionCountByIp(String clientIp) {
        return connections.values().stream()
            .filter(connection -> connection.getClientIp().equals(clientIp))
            .count();
    }
} 