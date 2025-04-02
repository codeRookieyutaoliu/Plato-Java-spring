package plato.gateway.infrastructure.connection;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接ID生成器
 * 对应Go版本的ConnIDGenerater结构体
 */
@Component
public class ConnectionIdGenerator {
    
    // 序列号，对应Go版本的seq
    private final AtomicLong sequence = new AtomicLong(0);
    
    // 上次生成ID的时间戳，对应Go版本的lastTimestamp
    private volatile long lastTimestamp = -1L;
    
    // 时间戳位移，对应Go版本的timestampLeftShift
    private static final int TIMESTAMP_LEFT_SHIFT = 22;
    
    // 序列号掩码，对应Go版本的sequenceMask
    private static final long SEQUENCE_MASK = (1L << 22) - 1;
    
    /**
     * 获取下一个连接ID
     * 对应Go版本的NextID方法
     *
     * @return 连接ID
     * @throws Exception 如果时钟回拨，则抛出异常
     */
    public synchronized long nextId() throws Exception {
        long timestamp = getMilliSeconds();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，抛出异常
        // 对应Go版本的if curTimestamp < w.lastTimestamp
        if (timestamp < lastTimestamp) {
            throw new Exception(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        
        // 如果是同一时间生成的，则进行序列号自增
        // 对应Go版本的if curTimestamp == w.lastTimestamp
        if (timestamp == lastTimestamp) {
            sequence.set((sequence.get() + 1) & SEQUENCE_MASK);
            // 序列号溢出，等待下一毫秒
            // 对应Go版本的if w.seq == 0
            if (sequence.get() == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，序列号重置为0
            // 对应Go版本的w.seq = 0
            sequence.set(0);
        }
        
        lastTimestamp = timestamp;
        
        // 生成并返回ID
        // 对应Go版本的return (curTimestamp << w.timestampLeftShift) | w.seq, nil
        return (timestamp << TIMESTAMP_LEFT_SHIFT) | sequence.get();
    }
    
    /**
     * 获取当前时间戳（毫秒）
     * 对应Go版本的getMilliSeconds方法
     *
     * @return 当前时间戳（毫秒）
     */
    private long getMilliSeconds() {
        return System.currentTimeMillis();
    }
    
    /**
     * 等待到下一个毫秒
     * 对应Go版本的tilNextMillis方法
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 下一个毫秒的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = getMilliSeconds();
        while (timestamp <= lastTimestamp) {
            timestamp = getMilliSeconds();
        }
        return timestamp;
    }
} 