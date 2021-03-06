package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 功能描述: SuggestAdvanceCondition
 *
 * @author liqin(zxl)
 * @date 2021/7/9
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SuggestAdvanceCondition extends CommonCondition{
    private List<SuggestItem> suggestItems;
    private Integer limit = 5;
    private String index;
    private String type = "_doc";

    @Override
    public boolean checkQueryCondition() {
        if (CollectionUtils.isEmpty(suggestItems)|| StringUtils.isEmpty(index)) {
            return false;
        }
        return true;
    }
}
