package com.antdigital.todo.service;

import com.antdigital.todo.api.request.TodoTaskCreateRequest;
import com.antdigital.todo.api.response.TodoTaskCreateResponse;
import com.antdigital.todo.common.exception.TodoBizException;

/**
 * 待办事项服务（系分 4.3 内部接口 S01）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public interface TodoTaskService {

    /**
     * 新增待办事项：创建人/租户取登录态，落库后返回结果视图。
     *
     * @param request 新增请求，事项名称必填、事项描述选填
     * @return 新建待办事项结果视图，含 taskId/taskName/gmtCreate
     * @throws TodoBizException 参数非法（TODO_001）、登录态缺失（TODO_002）、
     *                          租户缺失（TODO_003）、保存失败（TODO_004）、创建已关闭（TODO_005）
     */
    TodoTaskCreateResponse createTodoTask(TodoTaskCreateRequest request);
}
