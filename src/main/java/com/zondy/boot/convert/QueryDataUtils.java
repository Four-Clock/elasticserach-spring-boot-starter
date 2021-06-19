package com.zondy.boot.convert;

import com.zondy.boot.model.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: QueryDataUtils
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
public class QueryDataUtils {

    /**
     * 渲染通用查询条件
     *
     * @param queryStringCondition
     * @param boolQuery
     */
    public static void resolveCommonCondition(CommonCondition queryStringCondition, BoolQueryBuilder boolQuery) {
        if (queryStringCondition.getIntersectQueryItems() != null && queryStringCondition.getIntersectQueryItems().size() > 0) {
            for (QueryItem intersectQueryItem : queryStringCondition.getIntersectQueryItems()) {
                boolQuery.must(QueryBuilders.termsQuery(intersectQueryItem.getField(), intersectQueryItem.getValue()));
            }
        }
        if (queryStringCondition.getUnionQueryItems() != null && queryStringCondition.getUnionQueryItems().size() > 0) {
            for (QueryItem unionQueryItem : queryStringCondition.getUnionQueryItems()) {
                boolQuery.should(QueryBuilders.termsQuery(unionQueryItem.getField(), unionQueryItem.getValue()));
            }
        }
        if (queryStringCondition.getMustNotItems() != null && queryStringCondition.getMustNotItems().size() > 0) {
            for (QueryItem mustNotItem : queryStringCondition.getMustNotItems()) {
                boolQuery.mustNot(QueryBuilders.termsQuery(mustNotItem.getField(), mustNotItem.getValue()));
            }
        }
        if (queryStringCondition.getPrefixItems() != null && queryStringCondition.getPrefixItems().size() > 0) {
            BoolQueryBuilder tmp = QueryBuilders.boolQuery();
            for (QueryItem prefixItem : queryStringCondition.getPrefixItems()) {
                tmp.should(QueryBuilders.prefixQuery(prefixItem.getField(), prefixItem.getValue()[0]));
            }
            boolQuery.must(tmp);
        }
        if (queryStringCondition.getEmptyItemFields() != null && queryStringCondition.getEmptyItemFields().size() > 0) {
            for (String emptyItem : queryStringCondition.getEmptyItemFields()) {
                boolQuery.mustNot(QueryBuilders.boolQuery().should(QueryBuilders.termsQuery(emptyItem, "")));
            }
        }
        if (queryStringCondition.getNullsItemFields() != null && queryStringCondition.getNullsItemFields().size() > 0) {
            for (String nullsItem : queryStringCondition.getNullsItemFields()) {
                boolQuery.must(QueryBuilders.existsQuery(nullsItem));
            }
        }
        if (queryStringCondition.getRangeFilter() != null) {
            if (queryStringCondition.getRangeFilter() != null) {
                sourceRangeFilter(queryStringCondition.getRangeFilter(), boolQuery);
            }
        }
    }

    public static void resolveQueryCondition(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BaseCondition baseCondition) {
        if (baseCondition.getIncludes() != null && baseCondition.getIncludes().length > 0) {
            searchSourceBuilder.fetchSource(baseCondition.getIncludes(), null);
        }
        if (baseCondition.getExcludes() != null && baseCondition.getExcludes().length > 0) {
            searchSourceBuilder.fetchSource(null, baseCondition.getExcludes());
        }
        searchSourceBuilder.size(baseCondition.getPerSize());
        searchSourceBuilder.from(baseCondition.getFrom());
        searchSourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        searchRequest.indices(baseCondition.getIndex());
        searchRequest.types(baseCondition.getType());
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        if (baseCondition.getOrderItem() != null) {
            searchSourceBuilder.sort(baseCondition.getOrderItem().getOrderField(), baseCondition.getOrderItem().getOrder() == OrderEnum.ASC
                    ? SortOrder.ASC : SortOrder.DESC);
        }
        if (baseCondition.getPostFilter() != null) {
            searchSourceBuilder.postFilter(QueryBuilders.termsQuery(baseCondition.getPostFilter().getKey(),
                    baseCondition.getPostFilter().getValues()));
        }
        if (baseCondition.getAggFiledItem() != null) {
            String field = baseCondition.getAggFiledItem().getField();
            OrderItem orderItem = baseCondition.getAggFiledItem().getOrderItem();
            boolean px = orderItem.getOrder() != OrderEnum.DESC;
            searchSourceBuilder.aggregation(AggregationBuilders.terms("_aggs").size(100)
                    .field(field).order(BucketOrder.aggregation(orderItem.getOrderField(), px)));
        }
        searchRequest.source(searchSourceBuilder);
    }

