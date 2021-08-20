package com.zondy.boot.extend;

import com.zondy.boot.model.MapGeoTileGridAggregation;
import com.zondy.boot.model.NestedQueryCondition;
import com.zondy.boot.model.Point;
import com.zondy.boot.model.QueryItem;
import com.zondy.boot.model.QueryStringAdvanceCondition;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.CollectionUtils;

/**
 * 功能描述: IResolveAdapterAdvanceBoolQuery
 * 给外部提供一个扩展查询条件的入口 兼容 QueryStringAdvanceCondition
 * @author liqin(zxl)
 * @date 2021/8/19
 */
public interface IResolveAdapterAdvanceBoolQuery {

    /**
     * 填充 BoolQueryBuilder
     * @param boolQuery ：BoolQueryBuilder
     */
    void resolve(BoolQueryBuilder boolQuery, QueryStringAdvanceCondition queryStringAdvanceCondition);

    IResolveAdapterAdvanceBoolQuery DEFAULT_RESOLVE_ADAPTER_BOOL_QUERY = (boolQuery,queryStringAdvanceCondition)->{

        if (queryStringAdvanceCondition instanceof NestedQueryCondition){
            NestedQueryCondition nestedQueryCondition = (NestedQueryCondition) queryStringAdvanceCondition;
            BoolQueryBuilder nested = QueryBuilders.boolQuery();
            if (!CollectionUtils.isEmpty(nestedQueryCondition.getNestedQueryItems())){
                for (QueryItem nestedQueryItem : nestedQueryCondition.getNestedQueryItems()) {
                    nested.must(QueryBuilders.termsQuery(nestedQueryItem.getField(),nestedQueryItem.getValue()));
                }
            }
            if (!CollectionUtils.isEmpty(nestedQueryCondition.getNestedPrefixItems())){
                for (QueryItem nestedQueryItem : nestedQueryCondition.getNestedPrefixItems()) {
                    nested.must(QueryBuilders.prefixQuery(nestedQueryItem.getField(),nestedQueryItem.getValue()[0]));
                }
            }
            boolQuery.must(QueryBuilders.nestedQuery(nestedQueryCondition.getNestedField(),nested, ScoreMode.None));
        }
        if (queryStringAdvanceCondition instanceof MapGeoTileGridAggregation){
            MapGeoTileGridAggregation mapGeoTileGridAggregation =  (MapGeoTileGridAggregation)queryStringAdvanceCondition;
            Point bottomRight = mapGeoTileGridAggregation.getBottomRight();
            Point topLeft = mapGeoTileGridAggregation.getTopLeft();

            boolQuery.filter(QueryBuilders.geoBoundingBoxQuery(mapGeoTileGridAggregation.getGeoPoint())
                    .setCorners(new GeoPoint(topLeft.getLat(),topLeft.getLon())
                            ,new GeoPoint(bottomRight.getLat(),bottomRight.getLon())));
        }
    };
}
