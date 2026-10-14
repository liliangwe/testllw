package com.example.todo.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增待办事项请求对象
 *
 * @author AiWork
 * @date 2026/10/14
 */
public class CreateTodoRequest {

    /** 字段名：事项名称，用于校验失败时映射错误码 */
    public static final String FIELD_NAME = "name";

    /** 字段名：事项描述，用于校验失败时映射错误码 */
    public static final String FIELD_DESCRIPTION = "description";

    /** 事项名称最大长度 */
    private static final int MAX_NAME_LENGTH = 200;

    /** 事项描述最大长度 */
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    /** 事项名称，必填，最长 200 字符 */
    @NotBlank(message = "事项名称不能为空")
    @Size(max = MAX_NAME_LENGTH, message = "事项名称长度不能超过200字符")
    private String name;

    /** 事项描述，选填，最长 2000 字符 */
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "事项描述长度不能超过2000字符")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CreateTodoRequest{name='" + name + "', description='" + description + "'}";
    }
}
