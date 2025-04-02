package plato.common.exception;

/**
 * 网络异常类
 * <p>
 * 用于表示网络通信错误，如连接超时、读写失败、协议错误等
 * </p>
 * 
 * 对应Go项目中处理网络错误的机制，Go中通常使用net包中的各种网络错误类型
 */
public class NetworkException extends PlatoException {
    /**
     * 网络异常前缀
     */
    private static final String NETWORK_PREFIX = "PLATO-NET-";

    /**
     * 创建网络异常
     *
     * @param code 网络错误码（不含前缀）
     * @param message 错误信息
     */
    public NetworkException(String code, String message) {
        super(NETWORK_PREFIX + code, message);
    }

    /**
     * 创建网络异常
     *
     * @param code 网络错误码（不含前缀）
     * @param message 错误信息
     * @param cause 异常原因
     */
    public NetworkException(String code, String message, Throwable cause) {
        super(NETWORK_PREFIX + code, message, cause);
    }

    /**
     * 连接超时
     *
     * @param endpoint 端点信息
     * @return 网络异常
     */
    public static NetworkException connectionTimeout(String endpoint) {
        return new NetworkException("TIMEOUT-500", "连接超时: " + endpoint);
    }

    /**
     * 连接拒绝
     *
     * @param endpoint 端点信息
     * @return 网络异常
     */
    public static NetworkException connectionRefused(String endpoint) {
        return new NetworkException("REFUSED-500", "连接被拒绝: " + endpoint);
    }

    /**
     * 连接断开
     *
     * @param endpoint 端点信息
     * @return 网络异常
     */
    public static NetworkException connectionClosed(String endpoint) {
        return new NetworkException("CLOSED-500", "连接已断开: " + endpoint);
    }

    /**
     * 读取超时
     *
     * @param endpoint 端点信息
     * @return 网络异常
     */
    public static NetworkException readTimeout(String endpoint) {
        return new NetworkException("READ-TIMEOUT-500", "读取超时: " + endpoint);
    }

    /**
     * 写入失败
     *
     * @param endpoint 端点信息
     * @param message 错误信息
     * @return 网络异常
     */
    public static NetworkException writeFailed(String endpoint, String message) {
        return new NetworkException("WRITE-500", "写入失败[" + endpoint + "]: " + message);
    }

    /**
     * 协议错误
     *
     * @param message 错误信息
     * @return 网络异常
     */
    public static NetworkException protocolError(String message) {
        return new NetworkException("PROTOCOL-500", "协议错误: " + message);
    }

    /**
     * 网络不可用
     *
     * @param message 错误信息
     * @return 网络异常
     */
    public static NetworkException networkUnavailable(String message) {
        return new NetworkException("UNAVAILABLE-500", "网络不可用: " + message);
    }
} 