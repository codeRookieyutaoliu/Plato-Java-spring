package plato.common.config;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 配置服务接口
 * 定义配置管理的核心功能，包括配置获取、监听和动态更新
 */
public interface ConfigService {
    /**
     * 获取配置项
     * @param key 配置键
     * @return 配置值
     */
    String getConfig(String key);

    /**
     * 获取指定命名空间下的配置项
     * @param namespace 命名空间
     * @param key 配置键
     * @return 配置值
     */
    String getConfig(String namespace, String key);

    /**
     * 获取指定命名空间下的所有配置
     * @param namespace 命名空间
     * @return 配置映射
     */
    Map<String, String> getConfigs(String namespace);

    /**
     * 添加配置变更监听器
     * @param key 配置键
     * @param listener 变更监听器
     * @return 监听器ID
     */
    String addListener(String key, Consumer<ConfigChangeEvent> listener);

    /**
     * 移除配置变更监听器
     * @param listenerId 监听器ID
     */
    void removeListener(String listenerId);

    /**
     * 配置变更事件
     */
    interface ConfigChangeEvent {
        /**
         * 获取配置键
         * @return 配置键
         */
        String getKey();

        /**
         * 获取命名空间
         * @return 命名空间
         */
        String getNamespace();

        /**
         * 获取变更前的值
         * @return 旧值
         */
        String getOldValue();

        /**
         * 获取变更后的值
         * @return 新值
         */
        String getNewValue();

        /**
         * 获取变更类型
         * @return 变更类型
         */
        ChangeType getChangeType();
    }

    /**
     * 配置变更类型
     */
    enum ChangeType {
        ADDED,    // 新增配置
        MODIFIED, // 修改配置
        DELETED   // 删除配置
    }
}