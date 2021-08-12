package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 功能描述: GeoCondition
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoCondition extends CommonCondition {
    private GeoType geoType;
    private List<Point> points = new ArrayList<>();
    private String geoPoint;
    private double radius;

    @Override
    public boolean checkQueryCondition() {
        if (Objects.isNull(geoType)){
            return true;
        }
        switch (geoType) {
            case rect:
                return points.size() == 2;
            case polygon:
                return points.size() > 1;
            case circle:
                return radius > 0 & points.size() == 1;
            default:
                return false;
        }
    }

}
