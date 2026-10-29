package com.example.organization.common;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果。
 */
public class PageResult<T> {

    private long total;
    private List<T> list;
    private int pageNum;
    private int pageSize;

    public PageResult() {
    }

    public PageResult(long total, List<T> list, int pageNum, int pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L, Collections.emptyList(), 1, 0);
    }

    public static <T> PageResult<T> of(long total, List<T> list, int pageNum, int pageSize) {
        return new PageResult<>(total, list, pageNum, pageSize);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
