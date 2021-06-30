package com.zondy.boot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

/**
 * 功能描述: MatchCondition
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MatchCondition extends BaseCondition implements IHighlightEnabled {
    private String matchPhrase;
    private String[] multiFields;
    private Boolean isHighLight = true;
    private String postTags;
    private String preTags;
    private boolean isFullMatch = false;
    private boolean withSourceText;

    @Override
    public boolean checkQueryCondition() {
        return StringUtils.isEmpty(matchPhrase) || multiFields == null || multiFields.length == 0;
    }

    @Override
    public HighLightConfig getHighLightConfig() {
        return new HighLightConfig(multiFields, multiFields, postTags, preTags,withSourceText);
    }
}
