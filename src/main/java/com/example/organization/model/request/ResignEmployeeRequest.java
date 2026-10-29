package com.example.organization.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 员工离职请求。
 */
public class ResignEmployeeRequest {

    @NotBlank
    private String resignDate;

    public String getResignDate() {
        return resignDate;
    }

    public void setResignDate(String resignDate) {
        this.resignDate = resignDate;
    }
}
