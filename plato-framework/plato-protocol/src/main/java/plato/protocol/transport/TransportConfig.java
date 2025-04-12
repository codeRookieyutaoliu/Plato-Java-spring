package plato.protocol.transport;

/**
 * 传输配置
 * <p>
 * 用于配置传输层相关参数，如连接超时、读写超时等
 * </p>
 */
public class TransportConfig {

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 3000;

    /**
     * 读超时时间（毫秒）
     */
    private int readTimeout = 30000;

    /**
     * 写超时时间（毫秒）
     */
    private int writeTimeout = 3000;

    /**
     * 心跳间隔（毫秒）
     */
    private int heartbeatInterval = 60000;

    /**
     * 最大帧长度
     */
    private int maxFrameLength = 65536;

    /**
     * 是否启用TCP Keepalive
     */
    private boolean tcpKeepAlive = true;

    /**
     * 是否启用TCP nodelay（禁用Nagle算法）
     */
    private boolean tcpNoDelay = true;

    /**
     * 创建默认配置
     *
     * @return 默认传输配置
     */
    public static TransportConfig createDefault() {
        return new TransportConfig();
    }

    /**
     * 获取连接超时时间
     *
     * @return 连接超时时间（毫秒）
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置连接超时时间
     *
     * @param connectTimeout 连接超时时间（毫秒）
     * @return this
     */
    public TransportConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 获取读超时时间
     *
     * @return 读超时时间（毫秒）
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * 设置读超时时间
     *
     * @param readTimeout 读超时时间（毫秒）
     * @return this
     */
    public TransportConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 获取写超时时间
     *
     * @return 写超时时间（毫秒）
     */
    public int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * 设置写超时时间
     *
     * @param writeTimeout 写超时时间（毫秒）
     * @return this
     */
    public TransportConfig setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * 获取心跳间隔
     *
     * @return 心跳间隔（毫秒）
     */
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    /**
     * 设置心跳间隔
     *
     * @param heartbeatInterval 心跳间隔（毫秒）
     * @return this
     */
    public TransportConfig setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return this;
    }

    /**
     * 获取最大帧长度
     *
     * @return 最大帧长度
     */
    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    /**
     * 设置最大帧长度
     *
     * @param maxFrameLength 最大帧长度
     * @return this
     */
    public TransportConfig setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
        return this;
    }

    /**
     * 是否启用TCP Keepalive
     *
     * @return 是否启用TCP Keepalive
     */
    public boolean isTcpKeepAlive() {
        return tcpKeepAlive;
    }

    /**
     * 设置是否启用TCP Keepalive
     *
     * @param tcpKeepAlive 是否启用TCP Keepalive
     * @return this
     */
    public TransportConfig setTcpKeepAlive(boolean tcpKeepAlive) {
        this.tcpKeepAlive = tcpKeepAlive;
        return this;
    }

    /**
     * 是否启用TCP nodelay
     *
     * @return 是否启用TCP nodelay
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * 设置是否启用TCP nodelay
     *
     * @param tcpNoDelay 是否启用TCP nodelay
     * @return this
     */
    public TransportConfig setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }
}