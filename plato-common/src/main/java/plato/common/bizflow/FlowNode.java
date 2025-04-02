package plato.common.bizflow;

import java.util.Map;
import java.util.Optional;

/**
 * 流程节点接口
 * 定义节点的基本操作和生命周期方法
 */
public interface FlowNode {
    /**
     * 节点初始化
     * @param context 初始化上下文
     */
    void init(Map<String, Object> context);

    /**
     * 执行节点逻辑
     * @param context 执行上下文
     * @return 执行结果
     */
    Optional<Map<String, Object>> execute(Map<String, Object> context);

    /**
     * 节点销毁
     * @param context 销毁上下文
     */
    void destroy(Map<String, Object> context);

    /**
     * 获取节点状态
     * @return 节点状态
     */
    NodeStatus getStatus();

    /**
     * 获取节点元数据
     * @return 节点元数据
     */
    NodeMeta getMeta();
}