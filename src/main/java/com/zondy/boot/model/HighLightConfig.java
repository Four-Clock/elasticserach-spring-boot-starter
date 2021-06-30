package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 功能描述: HighLightConfig
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Data
@AllArgsConstructor
public class HighLightConfig {
    private String[] matchFields;
    private String[] hightFields;
    private String postTags;
    private String preTags;
    /**
     * 高亮查询时是否携带原始字段
     */
    private boolean withSourceText;
}