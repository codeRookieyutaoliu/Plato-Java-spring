package plato.common.utils;

import java.util.zip.CRC32;

/**
 * 哈希工具类
 * <p>
 * 提供字符串哈希计算的工具方法
 * </p>
 * 
 * 对应Go项目中的utils/hash.go文件中的HashStr函数
 */
public final class HashUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private HashUtils() {
        throw new UnsupportedOperationException("工具类不支持实例化");
    }
    
    /**
     * 计算字符串的CRC32哈希值
     * <p>
     * 对应Go项目中的utils.HashStr函数，使用CRC32算法计算字符串哈希值
     * Go代码:
     * <pre>
     * func HashStr(key string) uint32 {
     *     if len(key) < 64 {
     *         var scratch [64]byte
     *         copy(scratch[:], key)
     *         return crc32.ChecksumIEEE(scratch[:len(key)])
     *     }
     *     return crc32.ChecksumIEEE([]byte(key))
     * }
     * </pre>
     * </p>
     * 
     * @param key 需要计算哈希值的字符串
     * @return 计算得到的CRC32哈希值
     */
    public static long hashString(String key) {
        byte[] bytes;
        if (key.length() < 64) {
            // 优化小字符串处理，与Go版本保持一致
            bytes = new byte[64];
            System.arraycopy(key.getBytes(), 0, bytes, 0, key.length());
            bytes = java.util.Arrays.copyOf(bytes, key.length());
        } else {
            bytes = key.getBytes();
        }
        
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return crc32.getValue();
    }
}
