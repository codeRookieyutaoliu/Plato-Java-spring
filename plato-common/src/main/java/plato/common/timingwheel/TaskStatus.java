package plato.common.timingwheel;

/**
 * 任务状态枚举
 * 定义时间轮任务的生命周期状态
 */
public enum TaskStatus {
    /**
     * 等待执行状态
     */
    PENDING,

    /**
     * 执行中状态
     */
    RUNNING,

    /**
     * 执行完成状态
     */
    COMPLETED,

    /**
     * 执行失败状态
     */
    FAILED,

    /**
     * 已取消状态
     */
    CANCELLED
}