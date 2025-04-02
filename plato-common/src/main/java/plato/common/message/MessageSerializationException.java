package plato.common.message;

import plato.common.exception.PlatoException;

/**
 * 消息序列化异常
 * <p>
 * 表示消息序列化或反序列化过程中发生的异常
 * </p>
 */
public class MessageSerializationException extends PlatoException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public MessageSerializationException(String message) {
        super("MSG-SERIAL-001", message);
    }
    
    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause 原始异常
     */
    public MessageSerializationException(String message, Throwable cause) {
        super("MSG-SERIAL-001", message, cause);
    }
} 