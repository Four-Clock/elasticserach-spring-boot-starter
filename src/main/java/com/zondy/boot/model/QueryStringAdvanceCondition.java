package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能描述: QueryStringAdvanceCondition
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryStringAdvanceCondition extends CommonCondition implements AbstractHeightLightConfig {
    private List<MatchFieldItem> matchFieldItems;
    private List<MatchPhraseFieldItem> matchPhraseFieldItems;
    private String postTags;
    private String preTags;

    @Override
    public boolean checkQueryCondition() {
        return super.checkQueryCondition();
    }

    @Override
    public HighLightConfig getHighLightConfig() {
        return getHighLightConfig(matchFieldItems, matchPhraseFieldItems, postTags, preTags);

    }
}
