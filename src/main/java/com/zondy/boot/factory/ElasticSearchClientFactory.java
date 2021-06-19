package com.zondy.boot.factory;

import com.zondy.boot.autoconfigure.ElasticsearchProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.util.StringUtils;


/**
 * 功能描述: ElasticSearchClientFactory
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
public class ElasticSearchClientFactory extends HighLevelClient{

    private ElasticsearchProperties elasticsearchProperties;

    public ElasticSearchClientFactory(ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    public RestHighLevelClient restHighLevelClient(){
        HttpHost[] httpHost = this.getHttpHost(elasticsearchProperties.getUris());
        if (StringUtils.isEmpty(elasticsearchProperties.getUsername()) && StringUtils.isEmpty(elasticsearchProperties.getPassword())){
            return new RestHighLevelClient(RestClient.builder(httpHost));
        }
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword()));
        return new RestHighLevelClient(
                RestClient.builder(httpHost)
                        .setHttpClientConfigCallback(httpClientBuilder -> {
                            httpClientBuilder.disableAuthCaching();
                            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                        }));

    }
}
