package com.zondy.boot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述: CommonCondition
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonCondition extends BaseCondition {
    private List<QueryItem> intersectQueryItems;
    private List<QueryItem> unionQueryItems;
    private RangeFilterCondition rangeFilter;
    private List<QueryItem> mustNotItems;
    private List<QueryItem> prefixItems;
    private List<String> nullsItemFields;
    private List<String> emptyItemFields;

    public CommonCondition() {
        this.perSize = 20;
        this.pageIndex = 1;
        this.intersectQueryItems = new ArrayList<>();
        this.unionQueryItems = new ArrayList<>();
        this.mustNotItems = new ArrayList<>();
        this.prefixItems = new ArrayList<>();
        this.nullsItemFields = new ArrayList<>();

    }
}
