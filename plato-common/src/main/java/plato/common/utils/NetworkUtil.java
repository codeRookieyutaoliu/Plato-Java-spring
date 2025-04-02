package plato.common.utils;

import plato.common.exception.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 网络工具类
 * <p>
 * 提供常用的网络操作工具方法，如获取本机IP、检查端口可用性等
 * </p>
 */
@Slf4j
public final class NetworkUtil {
    
    /**
     * IP地址的正则表达式模式
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    
    /**
     * 私有构造函数，防止实例化
     */
    private NetworkUtil() {
        throw new UnsupportedOperationException("工具类不支持实例化");
    }
    
    /**
     * 获取本机内网IP地址
     * 
     * @return 本机内网IP地址
     * @throws NetworkException 如果获取失败
     */
    public static String getLocalIpAddress() throws NetworkException {
        try {
            // 首先尝试获取非回环地址
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // 跳过禁用的接口
                if (iface.isUp() && !iface.isLoopback() && !iface.isVirtual()) {
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        String ip = addr.getHostAddress();
                        // 过滤IPv6地址
                        if (IP_PATTERN.matcher(ip).matches()) {
                            return ip;
                        }
                    }
                }
            }
            
            // 如果没有找到合适的地址，返回本地回环地址
            return InetAddress.getLocalHost().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw NetworkException.networkUnavailable("获取本机IP地址失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有本机IP地址
     * 
     * @return 本机所有IP地址列表
     * @throws NetworkException 如果获取失败
     */
    public static List<String> getAllLocalIpAddresses() throws NetworkException {
        List<String> addressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isUp() && !iface.isLoopback()) {
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        String ip = addr.getHostAddress();
                        // 过滤IPv6地址
                        if (IP_PATTERN.matcher(ip).matches()) {
                            addressList.add(ip);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            throw NetworkException.networkUnavailable("获取所有本机IP地址失败: " + e.getMessage());
        }
        return addressList;
    }
    
    /**
     * 检查主机端口是否可用
     * 
     * @param host 主机地址
     * @param port 端口号
     * @param timeoutMillis 超时时间（毫秒）
     * @return 如果端口可用返回true，否则返回false
     */
    public static boolean isPortAvailable(String host, int port, int timeoutMillis) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 等待端口可用
     * 
     * @param host 主机地址
     * @param port 端口号
     * @param timeoutMillis 总超时时间（毫秒）
     * @param intervalMillis 检查间隔（毫秒）
     * @return 如果在超时时间内端口变为可用返回true，否则返回false
     */
    public static boolean waitForPortAvailable(String host, int port, int timeoutMillis, int intervalMillis) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutMillis;
        
        while (System.currentTimeMillis() < endTime) {
            if (isPortAvailable(host, port, intervalMillis)) {
                return true;
            }
            
            try {
                TimeUnit.MILLISECONDS.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * 解析主机端口字符串（格式：host:port）
     * 
     * @param hostPort 主机端口字符串
     * @return 包含主机和端口的InetSocketAddress
     * @throws NetworkException 如果格式不正确
     */
    public static InetSocketAddress parseHostPort(String hostPort) throws NetworkException {
        try {
            String[] parts = hostPort.split(":");
            if (parts.length != 2) {
                throw NetworkException.protocolError("主机端口格式不正确: " + hostPort);
            }
            
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            if (port < 1 || port > 65535) {
                throw NetworkException.protocolError("端口号无效: " + port);
            }
            
            return new InetSocketAddress(host, port);
        } catch (NumberFormatException e) {
            throw NetworkException.protocolError("端口号格式不正确: " + e.getMessage());
        }
    }
    
    /**
     * 检查IP地址格式是否有效
     * 
     * @param ip IP地址字符串
     * @return 如果格式有效返回true，否则返回false
     */
    public static boolean isValidIpAddress(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }
    
    /**
     * 将IP地址转换为长整数
     * 
     * @param ipAddress IP地址字符串
     * @return 长整数表示的IP地址
     * @throws NetworkException 如果IP地址格式不正确
     */
    public static long ipToLong(String ipAddress) throws NetworkException {
        if (!isValidIpAddress(ipAddress)) {
            throw NetworkException.protocolError("IP地址格式不正确: " + ipAddress);
        }
        
        String[] octets = ipAddress.split("\\.");
        return (Long.parseLong(octets[0]) << 24) +
               (Long.parseLong(octets[1]) << 16) +
               (Long.parseLong(octets[2]) << 8) +
               Long.parseLong(octets[3]);
    }
    
    /**
     * 将长整数转换为IP地址
     * 
     * @param ip 长整数表示的IP地址
     * @return IP地址字符串
     */
    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }
} 