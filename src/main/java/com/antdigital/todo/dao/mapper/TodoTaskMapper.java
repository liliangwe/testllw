package com.antdigital.todo.dao.mapper;

import com.antdigital.todo.dao.entity.TodoTaskDO;

/**
 * 待办事项数据访问接口。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public interface TodoTaskMapper {

    /**
     * 新增一条待办事项（单表单条 INSERT）。
     *
     * @param todoTask 待办事项数据对象，主键由数据库自增生成
     * @return 新建待办事项 ID，未取到自增主键时返回 null
     */
    Long insert(TodoTaskDO todoTask);
}
