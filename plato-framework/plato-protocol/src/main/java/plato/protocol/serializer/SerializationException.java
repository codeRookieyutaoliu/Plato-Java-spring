package plato.protocol.serializer;

/**
 * 序列化异常
 * <p>
 * 用于表示在序列化或反序列化过程中发生的异常。
 * 对应Go项目中的编解码错误处理。
 */
public class SerializationException extends RuntimeException {

    /**
     * 创建一个序列化异常
     *
     * @param message 异常信息
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * 创建一个序列化异常
     *
     * @param message 异常信息
     * @param cause 原始异常
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个序列化异常
     *
     * @param cause 原始异常
     */
    public SerializationException(Throwable cause) {
        super(cause);
    }
} 