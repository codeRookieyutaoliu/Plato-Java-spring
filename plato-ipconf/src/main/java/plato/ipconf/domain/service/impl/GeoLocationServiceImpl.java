package plato.ipconf.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import plato.ipconf.domain.model.GeoLocation;
import plato.ipconf.domain.service.GeoLocationService;
import plato.ipconf.infrastructure.ip.IpLocationProvider;

/**
 * 地理位置服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationServiceImpl implements GeoLocationService {

    private final IpLocationProvider ipLocationProvider;

    @Override
    @Cacheable(value = "geoLocationCache", key = "#ip", unless = "#result == null")
    public GeoLocation getGeoLocation(String ip) {
        try {
            log.info("Getting geo location for IP: {}", ip);
            return ipLocationProvider.getLocation(ip);
        } catch (Exception e) {
            log.error("Failed to get geo location for IP: {}", ip, e);
            return null;
        }
    }

    @Override
    public double calculateDistance(GeoLocation location1, GeoLocation location2) {
        if (location1 == null || location2 == null) {
            return Double.MAX_VALUE;
        }
        
        // 使用Haversine公式计算两点之间的距离
        double lat1 = Math.toRadians(location1.getLatitude());
        double lon1 = Math.toRadians(location1.getLongitude());
        double lat2 = Math.toRadians(location2.getLatitude());
        double lon2 = Math.toRadians(location2.getLongitude());
        
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // 地球半径（公里）
        double radius = 6371.0;
        
        return radius * c;
    }

    @Override
    public boolean isSameRegion(GeoLocation location1, GeoLocation location2) {
        if (location1 == null || location2 == null) {
            return false;
        }
        
        // 检查是否在同一国家
        if (location1.getCountry() != null && location1.getCountry().equals(location2.getCountry())) {
            // 检查是否在同一省份
            if (location1.getProvince() != null && location1.getProvince().equals(location2.getProvince())) {
                // 检查是否在同一城市
                if (location1.getCity() != null && location1.getCity().equals(location2.getCity())) {
                    return true;
                }
                return true; // 同一省份也算同一区域
            }
            return true; // 同一国家也算同一区域
        }
        
        return false;
    }
} 