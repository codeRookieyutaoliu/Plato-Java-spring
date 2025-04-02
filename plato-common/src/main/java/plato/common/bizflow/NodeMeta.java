package plato.common.bizflow;

import java.util.Map;

/**
 * 节点元数据接口
 * 定义节点的基本属性和配置信息
 */
public interface NodeMeta {
    /**
     * 获取节点ID
     * @return 节点ID
     */
    String getId();

    /**
     * 获取节点名称
     * @return 节点名称
     */
    String getName();

    /**
     * 获取节点类型
     * @return 节点类型
     */
    String getType();

    /**
     * 获取节点配置
     * @return 节点配置
     */
    Map<String, Object> getConfig();

    /**
     * 获取节点描述
     * @return 节点描述
     */
    String getDescription();
}