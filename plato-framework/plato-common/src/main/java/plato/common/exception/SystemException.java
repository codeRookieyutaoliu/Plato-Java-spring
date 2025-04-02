package plato.common.exception;

/**
 * 系统异常类
 * <p>
 * 用于表示系统级错误，如配置错误、资源不足、依赖服务故障等
 * </p>
 * 
 * 对应Go项目中通常使用的系统错误处理机制，在Go中通常会区分业务错误和系统错误
 */
public class SystemException extends PlatoException {
    /**
     * 系统异常前缀
     */
    private static final String SYSTEM_PREFIX = "PLATO-SYS-";

    /**
     * 创建系统异常
     *
     * @param code 系统错误码（不含前缀）
     * @param message 错误信息
     */
    public SystemException(String code, String message) {
        super(SYSTEM_PREFIX + code, message);
    }

    /**
     * 创建系统异常
     *
     * @param code 系统错误码（不含前缀）
     * @param message 错误信息
     * @param cause 异常原因
     */
    public SystemException(String code, String message, Throwable cause) {
        super(SYSTEM_PREFIX + code, message, cause);
    }

    /**
     * 配置错误
     *
     * @param message 错误信息
     * @return 系统异常
     */
    public static SystemException configError(String message) {
        return new SystemException("CONFIG-500", message);
    }

    /**
     * 资源不足
     *
     * @param resource 资源类型
     * @return 系统异常
     */
    public static SystemException resourceExhausted(String resource) {
        return new SystemException("RESOURCE-500", resource + "资源不足");
    }

    /**
     * 依赖服务故障
     *
     * @param service 服务名称
     * @param message 错误信息
     * @return 系统异常
     */
    public static SystemException dependencyError(String service, String message) {
        return new SystemException("DEP-500", "依赖服务[" + service + "]异常: " + message);
    }

    /**
     * 系统内部错误
     *
     * @param message 错误信息
     * @return 系统异常
     */
    public static SystemException internalError(String message) {
        return new SystemException("INTERNAL-500", message);
    }

    /**
     * 系统内部错误
     *
     * @param message 错误信息
     * @param cause 异常原因
     * @return 系统异常
     */
    public static SystemException internalError(String message, Throwable cause) {
        return new SystemException("INTERNAL-500", message, cause);
    }
} 