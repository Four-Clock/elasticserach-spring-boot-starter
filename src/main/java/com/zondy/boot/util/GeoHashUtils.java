package com.zondy.boot.util;

import ch.hsr.geohash.GeoHash;
import com.zondy.boot.model.Point;

/**
 * 功能描述: GeoHashUtils
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
public class GeoHashUtils {
    /**
     * 将GeoHash字符串转换中GeoHash中心点坐标
     *
     * @param geohashStr：geohash字符串
     * @return
     */
    public static Point getGeoHashCenterByGeoHashStr(String geohashStr) {
        GeoHash geoHash = GeoHash.fromGeohashString(geohashStr);
        return new Point(geoHash.getBoundingBox().getCenter().getLongitude(), geoHash.getBoundingBox().getCenter().getLatitude());
    }
}
