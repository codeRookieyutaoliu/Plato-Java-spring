package plato.protocol.transport;

/**
 * 传输异常
 * <p>
 * 用于表示传输层出现的各种异常情况
 * </p>
 */
public class TransportException extends RuntimeException {

    /**
     * 异常类型
     */
    private final TransportExceptionType type;

    /**
     * 构造函数
     *
     * @param type 异常类型
     * @param message 异常信息
     */
    public TransportException(TransportExceptionType type, String message) {
        super(message);
        this.type = type;
    }

    /**
     * 构造函数
     *
     * @param type 异常类型
     * @param message 异常信息
     * @param cause 异常原因
     */
    public TransportException(TransportExceptionType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    /**
     * 获取异常类型
     *
     * @return 异常类型
     */
    public TransportExceptionType getType() {
        return type;
    }

    /**
     * 创建连接超时异常
     *
     * @param endpoint 目标端点
     * @return 连接超时异常
     */
    public static TransportException connectTimeout(String endpoint) {
        return new TransportException(
                TransportExceptionType.CONNECT_TIMEOUT,
                "连接超时: " + endpoint
        );
    }

    /**
     * 创建连接失败异常
     *
     * @param endpoint 目标端点
     * @param cause 异常原因
     * @return 连接失败异常
     */
    public static TransportException connectFailed(String endpoint, Throwable cause) {
        return new TransportException(
                TransportExceptionType.CONNECT_FAILED,
                "连接失败: " + endpoint,
                cause
        );
    }

    /**
     * 创建连接关闭异常
     *
     * @param endpoint 目标端点
     * @return 连接关闭异常
     */
    public static TransportException connectionClosed(String endpoint) {
        return new TransportException(
                TransportExceptionType.CONNECTION_CLOSED,
                "连接已关闭: " + endpoint
        );
    }

    /**
     * 创建读超时异常
     *
     * @param endpoint 目标端点
     * @return 读超时异常
     */
    public static TransportException readTimeout(String endpoint) {
        return new TransportException(
                TransportExceptionType.READ_TIMEOUT,
                "读取超时: " + endpoint
        );
    }

    /**
     * 创建写超时异常
     *
     * @param endpoint 目标端点
     * @return 写超时异常
     */
    public static TransportException writeTimeout(String endpoint) {
        return new TransportException(
                TransportExceptionType.WRITE_TIMEOUT,
                "写入超时: " + endpoint
        );
    }

    /**
     * 创建读失败异常
     *
     * @param endpoint 目标端点
     * @param cause 异常原因
     * @return 读失败异常
     */
    public static TransportException readFailed(String endpoint, Throwable cause) {
        return new TransportException(
                TransportExceptionType.READ_FAILED,
                "读取失败: " + endpoint,
                cause
        );
    }

    /**
     * 创建写失败异常
     *
     * @param endpoint 目标端点
     * @param cause 异常原因
     * @return 写失败异常
     */
    public static TransportException writeFailed(String endpoint, Throwable cause) {
        return new TransportException(
                TransportExceptionType.WRITE_FAILED,
                "写入失败: " + endpoint,
                cause
        );
    }

    /**
     * 传输异常类型
     */
    public enum TransportExceptionType {
        /**
         * 连接超时
         */
        CONNECT_TIMEOUT,

        /**
         * 连接失败
         */
        CONNECT_FAILED,

        /**
         * 连接已关闭
         */
        CONNECTION_CLOSED,

        /**
         * 读取超时
         */
        READ_TIMEOUT,

        /**
         * 写入超时
         */
        WRITE_TIMEOUT,

        /**
         * 读取失败
         */
        READ_FAILED,

        /**
         * 写入失败
         */
        WRITE_FAILED
    }
}