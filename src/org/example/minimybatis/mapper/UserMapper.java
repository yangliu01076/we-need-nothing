package org.example.minimybatis.mapper;

import org.example.minimybatis.dto.User;

/**
 * @author duoyian
 * @date 2026/2/27
 */
public interface UserMapper {

    User selectById(Integer id);
}
