package com.example.todo.dao.mapper;

import com.example.todo.dao.entity.TodoItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待办事项数据访问接口
 *
 * @author AiWork
 * @date 2026/10/14
 */
@Mapper
public interface TodoItemMapper {

    /**
     * 新增待办事项
     *
     * @param todoItemDO 待办事项数据对象，插入成功后自增主键回填至 id
     * @return 受影响行数
     */
    int saveTodoItem(TodoItemDO todoItemDO);
}
