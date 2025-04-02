package plato.common.exception;

/**
 * 业务异常类
 * <p>
 * 用于表示业务逻辑错误，如参数校验失败、业务规则冲突等
 * </p>
 */
public class BusinessException extends PlatoException {
    /**
     * 业务异常前缀
     */
    private static final String BUSINESS_PREFIX = "PLATO-BIZ-";

    /**
     * 创建业务异常
     *
     * @param code 业务错误码（不含前缀）
     * @param message 错误信息
     */
    public BusinessException(String code, String message) {
        super(BUSINESS_PREFIX + code, message);
    }

    /**
     * 创建业务异常
     *
     * @param code 业务错误码（不含前缀）
     * @param message 错误信息
     * @param cause 异常原因
     */
    public BusinessException(String code, String message, Throwable cause) {
        super(BUSINESS_PREFIX + code, message, cause);
    }

    /**
     * 参数错误
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException invalidParameter(String message) {
        return new BusinessException("400", message);
    }

    /**
     * 资源不存在
     *
     * @param resource 资源名称
     * @return 业务异常
     */
    public static BusinessException resourceNotFound(String resource) {
        return new BusinessException("404", resource + "不存在");
    }

    /**
     * 操作被拒绝
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException operationDenied(String message) {
        return new BusinessException("403", message);
    }

    /**
     * 数据冲突
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException dataConflict(String message) {
        return new BusinessException("409", message);
    }

    /**
     * 业务处理失败
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException processFailed(String message) {
        return new BusinessException("500", message);
    }
} 