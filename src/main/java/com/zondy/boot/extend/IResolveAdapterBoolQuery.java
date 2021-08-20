package com.zondy.boot.extend;

import com.zondy.boot.model.QueryStringCondition;
import org.elasticsearch.index.query.BoolQueryBuilder;

/**
 * 功能描述: IResolveAdapterBoolQuery
 * 给外部提供一个扩展查询条件的入口
 * @author liqin(zxl)
 * @date 2021/8/8
 */
@FunctionalInterface
public interface IResolveAdapterBoolQuery {

    /**
     * 填充BoolQueryBuilder
     * @param boolQuery ：BoolQueryBuilder
     */
    void resolve(BoolQueryBuilder boolQuery, QueryStringCondition queryStringCondition);
}
