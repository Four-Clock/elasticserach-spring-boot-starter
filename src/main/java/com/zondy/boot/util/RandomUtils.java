package com.zondy.boot.util;

import java.util.UUID;

/**
 * 功能描述: RandomUtils
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
public class RandomUtils {

    public static String getUid(){
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}
