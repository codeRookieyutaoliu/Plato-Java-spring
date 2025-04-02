package plato.common.router;

/**
 * 路由规则接口
 * 定义路由规则的基本属性和匹配方法
 */
public interface RouteRule<K, V> {
    /**
     * 获取路由键
     * @return 路由键
     */
    K getKey();

    /**
     * 获取路由值
     * @return 路由值
     */
    V getValue();

    /**
     * 获取规则优先级
     * @return 优先级，数值越大优先级越高
     */
    int getPriority();

    /**
     * 判断是否匹配给定的键
     * @param key 待匹配的键
     * @return 是否匹配
     */
    boolean matches(K key);

    /**
     * 获取规则描述
     * @return 规则描述
     */
    String getDescription();
}