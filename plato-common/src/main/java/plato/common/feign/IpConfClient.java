package plato.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * IP配置服务的Feign客户端接口
 * 对应Go项目中ipconf服务的RPC接口
 */
@FeignClient(name = "plato-ipconf")
public interface IpConfClient {

    /**
     * 获取最佳网关
     * 对应Go项目中的GetBestGateway方法
     *
     * @param clientIp 客户端IP
     * @return 最佳网关信息
     */
    @GetMapping("/api/ipconf/best-gateway")
    GatewayInfo getBestGateway(@RequestParam("clientIp") String clientIp);

    /**
     * 注册网关
     * 对应Go项目中的RegisterGateway方法
     *
     * @param request 注册网关请求
     * @return 注册结果
     */
    @PostMapping("/api/ipconf/register")
    RegisterResponse registerGateway(@RequestBody RegisterRequest request);

    /**
     * 网关信息
     */
    class GatewayInfo {
        private String endpoint;
        private Integer weight;
        private Integer connections;
        
        // Getters and setters
        public String getEndpoint() {
            return endpoint;
        }
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public Integer getWeight() {
            return weight;
        }
        
        public void setWeight(Integer weight) {
            this.weight = weight;
        }
        
        public Integer getConnections() {
            return connections;
        }
        
        public void setConnections(Integer connections) {
            this.connections = connections;
        }
    }

    /**
     * 注册网关请求
     */
    class RegisterRequest {
        private String endpoint;
        private Integer weight;
        
        // Getters and setters
        public String getEndpoint() {
            return endpoint;
        }
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public Integer getWeight() {
            return weight;
        }
        
        public void setWeight(Integer weight) {
            this.weight = weight;
        }
    }

    /**
     * 注册网关响应
     */
    class RegisterResponse {
        private boolean success;
        private String message;
        
        // Getters and setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
} 