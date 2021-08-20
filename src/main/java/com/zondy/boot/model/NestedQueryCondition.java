package com.zondy.boot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * 功能描述: NestedQueryCondition
 *
 * @author liqin(zxl)
 * @date 2021/8/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NestedQueryCondition extends QueryStringAdvanceCondition  {
    private String nestedField;

    private List<QueryItem> nestedQueryItems;

    private List<QueryItem> nestedPrefixItems;

    @Override
    public boolean checkQueryCondition() {
        if (StringUtils.isEmpty(nestedField)
                || (CollectionUtils.isEmpty(nestedQueryItems) && CollectionUtils.isEmpty(nestedPrefixItems))){
            return false;
        }
        return super.checkQueryCondition();
    }

}