    public static HighlightBuilder getHighlightBuilder(IHighlightEnabled highligh) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighLightConfig highLightConfig = highligh.getHighLightConfig();
        highlightBuilder.postTags(highLightConfig.getPostTags());
        highlightBuilder.preTags(highLightConfig.getPreTags());
        for (String f : highLightConfig.getHightFields()) {
            HighlightBuilder.Field fd = new HighlightBuilder.Field(f);
            highlightBuilder.field(fd);
        }
        return highlightBuilder;
    }

    /**
     * 构造 SearchSourceBuilder
     * @param queryStringCondition
     */
    public static SearchSourceBuilder searchAdvanceBuilder(QueryStringAdvanceCondition queryStringCondition){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        List<MatchFieldItem> matchFieldItems = queryStringCondition.getMatchFieldItems();
        if (matchFieldItems!=null){
            for (MatchFieldItem matchFieldItem : matchFieldItems) {
                String queryStr = matchFieldItem.getQueryStr();
                if (!StringUtils.isEmpty(queryStr)) {
                    String[] matchFields = matchFieldItem.getMatchFields();
                    QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(queryStr);
                    if (matchFields != null && matchFields.length > 0) {
                        Map<String, Float> fields = new HashMap<>();
                        for (String matchField : matchFields) {
                            fields.put(matchField, 1.0f);
                        }
                        queryBuilder.fields(fields);
                    }
                    boolQuery.must(queryBuilder);
                }
            }
        }
        List<MatchPhraseFieldItem> mplist=queryStringCondition.getMatchPhraseFieldItems();
        if(mplist!=null && mplist.size()>0){
            for(MatchPhraseFieldItem item:mplist){
                String queryStr=item.getQueryStr();
                if (StringUtils.isEmpty(queryStr)){
                    continue;
                }
                String[] field=item.getMatchFields();
                if (null==field || field.length == 0 ){
                    continue;
                }
                if(field.length==1){
                    boolQuery.must(QueryBuilders.matchPhraseQuery(field[0],queryStr));
                }else{
                    BoolQueryBuilder subboolQuery = QueryBuilders.boolQuery();
                    for(String f:field) {
                        subboolQuery.should(QueryBuilders.matchPhraseQuery(f,queryStr));
                    }
                    boolQuery.must(subboolQuery);
                }
            }
        }
        QueryDataUtils.resolveCommonCondition(queryStringCondition, boolQuery);
        if (queryStringCondition.getHighLightConfig() != null && queryStringCondition.getHighLightConfig().getHightFields() != null) {
            HighlightBuilder highlightBuilder = getHighlightBuilder(queryStringCondition);
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        searchSourceBuilder.query(boolQuery);
        return searchSourceBuilder;
    }

    /**
     * 构造 SearchSourceBuilder
     * @param queryStringCondition 查询条件
     */
    public static SearchSourceBuilder searchBuilder(QueryStringCondition queryStringCondition){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(queryStringCondition.getQueryStr())) {
            QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(queryStringCondition.getQueryStr());
            String[] matchFields = queryStringCondition.getMatchFields();
            if ( matchFields != null && matchFields.length > 0) {
                Map<String, Float> fields = new HashMap<>();
                for (String matchField : queryStringCondition.getMatchFields()) {
                    fields.put(matchField, 1.0f);
                }
                queryBuilder.fields(fields);
            }
            boolQuery.must(queryBuilder);
        }
        QueryDataUtils.resolveCommonCondition(queryStringCondition, boolQuery);
        if (queryStringCondition.getHightFields() != null && queryStringCondition.getHightFields().length > 0) {
            HighlightBuilder highlightBuilder = getHighlightBuilder(queryStringCondition);
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        searchSourceBuilder.query(boolQuery);
        return searchSourceBuilder;
    }


    /**
     * 构造SearchSourceBuilder
     * @param geoQueryCondition：查询条件
     * @return SearchSourceBuilder
     */
    public static SearchSourceBuilder searchGeoBuilder(GeoCondition geoQueryCondition,BoolQueryBuilder queryBuilder){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        geo(geoQueryCondition, queryBuilder, searchSourceBuilder);
        return searchSourceBuilder;
    }

    /**
     * 构造SearchSourceBuilder
     * @param geoQueryCondition 查询条件
     */
    public static SearchSourceBuilder searchGeoMatchBuilder(GeoMatchQueryCondition geoQueryCondition){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        String queryStr = geoQueryCondition.getQueryStr();
        if (!StringUtils.isEmpty(queryStr)) {
            QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(queryStr);
            if (geoQueryCondition.getMatchFields() != null && geoQueryCondition.getMatchFields().length > 0) {
                Map<String, Float> fields = new HashMap<>();
                for (String matchField : geoQueryCondition.getMatchFields()) {
                    fields.put(matchField, 1.5f);
                }
                queryStringQueryBuilder.fields(fields);
            }
            queryBuilder.must(queryStringQueryBuilder);
        }
        BoolQueryBuilder geoBoolQueryBuilder = QueryBuilders.boolQuery();
        geo(geoQueryCondition, geoBoolQueryBuilder, searchSourceBuilder);
        queryBuilder.must(geoBoolQueryBuilder);
        QueryDataUtils.resolveCommonCondition(geoQueryCondition, queryBuilder);
        if (geoQueryCondition.getHightFields() != null && geoQueryCondition.getHightFields().length > 0) {
            HighlightBuilder highlightBuilder = QueryDataUtils.getHighlightBuilder(geoQueryCondition);
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        searchSourceBuilder.query(queryBuilder);
        return searchSourceBuilder;
    }

    /**
     * 构造SearchRequest
     * @param suggestCondition 查询条件
     * @return SearchRequest
     */
    public static SearchRequest toSuggestRequest(SuggestCondition suggestCondition){
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion(suggestCondition.getFiled()).skipDuplicates(true)
                .prefix(suggestCondition.getValue()).size(suggestCondition.getLimit());
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("suggest", suggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(suggestCondition.getIndex());
        searchRequest.types(suggestCondition.getType());
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        return searchRequest;
    }

    private static void geo(GeoCondition geoQueryCondition, BoolQueryBuilder queryBuilder, SearchSourceBuilder searchSourceBuilder) {
        if (geoQueryCondition.getGeoType() == GeoType.rect) {
            queryBuilder.filter(QueryBuilders.geoBoundingBoxQuery(geoQueryCondition.getGeoPoint())
                    .setCorners(new GeoPoint(geoQueryCondition.getPoints().get(0).getLat(),
                                    geoQueryCondition.getPoints().get(0).getLon()),
                            new GeoPoint(geoQueryCondition.getPoints().get(1).getLat(),
                                    geoQueryCondition.getPoints().get(1).getLon())));
        }
        if (geoQueryCondition.getGeoType() == GeoType.circle) {
            queryBuilder.filter(QueryBuilders.geoDistanceQuery(geoQueryCondition.getGeoPoint()).distance(
                    geoQueryCondition.getRadius(), DistanceUnit.METERS
            ).point(new GeoPoint(geoQueryCondition.getPoints().get(0).getLat(),
                    geoQueryCondition.getPoints().get(0).getLon())));
            //按离中心点距离排序
            GeoDistanceSortBuilder sort=new GeoDistanceSortBuilder(geoQueryCondition.getGeoPoint(),geoQueryCondition.getPoints().get(0).getLat(),geoQueryCondition.getPoints().get(0).getLon());
            sort.unit(DistanceUnit.METERS);
            sort.order(SortOrder.ASC);
            sort.point(geoQueryCondition.getPoints().get(0).getLat(),geoQueryCondition.getPoints().get(0).getLon());
            sort.geoDistance(GeoDistance.ARC);
            searchSourceBuilder.sort(sort);
        }
        if (geoQueryCondition.getGeoType() == GeoType.polygon) {
            queryBuilder.filter(QueryBuilders.geoPolygonQuery(geoQueryCondition.getGeoPoint(), convertGeoPoints(geoQueryCondition.getPoints())));
        }
    }

    public static Integer recallLevel(Integer level) {
        int prec = 5;
        if (level <= 2) {
            prec = 1;
        } else if (level <= 5) {
            prec = 2;
        } else if (level <= 8) {
            prec = 3;
        } else if (level <= 11) {
            prec = 5;
        } else if (level <= 13) {
            prec = 6;
        } else if (level <= 15) {
            prec = 7;
        } else {
            prec = 8;
        }
        return prec;
    }

    private static List<GeoPoint> convertGeoPoints(List<Point> points) {
        List<GeoPoint> rt = new ArrayList<>();
        points.forEach(t -> rt.add(new GeoPoint(t.getLat(), t.getLon())));
        return rt;
    }

    private static void sourceRangeFilter(RangeFilterCondition rangeFilter, BoolQueryBuilder boolQueryBuilder) {
        String field = rangeFilter.getField();
        Object max = rangeFilter.getMax();
        Object min = rangeFilter.getMin();
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field);
        if (max != null && !"".equals(max)) {
            rangeQueryBuilder.to(max);
        }
        if (min != null && !"".equals(min)) {
            rangeQueryBuilder.from(min);
        }
        boolQueryBuilder.must(rangeQueryBuilder);
    }
}
