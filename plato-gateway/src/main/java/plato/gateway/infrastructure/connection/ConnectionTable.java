package plato.gateway.infrastructure.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 连接表管理
 * 对应Go版本的table结构体
 */
@Slf4j
@Component
public class ConnectionTable {
    
    // 连接表，对应Go版本的conns
    private final Map<Long, IConnection> connections = new ConcurrentHashMap<>();
    
    // 连接数量，对应Go版本的count
    private final AtomicInteger count = new AtomicInteger(0);
    
    // 读写锁，对应Go版本的sync.RWMutex
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * 添加连接
     * 对应Go版本的add方法
     *
     * @param connection 连接对象
     */
    public void add(IConnection connection) {
        lock.writeLock().lock();
        try {
            connections.put(connection.getId(), connection);
            count.incrementAndGet();
            log.info("添加连接: {}, 当前连接数: {}", connection.getId(), count.get());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 删除连接
     * 对应Go版本的remove方法
     *
     * @param connectionId 连接ID
     * @return 被删除的连接，如果不存在则返回null
     */
    public IConnection remove(long connectionId) {
        lock.writeLock().lock();
        try {
            IConnection connection = connections.remove(connectionId);
            if (connection != null) {
                count.decrementAndGet();
                log.info("删除连接: {}, 当前连接数: {}", connectionId, count.get());
            }
            return connection;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取连接
     * 对应Go版本的get方法
     *
     * @param connectionId 连接ID
     * @return 连接对象，如果不存在则返回null
     */
    public IConnection get(long connectionId) {
        lock.readLock().lock();
        try {
            return connections.get(connectionId);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取所有连接
     * 对应Go版本的getAll方法
     *
     * @return 所有连接的列表
     */
    public List<IConnection> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(connections.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取连接数量
     * 对应Go版本的len方法
     *
     * @return 连接数量
     */
    public int size() {
        return count.get();
    }
    
    /**
     * 清空连接表
     * 对应Go版本的clear方法
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            // 关闭所有连接
            for (IConnection connection : connections.values()) {
                connection.close();
            }
            
            // 清空连接表
            connections.clear();
            count.set(0);
            log.info("清空连接表");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 关闭连接
     *
     * @param connectionId 连接ID
     * @return 是否成功
     */
    public boolean closeConnection(long connectionId) {
        IConnection connection = get(connectionId);
        if (connection != null) {
            log.info("关闭连接: id={}, remoteAddress={}", connectionId, connection.getRemoteAddress());
            
            // 从连接表中删除
            remove(connectionId);
            
            // 关闭连接
            connection.close();
            
            return true;
        } else {
            log.warn("连接不存在: id={}", connectionId);
            return false;
        }
    }
    
    /**
     * 发送消息
     *
     * @param connectionId 连接ID
     * @param payload 消息内容
     * @return 是否成功
     */
    public boolean sendMessage(long connectionId, byte[] payload) {
        IConnection connection = get(connectionId);
        if (connection != null) {
            log.debug("发送消息: id={}, length={}", connectionId, payload.length);
            return connection.sendMessage(payload);
        } else {
            log.warn("连接不存在: id={}", connectionId);
            return false;
        }
    }
}