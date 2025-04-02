package plato.common.utils.time;

import plato.common.utils.string.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间工具类
 * <p>
 * 提供日期时间处理相关工具方法，包括：
 * - 日期格式化和解析
 * - 时间加减操作
 * - 获取当前时间戳
 * - 判断时间是否过期
 * </p>
 * 在Go代码中通常使用time包处理时间相关操作
 */
public class TimeUtils {
    /**
     * 私有构造函数，防止实例化
     */
    private TimeUtils() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * 默认日期时间格式
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 默认时区
     */
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    /**
     * 使用默认格式格式化日期
     *
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * 使用指定格式格式化日期
     *
     * @param date 日期对象
     * @param pattern 日期格式模式
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = date.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime();
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 使用默认格式解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return 日期对象
     */
    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, DEFAULT_DATE_FORMAT);
    }

    /**
     * 使用指定格式解析日期字符串
     *
     * @param dateStr 日期字符串
     * @param pattern 日期格式模式
     * @return 日期对象
     */
    public static Date parseDate(String dateStr, String pattern) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        return Date.from(localDateTime.atZone(DEFAULT_ZONE).toInstant());
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前UTC时间戳（秒）
     *
     * @return 当前UTC时间戳（秒）
     */
    public static long getCurrentTimestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 将日期增加指定秒数
     *
     * @param date 原始日期
     * @param seconds 秒数
     * @return 增加后的日期
     */
    public static Date addSeconds(Date date, int seconds) {
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant().plusSeconds(seconds);
        return Date.from(instant);
    }

    /**
     * 将日期增加指定分钟数
     *
     * @param date 原始日期
     * @param minutes 分钟数
     * @return 增加后的日期
     */
    public static Date addMinutes(Date date, int minutes) {
        return addSeconds(date, minutes * 60);
    }

    /**
     * 将日期增加指定小时数
     *
     * @param date 原始日期
     * @param hours 小时数
     * @return 增加后的日期
     */
    public static Date addHours(Date date, int hours) {
        return addMinutes(date, hours * 60);
    }

    /**
     * 将日期增加指定天数
     *
     * @param date 原始日期
     * @param days 天数
     * @return 增加后的日期
     */
    public static Date addDays(Date date, int days) {
        return addHours(date, days * 24);
    }

    /**
     * 判断日期是否已过期
     *
     * @param date 日期
     * @param timeoutMillis 超时毫秒数
     * @return 如果已过期，则返回true
     */
    public static boolean isExpired(Date date, long timeoutMillis) {
        if (date == null) {
            return true;
        }
        return System.currentTimeMillis() - date.getTime() > timeoutMillis;
    }
    
    /**
     * 计算两个日期之间的差值（毫秒）
     *
     * @param start 开始日期
     * @param end 结束日期
     * @return 毫秒差值
     */
    public static long getDurationMillis(Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        }
        return end.getTime() - start.getTime();
    }
    
    /**
     * 将毫秒时间戳转换为日期对象
     *
     * @param timestamp 毫秒时间戳
     * @return 日期对象
     */
    public static Date fromTimestamp(long timestamp) {
        return new Date(timestamp);
    }
    
    /**
     * 将秒时间戳转换为日期对象
     *
     * @param timestamp 秒时间戳
     * @return 日期对象
     */
    public static Date fromTimestampSeconds(long timestamp) {
        return new Date(timestamp * 1000);
    }
} 