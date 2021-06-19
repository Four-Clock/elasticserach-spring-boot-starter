package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述: AggFiledItem
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggFiledItem {
    private String field;
    private OrderItem orderItem;
}
