package plato.gateway.domain.repository;

import plato.gateway.infrastructure.connection.NIOConnection;

import java.util.List;
import java.util.Optional;

/**
 * 连接仓储接口
 * 负责连接的存储和检索
 */
public interface ConnectionRepository {
    /**
     * 保存连接
     *
     * @param NIOConnection 连接
     * @return 保存后的连接
     */
    NIOConnection save(NIOConnection NIOConnection);

    /**
     * 根据ID查找连接
     *
     * @param id 连接ID
     * @return 连接
     */
    Optional<NIOConnection> findById(long id);

    /**
     * 查找所有活跃连接
     *
     * @return 活跃连接列表
     */
    List<NIOConnection> findAllActive();

    /**
     * 删除连接
     *
     * @param id 连接ID
     */
    void delete(long id);

    /**
     * 删除所有空闲超时的连接
     *
     * @param maxIdleTimeSeconds 最大空闲时间（秒）
     * @return 删除的连接数量
     */
    int deleteIdleConnections(int maxIdleTimeSeconds);

    /**
     * 获取活跃连接数量
     *
     * @return 活跃连接数量
     */
    long countActiveConnections();
} 