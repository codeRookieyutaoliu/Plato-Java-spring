package plato.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * State服务的Feign客户端接口
 * 对应Go项目中state服务的RPC接口
 */
@FeignClient(name = "plato-state")
public interface StateClient {

    /**
     * 用户登录
     * 对应Go项目中的Login方法
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/api/state/login")
    LoginResponse login(@RequestBody LoginRequest request);

    /**
     * 用户登出
     * 对应Go项目中的Logout方法
     *
     * @param request 登出请求
     * @return 登出结果
     */
    @PostMapping("/api/state/logout")
    LogoutResponse logout(@RequestBody LogoutRequest request);

    /**
     * 发送消息
     * 对应Go项目中的SendMsg方法
     *
     * @param request 发送消息请求
     * @return 发送结果
     */
    @PostMapping("/api/state/send")
    SendResponse sendMsg(@RequestBody SendRequest request);

    /**
     * 登录请求
     */
    class LoginRequest {
        private Long userId;
        private Long connId;
        private String endpoint;
        
        // Getters and setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public Long getConnId() {
            return connId;
        }
        
        public void setConnId(Long connId) {
            this.connId = connId;
        }
        
        public String getEndpoint() {
            return endpoint;
        }
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    /**
     * 登录响应
     */
    class LoginResponse {
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

    /**
     * 登出请求
     */
    class LogoutRequest {
        private Long connId;
        
        // Getters and setters
        public Long getConnId() {
            return connId;
        }
        
        public void setConnId(Long connId) {
            this.connId = connId;
        }
    }

    /**
     * 登出响应
     */
    class LogoutResponse {
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

    /**
     * 发送消息请求
     */
    class SendRequest {
        private Long fromUserId;
        private Long toUserId;
        private byte[] payload;
        
        // Getters and setters
        public Long getFromUserId() {
            return fromUserId;
        }
        
        public void setFromUserId(Long fromUserId) {
            this.fromUserId = fromUserId;
        }
        
        public Long getToUserId() {
            return toUserId;
        }
        
        public void setToUserId(Long toUserId) {
            this.toUserId = toUserId;
        }
        
        public byte[] getPayload() {
            return payload;
        }
        
        public void setPayload(byte[] payload) {
            this.payload = payload;
        }
    }

    /**
     * 发送消息响应
     */
    class SendResponse {
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