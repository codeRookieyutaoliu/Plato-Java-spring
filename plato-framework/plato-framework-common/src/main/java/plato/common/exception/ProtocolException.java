package plato.common.exception;

/**
 * 协议异常
 * <p>
 * 表示在协议处理过程中发生的异常
 * </p>
 */
public class ProtocolException extends RuntimeException {
    
    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public ProtocolException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param cause 异常原因
     */
    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数
     *
     * @param cause 异常原因
     */
    public ProtocolException(Throwable cause) {
        super(cause);
    }
} 