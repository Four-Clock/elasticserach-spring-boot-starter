package com.zondy.boot.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述: PageView
 *
 * @author liqin(zxl)
 * @date 2021/6/18
 */
public class PageView<T> {

    /**
     * 记录总条数
     **/
    private Long total;

    /**
     * 数据实体
     */
    private List<T> records = new ArrayList<>();

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 每页显示条数
     */
    private Integer perSize = 20;

    /**
     * 预留字段，用于绑定自定义扩展数据
     */
    private Object bindTag;

    public Integer getTotalPage() {
        if (perSize == 0) {
            return 0;
        }
        return total % perSize == 0 ? (int) (total / perSize) : (int) (total / perSize + 1);
    }

    public PageView(int perSize, Long total) {
        this.perSize = perSize;
        this.total = total;
    }

    public PageView() {
    }

    public Long getTotal() {
        return total;
    }

    public List<T> getRecords() {
        return records;
    }

    public Integer getPerSize() {
        return perSize;
    }

    public Object getBindTag() {
        return bindTag;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public void setPerSize(Integer perSize) {
        this.perSize = perSize;
    }

    public void setBindTag(Object bindTag) {
        this.bindTag = bindTag;
    }
}
