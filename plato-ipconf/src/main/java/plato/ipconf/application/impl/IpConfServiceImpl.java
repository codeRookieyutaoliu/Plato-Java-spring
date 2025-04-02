package plato.ipconf.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plato.ipconf.application.IpConfService;
import plato.ipconf.domain.model.ClientInfo;
import plato.ipconf.domain.model.GatewayEndpoint;
import plato.ipconf.domain.model.GeoLocation;
import plato.ipconf.domain.service.EndpointService;
import plato.ipconf.domain.service.GeoLocationService;

import java.util.Collections;
import java.util.List;

/**
 * IP配置应用服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpConfServiceImpl implements IpConfService {

    private final EndpointService endpointService;
    private final GeoLocationService geoLocationService;

    @Override
    public List<GatewayEndpoint> getAllEndpoints() {
        return endpointService.getAllEndpoints();
    }

    @Override
    public List<GatewayEndpoint> getEndpointsByService(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return Collections.emptyList();
        }
        return endpointService.getEndpointsByService(serviceName);
    }

    @Override
    public List<GatewayEndpoint> getBestEndpoints(ClientInfo clientInfo) {
        return getBestEndpoints(clientInfo, 3); // 默认返回3个端点
    }

    @Override
    public List<GatewayEndpoint> getBestEndpoints(ClientInfo clientInfo, int limit) {
        if (clientInfo == null) {
            log.warn("Client info is null");
            return Collections.emptyList();
        }
        
        // 如果客户端信息中没有地理位置信息，则根据IP获取
        if (clientInfo.getGeoLocation() == null && clientInfo.getIp() != null) {
            GeoLocation geoLocation = geoLocationService.getGeoLocation(clientInfo.getIp());
            clientInfo.setGeoLocation(geoLocation);
        }
        
        log.info("Getting best endpoints for client: {}, limit: {}", clientInfo, limit);
        return endpointService.getBestEndpoints(clientInfo, limit);
    }

    @Override
    public void updateConnectionCount(String endpoint, int connectionCount) {
        if (endpoint == null || endpoint.isEmpty()) {
            log.warn("Endpoint is null or empty");
            return;
        }
        
        if (connectionCount < 0) {
            log.warn("Connection count is negative: {}", connectionCount);
            return;
        }
        
        log.info("Updating connection count for endpoint: {}, count: {}", endpoint, connectionCount);
        endpointService.updateConnectionCount(endpoint, connectionCount);
    }

    @Override
    public void updateHealthStatus(String endpoint, boolean healthy) {
        if (endpoint == null || endpoint.isEmpty()) {
            log.warn("Endpoint is null or empty");
            return;
        }
        
        log.info("Updating health status for endpoint: {}, healthy: {}", endpoint, healthy);
        endpointService.updateHealthStatus(endpoint, healthy);
    }

    @Override
    public void refreshEndpoints() {
        log.info("Refreshing endpoints");
        endpointService.refreshEndpoints();
    }
} 