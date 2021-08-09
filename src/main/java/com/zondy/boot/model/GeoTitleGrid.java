package com.zondy.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 功能描述: GeoTitleGrid
 *
 * @author liqin(zxl)
 * @date 2021/8/8
 */
@Data
public class GeoTitleGrid {


    private GridSplit gridSplit;

    @Data
    public static class GridSplit{

        private Aggs aggs;
        private Geotile_grid geotile_grid;
    }

    @Data
    @AllArgsConstructor
    public static class Geotile_grid{
        private String field;
        private int precision;
    }

    @Data
    @AllArgsConstructor
    public static class Aggs{
        private GridCentroid gridCentroid;
    }

    @Data
    @AllArgsConstructor
    public static class GridCentroid{
        private Geo_centroid geo_centroid;
    }

    @Data
    @AllArgsConstructor
    public static class Geo_centroid{
        private String field;
    }
}
