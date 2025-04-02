package plato.common.id;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法ID生成器
 * 用于生成全局唯一的64位长整型ID
 * 对应Go版本中的ID生成器，但使用了更通用的雪花算法
 */
@Slf4j
@Component
public class SnowflakeIdGenerator {

    /**
     * 开始时间截 (2023-01-01)
     */
    private final long twepoch = 1672531200000L;

    /**
     * 机器ID所占的位数
     */
    private final long workerIdBits = 10L;

    /**
     * 数据中心ID所占的位数
     */
    private final long datacenterIdBits = 5L;

    /**
     * 序列号所占的位数
     */
    private final long sequenceBits = 12L;

    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;

    /**
     * 数据中心ID向左移22位(12+10)
     */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间截向左移27位(12+10+5)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作机器ID(0~1023)
     */
    @Value("${snowflake.worker-id:-1}")
    private long workerId;

    /**
     * 数据中心ID(0~31)
     */
    @Value("${snowflake.datacenter-id:-1}")
    private long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private final AtomicLong sequence = new AtomicLong(0);

    /**
     * 上次生成ID的时间截
     */
    private volatile long lastTimestamp = -1L;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        // 如果没有配置workerId，则根据MAC地址生成
        if (workerId == -1) {
            workerId = generateWorkerId();
            log.info("Using generated worker ID: {}", workerId);
        }

        // 如果没有配置datacenterId，则使用默认值
        if (datacenterId == -1) {
            datacenterId = 1;
            log.info("Using default datacenter ID: {}", datacenterId);
        }

        // 检查workerId和datacenterId是否在有效范围内
        long maxWorkerId = -1L ^ (-1L << workerIdBits);
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("Worker ID can't be greater than %d or less than 0", maxWorkerId));
        }

        long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("Datacenter ID can't be greater than %d or less than 0", maxDatacenterId));
        }

        log.info("SnowflakeIdGenerator initialized with workerId: {}, datacenterId: {}", workerId, datacenterId);
    }

    /**
     * 根据MAC地址生成workerId
     *
     * @return workerId
     */
    private long generateWorkerId() {
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                    break;
                }
            }
            
            // 如果没有找到MAC地址，则使用随机数
            if (sb.length() == 0) {
                return (int) (Math.random() * 1023);
            }
            
            // 使用MAC地址的哈希值对1023取模
            return Math.abs(sb.toString().hashCode() % 1024);
        } catch (SocketException e) {
            log.error("Failed to get network interfaces", e);
            return (int) (Math.random() * 1023);
        }
    }

    /**
     * 获取下一个ID
     *
     * @return 下一个ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        // 这里不抛出异常，而是使用上次的时间戳，保证ID的单调递增
        if (timestamp < lastTimestamp) {
            log.warn("Clock moved backwards. Using last timestamp: {}", lastTimestamp);
            timestamp = lastTimestamp;
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            // 序列号递增
            long seq = sequence.incrementAndGet() & sequenceMask;
            // 毫秒内序列溢出
            if (seq == 0) {
                // 阻塞到下一个毫秒，获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence.set(0);
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence.get();
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}