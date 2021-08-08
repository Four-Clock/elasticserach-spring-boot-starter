package com.zondy.boot.service;

import com.alibaba.fastjson.JSON;
import com.zondy.boot.autoconfigure.ElasticsearchProperties;
import com.zondy.boot.bean.PageView;
import com.zondy.boot.convert.ConvertDataUtils;
import com.zondy.boot.convert.QueryDataUtils;
import com.zondy.boot.convert.ResponseDataUtils;
import com.zondy.boot.extend.IResolveAdapterESDataRecord;
import com.zondy.boot.factory.ElasticSearchClientFactory;
import com.zondy.boot.model.CommonCondition;
import com.zondy.boot.model.FieldType;
import com.zondy.boot.model.GeoCondition;
import com.zondy.boot.model.GeoMatchQueryCondition;
import com.zondy.boot.model.HighLightConfig;
import com.zondy.boot.model.MapAggregation;
import com.zondy.boot.model.MatchCondition;
import com.zondy.boot.model.QueryItem;
import com.zondy.boot.model.QueryStringAdvanceCondition;
import com.zondy.boot.model.QueryStringCondition;
import com.zondy.boot.model.SuggestAdvanceCondition;
import com.zondy.boot.model.SuggestCondition;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 功能描述: ElasticsearchService
 *
 * @author liqin(zxl)
 * @date 2021/6/17
 */
