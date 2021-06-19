package com.zondy.boot.model;

/**
 * 功能描述: IHighlightEnabled
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@FunctionalInterface
public interface IHighlightEnabled {
    /**
     * 获取高亮参数配置
     * @return HighLightConfig
     */
    HighLightConfig getHighLightConfig();
}
