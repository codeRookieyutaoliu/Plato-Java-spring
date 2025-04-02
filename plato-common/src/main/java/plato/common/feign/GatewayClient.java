package plato.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Gateway服务的Feign客户端接口
 * 对应Go项目中gateway服务的RPC接口
 */
@FeignClient(name = "plato-gateway")
public interface GatewayClient {

    /**
     * 推送消息到客户端
     * 对应Go项目中的PushMsg方法
     *
     * @param request 推送消息请求
     * @return 推送结果
     */
    @PostMapping("/api/gateway/push")
    PushResponse pushMsg(@RequestBody PushRequest request);

    /**
     * 推送消息请求
     */
    class PushRequest {
        private Long connId;
        private byte[] payload;
        
        // Getters and setters
        public Long getConnId() {
            return connId;
        }
        
        public void setConnId(Long connId) {
            this.connId = connId;
        }
        
        public byte[] getPayload() {
            return payload;
        }
        
        public void setPayload(byte[] payload) {
            this.payload = payload;
        }
    }

    /**
     * 推送消息响应
     */
    class PushResponse {
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