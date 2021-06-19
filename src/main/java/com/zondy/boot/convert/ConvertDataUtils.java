package com.zondy.boot.convert;

import com.zondy.boot.constant.ESettings;
import com.zondy.boot.model.CommonCondition;
import com.zondy.boot.model.FieldType;
import com.zondy.boot.util.RandomUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: ConvertDataUtils
 * 数据转换类
 * @author liqin(zxl)
 * @date 2021/6/18
 */
public class ConvertDataUtils {

    /**
     * 构建
     * @return CreateIndexRequest
     */
    public static CreateIndexRequest createIndexRequest(String index, Integer shards, Integer replicas, List<FieldType> fields){
        CreateIndexRequest request =createIndexRequest(index,shards,replicas);
        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        fields.forEach(field -> {
            Map<String, Object> message = new HashMap<>(16);
            String fieldName = field.getField();
            FieldType.Field type = field.getType();
            switch (type){
                case IK:
                    message.put("type", "text");
                    message.put("analyzer", "ik_max_word");
                    message.put("search_analyzer", "ik_smart");
                    break;
                case COMPLETION:
                    message.put("type", type.getTypeName());
                    message.put("analyzer", "ik_max_word");
                    message.put("search_analyzer", "ik_smart");
                    break;
                default:
                    message.put("type", type.getTypeName());
                    properties.put(fieldName, message);
            }
            properties.put(fieldName, message);
        });
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        jsonMap.put("_doc", mapping);
        request.mapping("_doc", jsonMap);
        return request;
    }

   public static CreateIndexRequest createIndexRequest(String index, Integer shards, Integer replicas){
       CreateIndexRequest request = new CreateIndexRequest(index);
       request.settings(Settings.builder()
               .put(ESettings.INDEX_NUMBER_SHARDS, shards == null ? 3 : shards)
               .put(ESettings.INDEX_NUMBER_REPLICAS, replicas == null ? 1 : replicas)
       );
       return request;
   }

   public static IndexRequest indexRequest(String index, Map<String, Object> dataMap){
       String id = !dataMap.containsKey("id") ? RandomUtils.getUid() : dataMap.get("id").toString();
       dataMap.put("id", id);
       IndexRequest indexRequest = new IndexRequest(index, "_doc", id)
               .source(dataMap);
       indexRequest.timeout(TimeValue.timeValueSeconds(60));
       indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
       return indexRequest;
   }

   public static BulkRequest bulkRequest(String index, List<Map<String, Object>> dataList){
       BulkRequest bulkRequest = new BulkRequest();
       bulkRequest.timeout(TimeValue.timeValueSeconds(60));
       bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
       dataList.forEach(t -> {
           String id = !t.containsKey("id") ? RandomUtils.getUid() : t.get("id").toString();
           t.put("id", id);
           IndexRequest indexRequest = new IndexRequest(index, "_doc", id)
                   .source(t);
           bulkRequest.add(indexRequest);
       });
       return bulkRequest;
   }

   public static UpdateRequest updateRequest(String index, String id, Map<String, Object> dataMap){
       UpdateRequest request = new UpdateRequest(index, "_doc", id).doc(dataMap);
       request.timeout(TimeValue.timeValueSeconds(60));
       request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
       return request;
   }

   public static BulkRequest bulkBatchRequest(String index, List<Map<String, Map<String, Object>>> dataList){
       BulkRequest request = new BulkRequest();
       for (Map<String, Map<String, Object>> updataMap : dataList) {
           updataMap.forEach((k, v) -> {
               UpdateRequest updateRequest = new UpdateRequest(index, "_doc", k).doc(v);
               request.add(updateRequest);
           });
       }
       request.timeout(TimeValue.timeValueSeconds(60));
       request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
       return request;
   }

   public static DeleteRequest deleteRequest(String index, String docId){
       DeleteRequest request = new DeleteRequest(index, "_doc", docId);
       request.timeout(TimeValue.timeValueSeconds(60));
       request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
       return request;
   }

   public static DeleteByQueryRequest deleteByQueryRequest(CommonCondition commonCondition){
       DeleteByQueryRequest request =
               new DeleteByQueryRequest(commonCondition.getIndex());
       BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
       QueryDataUtils.resolveCommonCondition(commonCondition, boolQueryBuilder);
       request.setQuery(boolQueryBuilder);
       request.setConflicts("proceed");
       request.setBatchSize(5000);
       request.setSize(10000000);
       request.setSlices(2);
       request.setScroll(TimeValue.timeValueMinutes(10));
       request.setTimeout(TimeValue.timeValueMinutes(5));
       request.setRefresh(true);
       return request;
   }

   public static BulkRequest bulkDeleteRequest(String index,List<String> docIds){
       BulkRequest request = new BulkRequest();
       for (String docId : docIds) {
           request.add(new DeleteRequest(index, "_doc", docId));
       }
       request.timeout(TimeValue.timeValueSeconds(60));
       request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
       return request;

   }
}
