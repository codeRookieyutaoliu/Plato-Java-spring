package plato.common.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Nacos配置服务实现类
 * 基于Nacos实现配置的获取和动态更新
 */
@Slf4j
@Component
public class NacosConfigService implements plato.common.config.ConfigService {

    private final NacosConfigManager nacosConfigManager;
    
    // 默认命名空间
    private static final String DEFAULT_NAMESPACE = "public";
    // 默认分组
    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    // 监听器映射表
    private final Map<String, ListenerWrapper> listenerMap = new ConcurrentHashMap<>();

    @Autowired
    public NacosConfigService(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
    }

    @Override
    public String getConfig(String key) {
        return getConfig(DEFAULT_NAMESPACE, key);
    }

    @Override
    public String getConfig(String namespace, String key) {
        try {
            return nacosConfigManager.getConfigService().getConfig(key, DEFAULT_GROUP, 5000);
        } catch (NacosException e) {
            log.error("获取配置失败，命名空间：{}，键：{}", namespace, key, e);
            return null;
        }
    }

    @Override
    public Map<String, String> getConfigs(String namespace) {
        // Nacos没有直接获取命名空间下所有配置的API，这里返回空Map
        log.warn("Nacos不支持直接获取命名空间下所有配置，命名空间：{}", namespace);
        return new HashMap<>();
    }

    @Override
    public String addListener(String key, Consumer<plato.common.config.ConfigService.ConfigChangeEvent> listener) {
        return addListener(DEFAULT_NAMESPACE, key, listener);
    }

    /**
     * 添加配置变更监听器
     * @param namespace 命名空间
     * @param key 配置键
     * @param listener 变更监听器
     * @return 监听器ID
     */
    public String addListener(String namespace, String key, Consumer<plato.common.config.ConfigService.ConfigChangeEvent> listener) {
        String listenerId = UUID.randomUUID().toString();
        try {
            ConfigService configService = nacosConfigManager.getConfigService();
            
            Listener nacosListener = new Listener() {
                @Override
                public Executor getExecutor() {
                    return null; // 使用Nacos默认的执行器
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    // 创建配置变更事件
                    NacosConfigChangeEvent event = new NacosConfigChangeEvent(
                            key,
                            namespace,
                            null, // 旧值无法获取
                            configInfo,
                            ChangeType.MODIFIED // Nacos只通知变更，不区分类型
                    );
                    // 调用监听器
                    listener.accept(event);
                }
            };
            
            // 添加Nacos监听器
            configService.addListener(key, DEFAULT_GROUP, nacosListener);
            
            // 保存监听器包装对象
            ListenerWrapper wrapper = new ListenerWrapper(namespace, key, nacosListener);
            listenerMap.put(listenerId, wrapper);
            
            log.info("添加配置监听器成功，命名空间：{}，键：{}，监听器ID：{}", namespace, key, listenerId);
            return listenerId;
        } catch (NacosException e) {
            log.error("添加配置监听器失败，命名空间：{}，键：{}", namespace, key, e);
            return null;
        }
    }

    @Override
    public void removeListener(String listenerId) {
        ListenerWrapper wrapper = listenerMap.remove(listenerId);
        if (wrapper != null) {
            try {
                ConfigService configService = nacosConfigManager.getConfigService();
                configService.removeListener(wrapper.key, DEFAULT_GROUP, wrapper.listener);
                log.info("移除配置监听器成功，命名空间：{}，键：{}，监听器ID：{}", wrapper.namespace, wrapper.key, listenerId);
            } catch (Exception e) {
                log.error("移除配置监听器失败，监听器ID：{}", listenerId, e);
            }
        }
    }

    /**
     * 监听器包装类
     */
    private static class ListenerWrapper {
        private final String namespace;
        private final String key;
        private final Listener listener;

        public ListenerWrapper(String namespace, String key, Listener listener) {
            this.namespace = namespace;
            this.key = key;
            this.listener = listener;
        }
    }

    /**
     * Nacos配置变更事件实现
     */
    private static class NacosConfigChangeEvent implements plato.common.config.ConfigService.ConfigChangeEvent {
        private final String key;
        private final String namespace;
        private final String oldValue;
        private final String newValue;
        private final ChangeType changeType;

        public NacosConfigChangeEvent(String key, String namespace, String oldValue, String newValue, ChangeType changeType) {
            this.key = key;
            this.namespace = namespace;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.changeType = changeType;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public String getOldValue() {
            return oldValue;
        }

        @Override
        public String getNewValue() {
            return newValue;
        }

        @Override
        public ChangeType getChangeType() {
            return changeType;
        }
    }
} 