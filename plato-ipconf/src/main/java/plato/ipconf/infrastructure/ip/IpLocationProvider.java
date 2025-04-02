package plato.ipconf.infrastructure.ip;

import plato.ipconf.domain.model.GeoLocation;

/**
 * IP位置提供者接口
 * 用于根据IP地址获取地理位置信息
 */
public interface IpLocationProvider {
    
    /**
     * 根据IP地址获取地理位置信息
     * 
     * @param ip IP地址
     * @return 地理位置信息
     */
    GeoLocation getLocation(String ip);
} 