public class ElasticsearchService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchService.class);

    private RestHighLevelClient restHighLevelClient;

    private ResponseDataUtils responseDataUtils;

    public ElasticsearchService(ElasticsearchProperties elasticsearchProperties){
        ElasticSearchClientFactory elasticSearchClientFactory = new ElasticSearchClientFactory(elasticsearchProperties);
        restHighLevelClient = elasticSearchClientFactory.restHighLevelClient();
        responseDataUtils = new ResponseDataUtils(restHighLevelClient);
    }

    /**
     * 创建索引
     *
     * @param index：索引名称
     * @param shards：分片数量
     * @param replicas：副本数量
     * @param fields：字段信息
     * @return CreateIndexRequest
     */
    public boolean createIndex(String index, Integer shards, Integer replicas, List<FieldType> fields){
        if (fields == null || fields.size() == 0) {
            return false;
        }
        try {
            CreateIndexRequest request = ConvertDataUtils.createIndexRequest(index, shards, replicas, fields);
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged() && response.isShardsAcknowledged();
        }catch (Exception ex){
            LOGGER.error("createIndex error index:{},shards:{},replicas:{},fields:{}"
            ,index,shards,replicas, JSON.toJSONString(fields),ex);
            return false;
        }
    }

    /**
     *  创建索引
     * @param index：索引名称
     * @param shards：分片数量
     * @param replicas：副本数量
     * @return true
     */
    public boolean createIndex(String index, Integer shards, Integer replicas){
        if (StringUtils.isEmpty(index)){
            return false;
        }
        try {
            CreateIndexRequest request = ConvertDataUtils.createIndexRequest(index,shards,replicas);
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged() && createIndexResponse.isShardsAcknowledged();
        } catch (Exception ex) {
            LOGGER.error("createIndex error index:{},shards:{},replicas:{},fields:{}"
                    ,index,shards,replicas,ex);
            return false;
        }
    }

    /**
     * 判断指定索引是否存在
     * @param index:索引名称
     * @return true
     */
    public boolean checkIndexIsExist(String index){
        GetIndexRequest request = new GetIndexRequest().indices(index);
        request.includeDefaults(true);
        try {
            GetIndexResponse response = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT);
            return response.indices()!=null &&response.indices().length>0;
        } catch (Exception ex) {
            LOGGER.error("checkIndexIsExist error index:{}", index, ex);
            return false;
        }
    }

    /**
     * 删除指定索引
     *
     * @param index：索引名称
     * @return true
     */
    public boolean deleteIndex(String index){
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        try {
            AcknowledgedResponse deleteIndexResponse = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
            return deleteIndexResponse.isAcknowledged();
        } catch (Exception ex) {
            LOGGER.error("deleteIndex error index:{}", index, ex);
            return false;
        }
    }


    /**
     * 向指定索引中添加文档数据
     *
     * @param index：索引名称
     * @param dataMap：文档数据
     * @return
     */
    public boolean commitDocument(String index, Map<String, Object> dataMap) {
        if (dataMap.isEmpty()) {
            return false;
        }
        try {
            IndexRequest request = ConvertDataUtils.indexRequest(index, dataMap);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
            return true;
        } catch (Exception ex) {
            LOGGER.error("commitDocument error index:{},dataMap:{}", index,JSON.toJSONString(dataMap), ex);
            return false;
        }
    }

    /**
     * 批量提交索引文档
     *
     * @param index：索引名称
     * @param dataList：文档数据
     * @return true
     */
    public boolean commitDocument(String index, List<Map<String, Object>> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return false;
        }
        try {
            BulkRequest request = ConvertDataUtils.bulkRequest(index, dataList);
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            return true;
        } catch (Exception ex) {
            LOGGER.error("commitDocument is error index{}, dataList:{}:" ,index,JSON.toJSONString(dataList), ex);
            return false;
        }
    }


    /**
     * 更新索引数据
     *
     * @param index：索引名称
     * @param dataMap：文档数据
     * @return
     */
    public boolean updateDocument(String index, String id, Map<String, Object> dataMap) {
        if (dataMap.isEmpty() || StringUtils.isEmpty(id)) {
            return false;
        }
        try {
            restHighLevelClient.update(
                    ConvertDataUtils.updateRequest(index, id, dataMap), RequestOptions.DEFAULT);
            return true;
        } catch (Exception ex) {
            LOGGER.error("commitDocument is error index:{}, id:{},dataMap:{}" ,index,id,JSON.toJSONString(dataMap), ex);
            return false;
        }
    }

    /**
     * 批量更新ES数据
     *
     * @param index：索引名称
     * @param dataList：文档数据
     * @return
     */
    public boolean updateDocument(String index, List<Map<String, Map<String, Object>>> dataList) {
        if (dataList.isEmpty() || StringUtils.isEmpty(index)) {
            return false;
        }
        try {
            BulkRequest bulkRequest = ConvertDataUtils.bulkBatchRequest(index, dataList);
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return true;
        } catch (Exception ex) {
            LOGGER.error("updateDocument error index:{},dataList:{}" , index,JSON.toJSONString(dataList), ex);
            return false;
        }
    }

    /**
     * 删除文档
     * @param index:索引Index
     * @param docId：文档ID
     * @return true
     */
    public boolean deleteDocument(String index, String docId){
        if (StringUtils.isEmpty(docId) || StringUtils.isEmpty(index)) {
            return false;
        }
        try {
            DeleteRequest request = ConvertDataUtils.deleteRequest(index, docId);
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            return true;
        } catch (Exception ex) {
            LOGGER.error("delete document is error  index:{},docId:{}" , index,docId, ex);
            return false;
        }
    }

    /**
     * 根据查询条件批量删除数据
     *
     * @param commonCondition
     * @return true
     */
    public boolean deleteDocumentByQueryRequest(CommonCondition commonCondition) {
        DefaultKeyValue keyValue =new DefaultKeyValue();
        keyValue.setKey(true);
        try {
            DeleteByQueryRequest request = ConvertDataUtils.deleteByQueryRequest(commonCondition);
            restHighLevelClient.deleteByQueryAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkByScrollResponse>() {
                @Override
                public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                    LOGGER.info("delete document success:{}" , bulkByScrollResponse.getDeleted());
                }
                @Override
                public void onFailure(Exception e) {
                    keyValue.setKey(false);
                }
            });
        } catch (Exception ex) {
            LOGGER.error("delete document falied" ,ex);
            keyValue.setKey(false);
        }
        return (Boolean) keyValue.getKey();
    }

    /**
     * 批量删除文档数据
     *
     * @param index
     * @param docIds
     * @return
     */
    public boolean deleteDocument(String index, List<String> docIds){
        if (docIds.isEmpty() || StringUtils.isEmpty(index)) {
            return false;
        }
        try {
            BulkRequest request = ConvertDataUtils.bulkDeleteRequest(index, docIds);
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            return true;
        } catch (Exception ex) {
            LOGGER.info("delete document error index:{},docIds:{}" ,index,JSON.toJSONString(docIds),ex);
            return false;
        }
    }

    /**
     * 根据条件查询文档数据
     *
     * @param commonQueryCondition
     * @return
     */
    public PageView<Map<String, Object>> queryDocument(CommonCondition commonQueryCondition, IResolveAdapterESDataRecord... resolve) {
        PageView<Map<String, Object>> elasticResponseData = new PageView<>();
        elasticResponseData.setPerSize(commonQueryCondition.getPerSize());
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.trackTotalHits(true);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        QueryDataUtils.resolveCommonCondition(commonQueryCondition, queryBuilder);
        searchSourceBuilder.query(queryBuilder);
        QueryDataUtils.resolveQueryCondition(searchRequest, searchSourceBuilder, commonQueryCondition);
        try {
            responseDataUtils.wrapElasticResponse(searchRequest, elasticResponseData, null, resolve);
        } catch (Exception ex) {
            LOGGER.error("query document is error commonQueryCondition:{}" ,JSON.toJSONString(commonQueryCondition), ex);
            throw new RuntimeException(ex);
        }
        return elasticResponseData;
    }

    /**
     * 模糊检索文档数据
     *
     * @param matchQueryCondition
     * @return PageView<Map<String, Object>>
     */
    public PageView<Map<String, Object>> matchQuery(MatchCondition matchQueryCondition, IResolveAdapterESDataRecord... resolve) {
        if (!matchQueryCondition.checkQueryCondition()) {
            throw new IllegalArgumentException("查询参数有误，请检查查询参数");
        }
        PageView<Map<String, Object>> elasticResponseData = new PageView<>();
        elasticResponseData.setPerSize(matchQueryCondition.getPerSize());
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (matchQueryCondition.isFullMatch()) {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            for (String field : matchQueryCondition.getMultiFields()) {
                boolQuery.should(QueryBuilders.termQuery(field, matchQueryCondition.getMatchPhrase()));
            }
            searchSourceBuilder.query(boolQuery);
        } else {
            if (matchQueryCondition.getMultiFields().length == 1) {
                searchSourceBuilder.query(QueryBuilders.matchQuery(matchQueryCondition.getMultiFields()[0], matchQueryCondition.getMatchPhrase()));
            } else {
                searchSourceBuilder.query(QueryBuilders.multiMatchQuery(matchQueryCondition.getMatchPhrase(), matchQueryCondition.getMultiFields()));
            }
        }
        if (matchQueryCondition.getIsHighLight()) {
            HighlightBuilder highlightBuilder =  QueryDataUtils.getHighlightBuilder(matchQueryCondition);
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        QueryDataUtils.resolveQueryCondition(searchRequest, searchSourceBuilder, matchQueryCondition);
        try {
            responseDataUtils.wrapElasticResponse(searchRequest, elasticResponseData, matchQueryCondition.getHighLightConfig(), resolve);
        } catch (Exception ex) {
            LOGGER.error("query document is exception :" + ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return elasticResponseData;
    }

    /**
     * 多条件高亮查询文档数据
     *
     * @param queryStringCondition：查询条件参数
     * @return
     */
    public PageView<Map<String, Object>> matchQueryString(QueryStringCondition queryStringCondition, IResolveAdapterESDataRecord... resolve){
        if (!queryStringCondition.checkQueryCondition()) {
            throw new IllegalArgumentException("查询参数有误，请检查查询参数");
        }
        PageView<Map<String, Object>> elasticResponseData = new PageView<>();
        elasticResponseData.setPerSize(queryStringCondition.getPerSize());
        SearchRequest searchRequest = new SearchRequest();
        QueryDataUtils.resolveQueryCondition(searchRequest, QueryDataUtils.searchBuilder(queryStringCondition), queryStringCondition);
        try {
            responseDataUtils.wrapElasticResponse(searchRequest, elasticResponseData, queryStringCondition.getHighLightConfig(), resolve);
        } catch (Exception ex) {
            LOGGER.error("query document is error  queryStringCondition:{}" ,JSON.toJSONString(queryStringCondition), ex);
            throw new RuntimeException(ex.getMessage());
        }
        return elasticResponseData;
    }

    /**
     * 多条件高亮查询文档数据，同时支持多个matchfield的交集操作
     *
     * @param queryStringCondition 查询条件
     * @param resolve 装饰函数
     * @return PageView<Map<String, Object>>
     */
    public PageView<Map<String, Object>> matchAdvanceQueryString(QueryStringAdvanceCondition queryStringCondition, IResolveAdapterESDataRecord... resolve) {
        if (!queryStringCondition.checkQueryCondition()) {
            throw new IllegalArgumentException("查询参数有误，请检查查询参数是否合法");
        }
        PageView<Map<String, Object>> elasticResponseData = new PageView<>();
        elasticResponseData.setPerSize(queryStringCondition.getPerSize());
        SearchRequest searchRequest = new SearchRequest();
        QueryDataUtils.resolveQueryCondition(searchRequest, QueryDataUtils.searchAdvanceBuilder(queryStringCondition), queryStringCondition);
        try {
            responseDataUtils.wrapElasticResponse(searchRequest, elasticResponseData, queryStringCondition.getHighLightConfig(), resolve);
        } catch (Exception ex) {
            LOGGER.error("query document is error :{}" ,JSON.toJSONString(queryStringCondition), ex);
            throw new RuntimeException(ex.getMessage());
        }
        return elasticResponseData;
    }

    /**
     * 地理查询
     *
     * @param geoQueryCondition：地理查询条件
     * @return PageView<Map<String, Object>>
     */
    public PageView<Map<String, Object>> geoQuery(GeoCondition geoQueryCondition, IResolveAdapterESDataRecord... resolve){
        if (!geoQueryCondition.checkQueryCondition()) {
            throw new IllegalArgumentException("查询参数有误，请检查查询参数");
        }
        PageView<Map<String, Object>> elasticResponse = new PageView<>();
        SearchRequest searchRequest = new SearchRequest();
        elasticResponse.setPerSize(geoQueryCondition.getPerSize());
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder searchSourceBuilder = QueryDataUtils.searchGeoBuilder(geoQueryCondition,queryBuilder);
        QueryDataUtils.resolveCommonCondition(geoQueryCondition, queryBuilder);
        searchSourceBuilder.query(queryBuilder);
        QueryDataUtils. resolveQueryCondition(searchRequest, searchSourceBuilder, geoQueryCondition);
        try {
           responseDataUtils.wrapElasticResponse(searchRequest, elasticResponse, null, resolve);
            return elasticResponse;
        } catch (Exception ex) {
            LOGGER.error("geoQuery document is error :{}" ,JSON.toJSONString(geoQueryCondition), ex);
            throw new RuntimeException(ex);
        }
    }
    /**
     * 地理查询带有关键字高亮
     *
     * @param geoQueryCondition 查询条件
     * @param resolve 装饰函数
     * @return PageView<Map<String, Object>>
     */
    public PageView<Map<String, Object>> geoMatchQuery(GeoMatchQueryCondition geoQueryCondition, IResolveAdapterESDataRecord... resolve){
        if (!geoQueryCondition.checkQueryCondition()) {
            throw new IllegalArgumentException("查询参数有误，请检查查询参数");
        }
        PageView<Map<String, Object>> elasticResponse = new PageView<>();
        SearchRequest searchRequest = new SearchRequest();
        elasticResponse.setPerSize(geoQueryCondition.getPerSize());

        QueryDataUtils.resolveQueryCondition(searchRequest, QueryDataUtils.searchGeoMatchBuilder(geoQueryCondition), geoQueryCondition);
        try {
           responseDataUtils.wrapElasticResponse(searchRequest, elasticResponse, geoQueryCondition.getHighLightConfig(), resolve);
            return elasticResponse;
        } catch (Exception ex) {
            LOGGER.error("geoMatchQuery document is error :{}",JSON.toJSONString(geoQueryCondition), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * 搜索建议
     *
     * @param suggestCondition
     * @return
     */
    public Set<String> suggest(SuggestCondition suggestCondition){
        if (!suggestCondition.checkQueryCondition()) {
            throw new IllegalArgumentException("查询参数有误，请检查查询参数");
        }
        return responseDataUtils.parseSuggestResp(QueryDataUtils.toSuggestRequest(suggestCondition));
    }

    /**
     * 搜索建议
     *
     * @param suggestCondition
     * @return
     */
    public Set<String> suggest(SuggestAdvanceCondition suggestCondition){
        if (!suggestCondition.checkQueryCondition()) {
            throw new IllegalArgumentException("查询参数有误，请检查查询参数");
        }
        return responseDataUtils.parseSuggestResp(QueryDataUtils.toSuggestRequest(suggestCondition));
    }

    /**
     * 地图聚合查询接口
     *
     * @param mapAggregation：聚合查询条件
     * @return
     */
    public List<Map<String, Object>> mapAggregation(MapAggregation mapAggregation) {
        if (!mapAggregation.checkParam()){
            throw new IllegalArgumentException("查询参数有误，请检查查询参数");
        }
        Integer prc = QueryDataUtils.recallLevel((mapAggregation));
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (mapAggregation.getBottomRight() != null && mapAggregation.getTopLeft() != null) {
            ConstantScoreQueryBuilder constantScoreQueryBuilder = QueryBuilders.constantScoreQuery(QueryBuilders.geoBoundingBoxQuery(StringUtils.isEmpty(mapAggregation.getGeoPoint()) ? "location" : mapAggregation.getGeoPoint()).setCorners(
                    new GeoPoint(mapAggregation.getTopLeft().getLat(), mapAggregation.getTopLeft().getLon()),
                    new GeoPoint(mapAggregation.getBottomRight().getLat(), mapAggregation.getBottomRight().getLon())
            ));
            boolQueryBuilder = boolQueryBuilder.filter(constantScoreQueryBuilder);
        }
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryDataUtils.resolveQueryCondition(searchRequest, searchSourceBuilder, mapAggregation);
        if (mapAggregation.getIntersectQueryItems() != null && mapAggregation.getIntersectQueryItems().size() > 0) {
            for (QueryItem intersectQueryItem : mapAggregation.getIntersectQueryItems()) {
                boolQueryBuilder.filter(QueryBuilders.termsQuery(intersectQueryItem.getField(), intersectQueryItem.getValue()));
            }
        }
        searchSourceBuilder.size(0);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        GeoGridAggregationBuilder geoGridAggregationBuilder = AggregationBuilders.geohashGrid("geohash").precision(prc).field(StringUtils.isEmpty(mapAggregation.getGeoPoint()) ? "location" : mapAggregation.getGeoPoint()).size(1000);
        searchSourceBuilder.aggregation(geoGridAggregationBuilder);
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        return responseDataUtils.parseGeoGridAggregationResp(searchRequest, mapAggregation);
    }

    /**
     * 关闭连接
     * @throws Exception
     */
    public void closeClient() throws Exception {
        restHighLevelClient.close();
    }
}
