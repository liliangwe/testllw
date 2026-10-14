package com.antdigital.todo.dao.mapper;

import com.antdigital.todo.model.entity.TodoItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待办事项数据访问接口。
 */
@Mapper
public interface TodoItemMapper {

    /**
     * 插入待办事项记录。
     *
     * @param todoItem 待办事项实体
     * @return 影响行数 (1 表示成功)
     */
    int insert(TodoItemDO todoItem);
}