package plato.common.logger;

import java.util.Map;

/**
 * 日志服务接口
 * 定义日志操作的抽象接口，遵循依赖倒置原则
 * 具体实现可以是Log4j、Logback等
 */
public interface LogService {
    /**
     * 记录DEBUG级别日志
     * @param message 日志消息
     */
    void debug(String message);

    /**
     * 记录DEBUG级别日志（带上下文）
     * @param message 日志消息
     * @param context 上下文信息
     */
    void debug(String message, Map<String, Object> context);

    /**
     * 记录INFO级别日志
     * @param message 日志消息
     */
    void info(String message);

    /**
     * 记录INFO级别日志（带上下文）
     * @param message 日志消息
     * @param context 上下文信息
     */
    void info(String message, Map<String, Object> context);

    /**
     * 记录WARN级别日志
     * @param message 日志消息
     */
    void warn(String message);

    /**
     * 记录WARN级别日志（带上下文）
     * @param message 日志消息
     * @param context 上下文信息
     */
    void warn(String message, Map<String, Object> context);

    /**
     * 记录ERROR级别日志
     * @param message 日志消息
     * @param throwable 异常信息
     */
    void error(String message, Throwable throwable);

    /**
     * 记录ERROR级别日志（带上下文）
     * @param message 日志消息
     * @param throwable 异常信息
     * @param context 上下文信息
     */
    void error(String message, Throwable throwable, Map<String, Object> context);

    /**
     * 设置日志上下文
     * @param key 上下文键
     * @param value 上下文值
     */
    void setContext(String key, Object value);

    /**
     * 清除日志上下文
     */
    void clearContext();
}