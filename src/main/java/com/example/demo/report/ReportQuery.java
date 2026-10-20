package com.example.demo.report;

/**
 * 报表聚合查询请求参数。
 */
public class ReportQuery {

    private String dimension;
    private String groupBy;
    private String start;
    private String end;
    private String apiName;

    public ReportQuery() {
    }

    public ReportQuery(String dimension, String groupBy, String start, String end, String apiName) {
        this.dimension = dimension;
        this.groupBy = groupBy;
        this.start = start;
        this.end = end;
        this.apiName = apiName;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
}