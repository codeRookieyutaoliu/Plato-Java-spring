package plato.state.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 消息配置
 * 配置消息状态机相关参数
 * 对应Go版本中的消息相关配置
 */
@Configuration
@ConfigurationProperties(prefix = "state.message")
public class MessageConfig {

    /**
     * 消息重传间隔（毫秒）
     * 对应Go版本中的msgRetryInterval配置
     */
    private long retryInterval = 3000;

    /**
     * 消息最大重传次数
     * 对应Go版本中的msgMaxRetryTimes配置
     */
    private int maxRetryTimes = 3;

    /**
     * 消息过期时间（秒）
     * 对应Go版本中的msgExpireTime配置
     */
    private long expireTime = 7 * 24 * 60 * 60; // 7天

    /**
     * 获取消息重传间隔
     *
     * @return 消息重传间隔（毫秒）
     */
    public long getRetryInterval() {
        return retryInterval;
    }

    /**
     * 设置消息重传间隔
     *
     * @param retryInterval 消息重传间隔（毫秒）
     */
    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    /**
     * 获取消息最大重传次数
     *
     * @return 消息最大重传次数
     */
    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    /**
     * 设置消息最大重传次数
     *
     * @param maxRetryTimes 消息最大重传次数
     */
    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    /**
     * 获取消息过期时间
     *
     * @return 消息过期时间（秒）
     */
    public long getExpireTime() {
        return expireTime;
    }

    /**
     * 设置消息过期时间
     *
     * @param expireTime 消息过期时间（秒）
     */
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
} 