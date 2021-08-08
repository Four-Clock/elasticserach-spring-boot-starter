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
    private String geoPoint;
    private List<QueryItem> intersectQueryItems;
    private Integer precision;

    public boolean checkParam() {

        return  !checkQueryCondition()||!(topLeft == null || bottomRight == null)
                ||!(level == null || precision == null);
    }
}