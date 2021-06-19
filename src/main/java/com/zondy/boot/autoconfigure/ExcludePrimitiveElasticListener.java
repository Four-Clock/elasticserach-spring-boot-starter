package com.zondy.boot.autoconfigure;

import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述: ExcludePrimitiveElasticListener
 * 排除SpringBoot自动注入的依赖
 * @author liqin(zxl)
 * @date 2021/6/18
 */
public class ExcludePrimitiveElasticListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String EXCLUDE_PRIMITIVE_ES_SOURCE_NAME = "excludePrimitiveElasticProperties";

    private static final String SPRING_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        Map<String, Object> source = new HashMap<>(4);
        source.put(SPRING_AUTOCONFIGURE_EXCLUDE,new String[]{ElasticsearchRestClientAutoConfiguration.class.getName()});
        environment.getPropertySources().addLast(new MapPropertySource(EXCLUDE_PRIMITIVE_ES_SOURCE_NAME,source));
    }
}
