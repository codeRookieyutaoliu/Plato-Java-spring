package plato.state.interfaces.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plato.common.feign.StateClient;
import plato.state.application.service.StateService;

/**
 * State服务的Controller
 * 处理来自其他服务的RPC调用
 */
@Slf4j
@RestController
@RequestMapping("/api/state")
@RequiredArgsConstructor
public class StateController {

    private final StateService stateService;

    /**
     * 用户登录
     * 对应Go项目中的Login方法
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public StateClient.LoginResponse login(@RequestBody StateClient.LoginRequest request) {
        log.info("收到用户登录请求，用户ID：{}，连接ID：{}，终端：{}", 
                request.getUserId(), request.getConnId(), request.getEndpoint());
        
        StateClient.LoginResponse response = new StateClient.LoginResponse();
        
        try {
            boolean result = stateService.login(request.getUserId(), request.getConnId(), request.getEndpoint());
            response.setSuccess(result);
            response.setMessage(result ? "用户登录成功" : "用户登录失败");
        } catch (Exception e) {
            log.error("用户登录异常", e);
            response.setSuccess(false);
            response.setMessage("用户登录异常：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 用户登出
     * 对应Go项目中的Logout方法
     *
     * @param request 登出请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    public StateClient.LogoutResponse logout(@RequestBody StateClient.LogoutRequest request) {
        log.info("收到用户登出请求，连接ID：{}", request.getConnId());
        
        StateClient.LogoutResponse response = new StateClient.LogoutResponse();
        
        try {
            boolean result = stateService.logout(request.getConnId());
            response.setSuccess(result);
            response.setMessage(result ? "用户登出成功" : "用户登出失败");
        } catch (Exception e) {
            log.error("用户登出异常", e);
            response.setSuccess(false);
            response.setMessage("用户登出异常：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 发送消息
     * 对应Go项目中的SendMsg方法
     *
     * @param request 发送消息请求
     * @return 发送结果
     */
    @PostMapping("/send")
    public StateClient.SendResponse sendMsg(@RequestBody StateClient.SendRequest request) {
        log.info("收到发送消息请求，发送者ID：{}，接收者ID：{}，消息长度：{}", 
                request.getFromUserId(), request.getToUserId(), request.getPayload().length);
        
        StateClient.SendResponse response = new StateClient.SendResponse();
        
        try {
            boolean result = stateService.sendMessage(
                    request.getFromUserId(), request.getToUserId(), request.getPayload());
            response.setSuccess(result);
            response.setMessage(result ? "消息发送成功" : "消息发送失败");
        } catch (Exception e) {
            log.error("发送消息异常", e);
            response.setSuccess(false);
            response.setMessage("发送消息异常：" + e.getMessage());
        }
        
        return response;
    }
} 