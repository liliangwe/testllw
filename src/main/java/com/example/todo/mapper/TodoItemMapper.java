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
}