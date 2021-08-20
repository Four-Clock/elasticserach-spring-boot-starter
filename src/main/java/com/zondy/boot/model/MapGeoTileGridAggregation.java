package com.zondy.boot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

/**
 * 功能描述: MapGeoTileGridAggregation
 * 网格热力图请求参数
 * @author liqin(zxl)
 * @date 2021/8/8
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MapGeoTileGridAggregation extends QueryStringAdvanceCondition{

    /**
     * 一般指地图缩放级数
     */
    private Integer precision;

    /**
     * 左上角坐标
     */
    private Point topLeft;

    /**
     * 右下角坐标
     */
    private Point bottomRight;

    /**
     * geo_point 类型字段
     */
    private String geoPoint;

    @Override
    public boolean checkQueryCondition() {
        if (precision == null || topLeft == null || bottomRight == null || StringUtils.isEmpty(geoPoint)){
            return false;
        }
        return super.checkQueryCondition();
    }

}
