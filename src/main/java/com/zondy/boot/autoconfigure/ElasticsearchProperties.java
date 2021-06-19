package com.zondy.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 功能描述: ElasticsearchProperties
 * Elasticsearch 配置属性类
 * @author liqin(zxl)
 * @date 2021/6/17
 */
@ConfigurationProperties(prefix = "liqin.elasticsearch")
public class ElasticsearchProperties {

    private List<String> uris = new ArrayList<>(Collections.singletonList("http://localhost:9200"));
    private String username;
    private String password;

    public List<String> getUris() {
        return uris;
    }

    public void setUris(List<String> uris) {
        this.uris = uris;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
