package com.example.demo.tracking;

import java.time.LocalDateTime;

/**
 * demo_api_tracking 表的记录实体（与 schema.sql 字段一一对应）。
 */
public class TrackingRecord {

    private final String apiName;
    private final String callerId;
    private final String personType;
    private final String personLevel;
    private final String department;
    private final LocalDateTime callTime;
    private final LocalDateTime gmtCreate;
    private final LocalDateTime gmtModified;

    public TrackingRecord(String apiName, String callerId, String personType, String personLevel,
                          String department, LocalDateTime callTime, LocalDateTime gmtCreate,
                          LocalDateTime gmtModified) {
        this.apiName = apiName;
        this.callerId = callerId;
        this.personType = personType;
        this.personLevel = personLevel;
        this.department = department;
        this.callTime = callTime;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
    }

    public String getApiName() {
        return apiName;
    }

    public String getCallerId() {
        return callerId;
    }

    public String getPersonType() {
        return personType;
    }

    public String getPersonLevel() {
        return personLevel;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDateTime getCallTime() {
        return callTime;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }
}