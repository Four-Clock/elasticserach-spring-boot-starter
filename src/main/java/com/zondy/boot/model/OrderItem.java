package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述: OrderItem
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private String orderField;
    private OrderEnum order;
}
