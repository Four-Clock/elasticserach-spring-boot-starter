package com.zondy.boot.model;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 功能描述: BaseCondition
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Data
public class BaseCondition {
    protected Integer pageIndex = 0;
    protected Integer perSize = 10;
    protected String index;
    protected String type = "_doc";
    protected String[] includes;
    protected String[] excludes;
    protected OrderItem orderItem;
    protected PostFilterCondition postFilter;
    protected AggFiledItem aggFiledItem;

    protected boolean checkQueryCondition() {
        return !StringUtils.isEmpty(index);
    }

    public Integer getFrom() {
        return (this.pageIndex - 1) >= 0 ? (this.pageIndex - 1) * this.perSize : 0;
    }
}
