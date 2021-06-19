package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述: MatchFieldItem
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchFieldItem {
    private String[] matchFields;
    private String queryStr;
    private String[] hightFields;
}
