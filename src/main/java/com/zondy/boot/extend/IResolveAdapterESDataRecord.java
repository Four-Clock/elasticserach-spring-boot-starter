package com.zondy.boot.extend;

import java.util.Map;

/**
 * 功能描述: IResolveAdapterESDataRecord
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@FunctionalInterface
public interface IResolveAdapterESDataRecord {

    /**
     * 装饰ES查询出来的数据
     * @param record
     */
    void resolve(Map<String, Object> record);
}
