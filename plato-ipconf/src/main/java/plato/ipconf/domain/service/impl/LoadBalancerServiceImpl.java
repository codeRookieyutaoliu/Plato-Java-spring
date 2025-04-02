package plato.ipconf.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plato.ipconf.domain.model.ClientInfo;
import plato.ipconf.domain.model.EndpointScore;
import plato.ipconf.domain.model.GatewayEndpoint;
import plato.ipconf.domain.service.GeoLocationService;
import plato.ipconf.domain.service.LoadBalancerService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 负载均衡服务实现
 * 实现了动静结合的排序策略：
 * 1. 动态分数(ActiveScore)：基于节点的实时负载情况，如连接数
 * 2. 静态分数(StaticScore)：基于节点的地理位置和网络质量
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoadBalancerServiceImpl implements LoadBalancerService {

    private final GeoLocationService geoLocationService;

    @Override
    public List<EndpointScore> rankEndpoints(List<GatewayEndpoint> endpoints, ClientInfo clientInfo) {
        if (endpoints == null || endpoints.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.debug("Ranking {} endpoints for client {}", endpoints.size(), clientInfo);
        
        // 计算每个端点的得分并按照动静结合的排序策略排序
        // 首先比较动态活跃度分数，如果相同，则比较静态资源分数
        List<EndpointScore> scores = endpoints.stream()
                .map(endpoint -> calculateScore(endpoint, clientInfo))
                .sorted((s1, s2) -> {
                    if (s1.getActiveScore() != s2.getActiveScore()) {
                        return Double.compare(s2.getActiveScore(), s1.getActiveScore()); // 活跃分数降序
                    }
                    return Double.compare(s2.getStaticScore(), s1.getStaticScore()); // 静态分数降序
                })
                .collect(Collectors.toList());
        
        return scores;
    }

    @Override
    public List<GatewayEndpoint> getBestEndpoints(List<GatewayEndpoint> endpoints, ClientInfo clientInfo, int limit) {
        if (endpoints == null || endpoints.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取排序后的端点评分列表
        List<EndpointScore> rankedScores = rankEndpoints(endpoints, clientInfo);
        
        // 转换为GatewayEndpoint列表
        List<GatewayEndpoint> rankedEndpoints = rankedScores.stream()
                .map(EndpointScore::getEndpoint)
                .collect(Collectors.toList());
        
        // 限制返回数量
        int actualLimit = Math.min(limit > 0 ? limit : 5, rankedEndpoints.size());
        return rankedEndpoints.subList(0, actualLimit);
    }

    /**
     * 计算端点得分
     * 综合考虑负载、地理位置和网络质量
     */
    public EndpointScore calculateScore(GatewayEndpoint endpoint, ClientInfo clientInfo) {
        double loadScore = calculateLoadScore(endpoint);
        double geoScore = calculateGeoScore(endpoint, clientInfo);
        double networkScore = calculateNetworkScore(clientInfo);
        
        // 动态分数主要由负载决定
        double activeScore = loadScore;

        // 静态分数由地理位置和网络质量决定
        double staticScore = geoScore * 0.7 + networkScore * 0.3;
        
        log.debug("Endpoint {} scores: load={}, geo={}, network={}, active={}, static={}",
                endpoint.getEndpoint(), loadScore, geoScore, networkScore, activeScore, staticScore);
        
        return new EndpointScore(endpoint, activeScore, staticScore);
    }

    /**
     * 计算负载得分
     * 连接数越少，得分越高
     */
    private double calculateLoadScore(GatewayEndpoint endpoint) {
        int maxConnections = 10000; // 假设最大连接数为10000
        
        if (maxConnections <= 0) {
            return 1.0; // 防止除以零
        }
        
        // 剩余连接容量的百分比作为得分
        double remainingCapacity = 1.0 - (double) endpoint.getConnectionCount() / maxConnections;
        return Math.max(0.1, remainingCapacity); // 最低得分为0.1
    }

    /**
     * 计算地理位置得分
     * 距离越近，得分越高
     */
    private double calculateGeoScore(GatewayEndpoint endpoint, ClientInfo clientInfo) {
        if (clientInfo == null || clientInfo.getIp() == null || endpoint.getGeoLocation() == null) {
            return 0.5; // 默认中等得分
        }
        
        // 获取客户端地理位置
        var clientGeo = geoLocationService.getGeoLocation(clientInfo.getIp());
        if (clientGeo == null) {
            return 0.5; // 默认中等得分
        }
        
        // 计算地理距离
        double distance = geoLocationService.calculateDistance(clientGeo, endpoint.getGeoLocation());
        
        // 距离越近，得分越高，使用指数衰减函数
        return Math.exp(-distance / 5000.0); // 5000km作为特征距离
    }

    /**
     * 计算网络质量得分
     * 网络类型越好，得分越高
     */
    private double calculateNetworkScore(ClientInfo clientInfo) {
        if (clientInfo == null || clientInfo.getNetworkType() == null) {
            return 0.5; // 默认中等得分
        }
        
        // 根据网络类型评分
        return switch (clientInfo.getNetworkType().toLowerCase()) {
            case "wifi" -> 1.0;
            case "5g" -> 0.9;
            case "4g" -> 0.7;
            case "3g" -> 0.5;
            case "2g" -> 0.3;
            default -> 0.5;
        };
    }
}