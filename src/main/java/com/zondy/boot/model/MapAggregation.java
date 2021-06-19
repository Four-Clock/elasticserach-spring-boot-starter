package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能描述: MapAggregation
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapAggregation extends BaseCondition {
    private Integer level;
    private Point topLeft;
    private Point bottomRight;
    private Integer aggregationType = 1;
    private String geoPoint;
    private List<QueryItem> intersectQueryItems;

    public boolean checkParam() {
        return !(aggregationType == 0 && (topLeft == null || bottomRight == null));
    }
}