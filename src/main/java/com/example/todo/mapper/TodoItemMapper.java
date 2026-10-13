package com.example.todo.mapper;

import com.example.todo.entity.TodoItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待办事项 Mapper 接口
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Mapper
public interface TodoItemMapper {

    /**
     * 插入待办事项
     *
     * @param todoItem 待办事项实体
     * @return 影响行数
     */
    int insert(TodoItemDO todoItem);

    /**
     * 根据请求幂等键查询
     *
     * @param requestId 请求幂等键
     * @return 待办事项实体，未找到返回 null
     */
    TodoItemDO findByRequestId(String requestId);
}