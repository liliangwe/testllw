package com.example.organization.model.dto;

/**
 * 唯一性校验响应。
 */
public class UniqueCheckResponse {

    private boolean exist;

    public UniqueCheckResponse() {
    }

    public UniqueCheckResponse(boolean exist) {
        this.exist = exist;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }
}
