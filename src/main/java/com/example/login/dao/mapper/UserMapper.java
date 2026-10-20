package com.example.login.dao.mapper;

import com.example.login.model.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问接口。
 *
 * @author AiWork
 * @date 2026/10/20
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户数据对象，未找到返回 null
     */
    UserDO selectByUsername(@Param("username") String username);
}