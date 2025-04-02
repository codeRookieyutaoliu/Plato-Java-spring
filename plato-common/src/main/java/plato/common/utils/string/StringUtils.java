package plato.common.utils.string;

import java.util.UUID;

/**
 * 字符串工具类
 * <p>
 * 提供字符串相关的常用操作方法，包括：
 * - 字符串判空
 * - UUID生成
 * - 字符串默认值
 * - 字符串比较
 * - 字符串修剪
 * </p>
 * 对应Go代码中的部分字符串处理函数
 */
public class StringUtils {
    /**
     * 私有构造函数，防止实例化
     */
    private StringUtils() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * 判断字符串是否为空或仅包含空白字符
     *
     * @param str 待检查的字符串
     * @return 如果字符串为null或空白，则返回true
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否非空且包含非空白字符
     *
     * @param str 待检查的字符串
     * @return 如果字符串非null且包含非空白字符，则返回true
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 生成不带连字符的UUID字符串
     *
     * @return 32位UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 如果字符串为空，则返回默认值
     *
     * @param str 原始字符串
     * @param defaultStr 默认字符串
     * @return 非空字符串或默认值
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * 去除字符串两端的空白字符
     *
     * @param str 原始字符串
     * @return 修剪后的字符串，如果输入为null则返回null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 比较两个字符串是否相等
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串相等，则返回true
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 忽略大小写比较两个字符串是否相等
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串忽略大小写后相等，则返回true
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }
} 