package com.zondy.boot.model;

import lombok.Data;

/**
 * 功能描述: FieldType
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
@Data
public class FieldType {

    private String field;
    private Field type;

    public enum Field {
        /**
         * 文本类型
         */
        TEXT(1, "text"),
        /**
         * IK分词
         */
        IK(2, "ik_smart"),
        /**
         * 关键字
         */
        KEYWORD(3, "keyword"),
        /**
         * 联想
         */
        COMPLETION(4, "completion"),
        /**
         * 坐标点geoPoint类型
         */
        GEO_POINT(5, "geo_point");

        private Integer index;

        private String typeName;

        Field(Integer index, String typeName) {
            this.index = index;
            this.typeName = typeName;
        }
        public Integer getIndex() {
            return index;
        }

        public String getTypeName() {
            return typeName;
        }

    }
}
