package com.zondy.boot.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能描述: AbstractHeightLightConfig
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
public interface  AbstractHeightLightConfig extends IHighlightEnabled {

    /**
     * 获取高亮参数配置
     * @param matchFieldItems
     * @param matchPhraseFieldItems
     * @param postTags
     * @param preTags
     * @return
     */
     default HighLightConfig getHighLightConfig(List<MatchFieldItem> matchFieldItems, List<MatchPhraseFieldItem> matchPhraseFieldItems, String postTags, String preTags) {
        List<String> heightFields = new ArrayList<>();
        if (matchFieldItems != null && matchFieldItems.size() > 0) {
            for (MatchFieldItem matchFieldItem : matchFieldItems) {
                heightFields.addAll(Arrays.stream(matchFieldItem.getMatchFields()).collect(Collectors.toList()));
            }
        }
        if(matchPhraseFieldItems !=null&& matchPhraseFieldItems.size()>0){
            for (MatchPhraseFieldItem matchFieldItem : matchPhraseFieldItems) {
                heightFields.addAll(Arrays.stream(matchFieldItem.getMatchFields()).collect(Collectors.toList()));
            }
        }
        if(heightFields.size()>0){
            return new HighLightConfig(null, heightFields.toArray(new String[heightFields.size()]), postTags, preTags);
        }else {
            return null;
        }
    }
}
