package com.zondy.boot.extend;

import com.zondy.boot.model.MapGeoTileGridAggregation;
import com.zondy.boot.model.NestedQueryCondition;
import com.zondy.boot.model.Point;
import com.zondy.boot.model.QueryItem;
import com.zondy.boot.model.QueryStringCondition;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * 功能描述: IResolveAdapterBoolQuery
 *
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



    IResolveAdapterBoolQuery DEFAULT_RESOLVE_ADAPTER_BOOL_QUERY = (boolQuery,queryStringCondition)->{
        if (queryStringCondition instanceof MapGeoTileGridAggregation){
            MapGeoTileGridAggregation mapGeoTileGridAggregation =  (MapGeoTileGridAggregation)queryStringCondition;
            Point bottomRight = mapGeoTileGridAggregation.getBottomRight();
            Point topLeft = mapGeoTileGridAggregation.getTopLeft();

            boolQuery.filter(QueryBuilders.geoBoundingBoxQuery(mapGeoTileGridAggregation.getGeoPoint())
            .setCorners(new GeoPoint(topLeft.getLat(),topLeft.getLon())
                    ,new GeoPoint(bottomRight.getLat(),bottomRight.getLon())));

        }
        if (queryStringCondition instanceof  NestedQueryCondition){
              NestedQueryCondition nestedQueryCondition = (NestedQueryCondition) queryStringCondition;
              BoolQueryBuilder nested = QueryBuilders.boolQuery();
              for (QueryItem nestedQueryItem : nestedQueryCondition.getNestedQueryItems()) {
                  nested.must(QueryBuilders.termsQuery(nestedQueryItem.getField(),nestedQueryItem.getValue()));
             }
             boolQuery.must(QueryBuilders.nestedQuery(nestedQueryCondition.getNestedField(),nested, ScoreMode.None));
        }
    };
}
