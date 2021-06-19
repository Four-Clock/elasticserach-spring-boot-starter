package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 功能描述: QueryStringCondition
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryStringCondition  extends CommonCondition implements IHighlightEnabled {
    private String[] matchFields;
    private String queryStr;
    private String[] hightFields;
    private String postTags;
    private String preTags;

    @Override
    public boolean checkQueryCondition() {
        return super.checkQueryCondition();
    }

    @Override
    public HighLightConfig getHighLightConfig() {
        return new HighLightConfig(matchFields, hightFields, postTags, preTags);
    }
}
