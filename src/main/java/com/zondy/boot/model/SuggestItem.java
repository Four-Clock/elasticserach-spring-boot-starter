package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述: SuggestItem
 *
 * @author liqin(zxl)
 * @date 2021/7/9
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SuggestItem {
    private String field;
    private String value;
}
