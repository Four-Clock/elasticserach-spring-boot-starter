package com.zondy.boot.model;

import lombok.Data;

/**
 * 功能描述: OrderItem
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Data
public class OrderItem {
    private String orderField;
    private OrderEnum order;
}
