package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述: MatchPhraseFieldItem
 *  短语查询
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPhraseFieldItem {
    private String[] matchFields;
    private String queryStr;
    private String[] heightFields;
}
