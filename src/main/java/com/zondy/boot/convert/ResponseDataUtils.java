package com.zondy.boot.convert;

import com.alibaba.fastjson.JSON;
import com.zondy.boot.bean.PageView;
import com.zondy.boot.extend.IResolveAdapterESDataRecord;
import com.zondy.boot.model.HighLightConfig;
import com.zondy.boot.model.MapAggregation;
import com.zondy.boot.model.Point;
import com.zondy.boot.util.GeoHashUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 功能描述: ResponseDataUtils
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Slf4j
public class ResponseDataUtils {

    private static final String SOURCE_POST="_source";

    private RestHighLevelClient restHighLevelClient;

    public ResponseDataUtils(RestHighLevelClient restHighLevelClient){
        this.restHighLevelClient = restHighLevelClient;
    }

    public void wrapElasticResponse(SearchRequest searchRequest, PageView<Map<String, Object>> elasticResponse, HighLightConfig highLightConfig, IResolveAdapterESDataRecord... resolve) throws IOException {
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            SearchHits hits = searchResponse.getHits();
            elasticResponse.setTotal(hits.getTotalHits().value);
            hits.forEach(t -> {
                Map<String, Object> m = t.getSourceAsMap();
                if (highLightConfig!=null
                    &&!StringUtils.isEmpty(highLightConfig.getPostTags())
                    &&!StringUtils.isEmpty(highLightConfig.getPreTags())) {
                    refreshHit(m, t.getHighlightFields(),highLightConfig.isWithSourceText());
                }
                if (resolve != null && resolve.length > 0) {
                    resolve[0].resolve(m);
                }
                elasticResponse.getRecords().add(m);
            });
            Aggregations aggregations = searchResponse.getAggregations();
            if (aggregations == null) {
                return;
            }
            Terms agg = aggregations.get("_aggs");
            if (agg != null) {
                List<Map<String, Object>> bulks = new ArrayList<>(agg.getBuckets().size());
                agg.getBuckets().forEach(bk -> {
                    Map<String, Object> bulk = new HashMap<>();
                    bulk.put("lx", bk.getKey());
                    bulk.put("count", bk.getDocCount());
                    bulks.add(bulk);
                });
                elasticResponse.setBindTag(bulks);
            }
        }
    }

    /**
     *  组装返回结果
     * @param searchRequest 查询条件
     * @return Set<String>
     */
    public Set<String> parseSuggestResp(SearchRequest searchRequest){
        Set<String> result = new HashSet<>();
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion =
                    response.getSuggest().getSuggestion("suggest");
            List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> entries = suggestion.getEntries();
            for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : entries) {
                entry.getOptions().forEach(o -> {
                    result.add(o.getText().string());
                });
            }
        } catch (IOException ex) {
            log.error("query document is error :{}" , JSON.toJSONString(searchRequest), ex);
            throw new RuntimeException(ex);
        }
        return result;
    }

    /**
     * 结果集组装
     * @param searchRequest 查询条件
     * @param mapAggregation 聚合参数
     * @return List<Map<String, Object>>
     */
    public List<Map<String, Object>> parseGeoGridAggregationResp(SearchRequest searchRequest, MapAggregation mapAggregation){
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            GeoGrid geoGrid = response.getAggregations().get("geohash");
            if (geoGrid == null){
                return result;
            }
            for (GeoGrid.Bucket bucket : geoGrid.getBuckets()) {
                Map<String, Object> map = new HashMap<>(4);
                String geoHash = bucket.getKeyAsString();
                long docCount = bucket.getDocCount();
                Point center = GeoHashUtils.getGeoHashCenterByGeoHashStr(geoHash);
                map.put("center", center);
                map.put("count", docCount);
                result.add(map);
            }
        } catch (Exception ex) {
            log.error("query document is error :{}",JSON.toJSONString(mapAggregation), ex);
        }
        return result;
    }


    private void refreshHit(Map<String, Object> source, Map<String, HighlightField> hight,boolean withSourceText) {
        if (hight == null) {
            return;
        }
        hight.forEach((k, v) -> {
            Object s = source.get(k);
            source.put(k, v.fragments()[0].string());
            if (withSourceText){
                source.put(k+SOURCE_POST, s);
            }
        });
    }

}
