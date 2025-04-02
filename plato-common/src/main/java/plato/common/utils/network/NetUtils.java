package plato.common.utils.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 网络工具类
 * <p>
 * 提供网络相关的工具方法，包括：
 * - 获取本机IP地址
 * - 检查IP地址有效性
 * - 解析主机名和端口
 * - 检查端口可用性
 * </p>
 * 在Go代码中通常使用net包实现类似功能
 */
public class NetUtils {
    /**
     * 私有构造函数，防止实例化
     */
    private NetUtils() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * IP地址正则表达式模式
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    /**
     * 获取本机IP地址
     *
     * @return 本机IP地址，获取失败则返回"127.0.0.1"
     */
    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
     * 获取本机IP地址（非回环地址）
     *
     * @return 本机非回环IP地址，获取失败则返回"127.0.0.1"
     */
    public static String getLocalNonLoopbackIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // 跳过禁用和回环接口
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') < 0) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            // 获取失败，返回本地回环地址
        }
        return "127.0.0.1";
    }

    /**
     * 检查IP地址是否有效
     *
     * @param ip IP地址字符串
     * @return 如果是有效的IPv4地址，则返回true
     */
    public static boolean isValidIp(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 解析主机名和端口
     *
     * @param endpoint 格式为"host:port"的端点字符串
     * @return 包含主机名和端口的数组，[0]为主机名，[1]为端口
     * @throws IllegalArgumentException 如果格式无效
     */
    public static String[] parseEndpoint(String endpoint) {
        if (endpoint == null || !endpoint.contains(":")) {
            throw new IllegalArgumentException("Invalid endpoint format, expected 'host:port'");
        }
        String[] parts = endpoint.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid endpoint format, expected 'host:port'");
        }
        return parts;
    }

    /**
     * 解析主机地址
     *
     * @param endpoint 格式为"host:port"的端点字符串
     * @return 主机名部分
     * @throws IllegalArgumentException 如果格式无效
     */
    public static String parseHost(String endpoint) {
        return parseEndpoint(endpoint)[0];
    }

    /**
     * 解析端口
     *
     * @param endpoint 格式为"host:port"的端点字符串
     * @return 端口号
     * @throws IllegalArgumentException 如果格式无效或端口非数字
     */
    public static int parsePort(String endpoint) {
        String portStr = parseEndpoint(endpoint)[1];
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port number: " + portStr);
        }
    }

    /**
     * 构建端点字符串
     *
     * @param host 主机名
     * @param port 端口
     * @return 格式为"host:port"的端点字符串
     */
    public static String buildEndpoint(String host, int port) {
        return host + ":" + port;
    }
} 