package plato.common.exception;

/**
 * Plato基础异常类
 * <p>
 * 所有自定义异常的基类，包含错误码和错误信息
 * </p>
 * 
 * 对应Go项目中的错误处理机制，Go语言中通常使用error接口和自定义error类型实现异常处理
 */
public class PlatoException extends RuntimeException {
    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 默认错误码
     */
    private static final String DEFAULT_ERROR_CODE = "PLATO-SYS-500";

    /**
     * 创建带有默认错误码的异常
     *
     * @param message 错误信息
     */
    public PlatoException(String message) {
        this(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 创建带有默认错误码和原因的异常
     *
     * @param message 错误信息
     * @param cause 异常原因
     */
    public PlatoException(String message, Throwable cause) {
        this(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * 创建带有错误码的异常
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public PlatoException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 创建带有错误码和原因的异常
     *
     * @param errorCode 错误码
     * @param message 错误信息
     * @param cause 异常原因
     */
    public PlatoException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return "[" + errorCode + "] " + getMessage();
    }
} 