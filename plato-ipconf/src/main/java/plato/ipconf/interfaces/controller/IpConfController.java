package plato.ipconf.interfaces.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plato.ipconf.application.IpConfService;
import plato.ipconf.domain.model.ClientInfo;
import plato.ipconf.domain.model.GatewayEndpoint;
import plato.ipconf.interfaces.dto.ClientInfoDTO;
import plato.ipconf.interfaces.dto.EndpointResponseDTO;
import plato.ipconf.interfaces.dto.EndpointUpdateDTO;
import plato.ipconf.interfaces.dto.ResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IP配置控制器
 * 提供网关地址查询和负载均衡API
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class IpConfController {

    private final IpConfService ipConfService;

    /**
     * 获取最佳网关端点
     * 对应Go版本中的GetIpInfoList API
     *
     * @param clientInfoDTO 客户端信息
     * @return 最佳网关端点列表
     */
    @GetMapping("/ip/list")
    public ResponseEntity<ResponseDTO<List<String>>> getIpInfoList(ClientInfoDTO clientInfoDTO) {
        log.info("Received request for IP list: {}", clientInfoDTO);
        
        ClientInfo clientInfo = convertToClientInfo(clientInfoDTO);
        List<GatewayEndpoint> endpoints = ipConfService.getBestEndpoints(clientInfo, 5); // 默认返回5个端点
        
        List<String> endpointStrings = endpoints.stream()
                .map(GatewayEndpoint::getEndpoint)
                .collect(Collectors.toList());
        
        ResponseDTO<List<String>> response = new ResponseDTO<>("ok", 0, endpointStrings);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取最佳网关端点
     * 兼容旧版API
     *
     * @param clientInfoDTO 客户端信息
     * @return 最佳网关端点列表
     */
    @PostMapping("/api/ipconf/endpoints")
    public ResponseEntity<EndpointResponseDTO> getBestEndpoints(@RequestBody ClientInfoDTO clientInfoDTO) {
        log.info("Received request for best endpoints: {}", clientInfoDTO);
        
        ClientInfo clientInfo = convertToClientInfo(clientInfoDTO);
        List<GatewayEndpoint> endpoints = ipConfService.getBestEndpoints(clientInfo, clientInfoDTO.getLimit());
        
        EndpointResponseDTO response = new EndpointResponseDTO();
        response.setEndpoints(endpoints.stream()
                .map(GatewayEndpoint::getEndpoint)
                .collect(Collectors.toList()));
        response.setTimestamp(System.currentTimeMillis());
        response.setTtl(300); // 5分钟缓存时间
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有网关端点
     *
     * @return 所有网关端点列表
     */
    @GetMapping("/api/ipconf/endpoints/all")
    public ResponseEntity<EndpointResponseDTO> getAllEndpoints() {
        log.info("Received request for all endpoints");
        
        List<GatewayEndpoint> endpoints = ipConfService.getAllEndpoints();
        
        EndpointResponseDTO response = new EndpointResponseDTO();
        response.setEndpoints(endpoints.stream()
                .map(GatewayEndpoint::getEndpoint)
                .collect(Collectors.toList()));
        response.setTimestamp(System.currentTimeMillis());
        response.setTtl(300); // 5分钟缓存时间
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新端点连接数
     *
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    @PostMapping("/api/ipconf/endpoints/update")
    public ResponseEntity<String> updateEndpoint(@RequestBody EndpointUpdateDTO updateDTO) {
        log.info("Received endpoint update: {}", updateDTO);
        
        ipConfService.updateConnectionCount(updateDTO.getEndpoint(), updateDTO.getConnectionCount());
        
        return ResponseEntity.ok("Updated successfully");
    }

    /**
     * 更新端点健康状态
     *
     * @param endpoint 端点地址
     * @param healthy 健康状态
     * @return 更新结果
     */
    @PostMapping("/api/ipconf/endpoints/health")
    public ResponseEntity<String> updateHealthStatus(
            @RequestParam String endpoint,
            @RequestParam boolean healthy) {
        log.info("Received health update for {}: {}", endpoint, healthy);
        
        ipConfService.updateHealthStatus(endpoint, healthy);
        
        return ResponseEntity.ok("Health status updated");
    }

    /**
     * 刷新端点信息
     *
     * @return 刷新结果
     */
    @PostMapping("/api/ipconf/endpoints/refresh")
    public ResponseEntity<String> refreshEndpoints() {
        log.info("Received request to refresh endpoints");
        
        ipConfService.refreshEndpoints();
        
        return ResponseEntity.ok("Endpoints refreshed");
    }

    /**
     * 将DTO转换为领域模型
     *
     * @param dto 客户端信息DTO
     * @return 客户端信息领域模型
     */
    private ClientInfo convertToClientInfo(ClientInfoDTO dto) {
        if (dto == null) {
            return ClientInfo.builder().build();
        }
        
        return ClientInfo.builder()
                .ip(dto.getIp())
                .clientType(dto.getClientType())
                .clientVersion(dto.getClientVersion())
                .networkType(dto.getNetworkType())
                .token(dto.getToken())
                .build();
    }
} 