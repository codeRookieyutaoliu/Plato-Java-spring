package plato.common.timingwheel;

/**
 * 时间轮任务接口
 * 定义延时任务的执行方法和任务属性
 */
public interface TimingTask {
    /**
     * 执行任务
     */
    void execute();

    /**
     * 获取任务ID
     * @return 任务ID
     */
    String getTaskId();

    /**
     * 获取任务名称
     * @return 任务名称
     */
    String getName();

    /**
     * 获取任务描述
     * @return 任务描述
     */
    String getDescription();

    /**
     * 获取任务创建时间
     * @return 创建时间戳
     */
    long getCreateTime();

    /**
     * 获取任务状态
     * @return 任务状态
     */
    TaskStatus getStatus();
}