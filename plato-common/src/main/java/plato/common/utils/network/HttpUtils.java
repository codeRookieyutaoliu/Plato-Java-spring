package plato.common.utils.network;

import plato.common.utils.json.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP工具类
 * <p>
 * 基于Java 11 HttpClient实现的HTTP请求工具，提供：
 * - 同步GET/POST请求
 * - 异步GET/POST请求
 * - JSON数据发送与接收
 * </p>
 * 在Go代码中通常使用标准库的http包实现类似功能
 */
public class HttpUtils {
    /**
     * 私有构造函数，防止实例化
     */
    private HttpUtils() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * 默认超时时间（秒）
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    /**
     * 默认HTTP客户端
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .build();

    /**
     * 发送GET请求
     *
     * @param url 请求URL
     * @return 响应内容
     * @throws IOException 如果请求失败
     * @throws InterruptedException 如果请求被中断
     */
    public static String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 发送GET请求，并将响应解析为指定类型
     *
     * @param url 请求URL
     * @param responseType 响应类型Class
     * @param <T> 响应对象类型
     * @return 响应对象
     * @throws IOException 如果请求失败
     * @throws InterruptedException 如果请求被中断
     */
    public static <T> T get(String url, Class<T> responseType) throws IOException, InterruptedException {
        String responseBody = get(url);
        return JsonUtils.fromJson(responseBody, responseType);
    }

    /**
     * 异步发送GET请求
     *
     * @param url 请求URL
     * @return 包含响应内容的CompletableFuture
     */
    public static CompletableFuture<String> getAsync(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * 发送POST请求
     *
     * @param url 请求URL
     * @param body 请求体内容
     * @return 响应内容
     * @throws IOException 如果请求失败
     * @throws InterruptedException 如果请求被中断
     */
    public static String post(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 发送POST请求，使用对象作为请求体并将响应解析为指定类型
     *
     * @param url 请求URL
     * @param requestObject 请求对象
     * @param responseType 响应类型Class
     * @param <T> 响应对象类型
     * @param <R> 请求对象类型
     * @return 响应对象
     * @throws IOException 如果请求失败
     * @throws InterruptedException 如果请求被中断
     */
    public static <T, R> T post(String url, R requestObject, Class<T> responseType) throws IOException, InterruptedException {
        String requestBody = JsonUtils.toJson(requestObject);
        String responseBody = post(url, requestBody);
        return JsonUtils.fromJson(responseBody, responseType);
    }

    /**
     * 异步发送POST请求
     *
     * @param url 请求URL
     * @param body 请求体内容
     * @return 包含响应内容的CompletableFuture
     */
    public static CompletableFuture<String> postAsync(String url, String body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * 构建查询参数字符串
     *
     * @param params 参数映射表
     * @return URL查询参数字符串，格式为"param1=value1&param2=value2"
     */
    public static String buildQueryParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        return sb.toString();
    }

    /**
     * 构建包含查询参数的URL
     *
     * @param baseUrl 基础URL
     * @param params 查询参数映射表
     * @return 完整URL
     */
    public static String buildUrl(String baseUrl, Map<String, String> params) {
        String queryParams = buildQueryParams(params);
        if (queryParams.isEmpty()) {
            return baseUrl;
        }
        
        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + queryParams;
    }
} 