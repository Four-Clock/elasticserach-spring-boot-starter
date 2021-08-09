package com.zondy.boot.autoconfigure;

import com.zondy.boot.service.ElasticsearchService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 功能描述: ElasticsearchAutoConfiguration
 * Elasticsearch 自动配置类
 * @author liqin(zxl)
 * @date 2021/6/17
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ElasticsearchProperties.class)
@ConditionalOnProperty(prefix ="liqin.elasticsearch.starter",name = "enable",havingValue = "true",matchIfMissing = true)
public class ElasticsearchAutoConfiguration {

    final ElasticsearchProperties elasticsearchProperties;

    public ElasticsearchAutoConfiguration(ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchService elasticsearchService(){
        return new ElasticsearchService(elasticsearchProperties);
    }

}
