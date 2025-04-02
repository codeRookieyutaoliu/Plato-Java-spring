package plato.common.bizflow;

import java.util.Map;
import java.util.Optional;

/**
 * 业务流程引擎接口
 * 定义业务流程的基本操作，包括节点管理和流程执行
 */
public interface BizFlow {
    /**
     * 添加流程节点
     * @param nodeId 节点ID
     * @param node 节点实现
     * @return 是否添加成功
     */
    boolean addNode(String nodeId, FlowNode node);

    /**
     * 连接两个节点
     * @param fromNodeId 起始节点ID
     * @param toNodeId 目标节点ID
     * @return 是否连接成功
     */
    boolean connect(String fromNodeId, String toNodeId);

    /**
     * 执行流程
     * @param startNodeId 起始节点ID
     * @param context 流程上下文
     * @return 执行结果
     */
    Optional<Map<String, Object>> execute(String startNodeId, Map<String, Object> context);

    /**
     * 获取流程节点
     * @param nodeId 节点ID
     * @return 节点实现
     */
    Optional<FlowNode> getNode(String nodeId);

    /**
     * 验证流程是否有效
     * @return 是否有效
     */
    boolean validate();
}