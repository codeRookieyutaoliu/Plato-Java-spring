package plato.common.router;

import java.util.List;
import java.util.Optional;

/**
 * 路由表接口
 * 定义路由规则管理和路由查找的基本操作
 */
public interface Router<K, V> {
    /**
     * 添加路由规则
     * @param key 路由键
     * @param value 路由值
     * @return 是否添加成功
     */
    boolean addRoute(K key, V value);

    /**
     * 删除路由规则
     * @param key 路由键
     * @return 是否删除成功
     */
    boolean removeRoute(K key);

    /**
     * 查找路由
     * @param key 路由键
     * @return 路由值
     */
    Optional<V> findRoute(K key);

    /**
     * 获取所有路由规则
     * @return 路由规则列表
     */
    List<RouteRule<K, V>> getAllRoutes();

    /**
     * 清空路由表
     */
    void clear();

    /**
     * 获取路由表大小
     * @return 路由规则数量
     */
    int size();
}