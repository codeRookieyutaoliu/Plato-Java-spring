package plato.ipconf.domain.service;

import plato.ipconf.domain.model.GeoLocation;

/**
 * 地理位置服务接口
 * 定义获取IP地址地理位置的功能
 */
public interface GeoLocationService {
    
    /**
     * 根据IP地址获取地理位置信息
     * 
     * @param ip IP地址
     * @return 地理位置信息
     */
    GeoLocation getGeoLocation(String ip);
    
    /**
     * 计算两个地理位置之间的距离（单位：公里）
     * 
     * @param location1 地理位置1
     * @param location2 地理位置2
     * @return 距离（公里）
     */
    double calculateDistance(GeoLocation location1, GeoLocation location2);
    
    /**
     * 判断两个地理位置是否在同一区域
     * 
     * @param location1 地理位置1
     * @param location2 地理位置2
     * @return 是否在同一区域
     */
    boolean isSameRegion(GeoLocation location1, GeoLocation location2);
} 