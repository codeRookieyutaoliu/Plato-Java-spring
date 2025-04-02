package plato.common.bizflow;

/**
 * 节点状态枚举
 * 定义流程节点的生命周期状态
 */
public enum NodeStatus {
    /**
     * 初始状态
     */
    INIT,

    /**
     * 就绪状态
     */
    READY,

    /**
     * 运行状态
     */
    RUNNING,

    /**
     * 完成状态
     */
    COMPLETED,

    /**
     * 失败状态
     */
    FAILED,

    /**
     * 已销毁状态
     */
    DESTROYED
}