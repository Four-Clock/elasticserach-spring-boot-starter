package com.zondy.boot.factory;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 功能描述: HighLevelClient
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
abstract class HighLevelClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HighLevelClient.class);

    protected HttpHost[] getHttpHost(List<String> uris){
        if (StringUtils.isEmpty(uris)) {
            throw new RuntimeException("配置有问题，elasticsearch.urls为空");
        }
        LOGGER.info("【URLS:{}】",uris);
        HttpHost[] httpHosts = new HttpHost[uris.size()];
        for (int i = 0; i <uris.size() ; i++) {
            if(StringUtils.isEmpty(uris.get(i))) {
                continue;
            }
            httpHosts[i] = HttpHost.create(uris.get(i));
        }
        return httpHosts;
    }
}
