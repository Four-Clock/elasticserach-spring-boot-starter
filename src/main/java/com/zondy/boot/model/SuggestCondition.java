package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 功能描述: SuggestCondition
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SuggestCondition {
    private String filed;
    private String value;
    private Integer limit = 5;
    private String index;
    private String type = "_doc";

    public boolean checkQueryCondition() {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(filed) || StringUtils.isEmpty(index)) {
            return false;
        }
        return true;
    }
}