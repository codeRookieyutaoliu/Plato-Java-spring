package plato.ipconf.infrastructure.ip.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import plato.ipconf.domain.model.GeoLocation;
import plato.ipconf.infrastructure.ip.IpLocationProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认IP位置提供者实现
 * 使用模拟数据提供IP地理位置信息
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultIpLocationProvider implements IpLocationProvider {
    
    // 模拟数据，实际项目中应该使用真实的IP地址库或第三方服务
    private static final Map<String, GeoLocation> MOCK_DATA = new HashMap<>();
    
    static {
        // 添加一些模拟数据
        MOCK_DATA.put("127.0.0.1", GeoLocation.builder()
                .latitude(39.9042)
                .longitude(116.4074)
                .country("China")
                .province("Beijing")
                .city("Beijing")
                .isp("Local")
                .build());
        
        MOCK_DATA.put("192.168.1.1", GeoLocation.builder()
                .latitude(31.2304)
                .longitude(121.4737)
                .country("China")
                .province("Shanghai")
                .city("Shanghai")
                .isp("Local")
                .build());
        
        // 添加更多模拟数据
        MOCK_DATA.put("10.0.0.1", GeoLocation.builder()
                .latitude(22.5431)
                .longitude(114.0579)
                .country("China")
                .province("Guangdong")
                .city("Shenzhen")
                .isp("Local")
                .build());
    }

    @Override
    public GeoLocation getLocation(String ip) {
        // 首先检查模拟数据
        if (MOCK_DATA.containsKey(ip)) {
            log.debug("Using mock data for IP: {}", ip);
            return MOCK_DATA.get(ip);
        }
        
        // 对于未知IP，返回默认位置
        log.debug("Using default location for unknown IP: {}", ip);
        return GeoLocation.builder()
                .latitude(0.0)
                .longitude(0.0)
                .country("Unknown")
                .province("Unknown")
                .city("Unknown")
                .isp("Unknown")
                .build();
    }
} 