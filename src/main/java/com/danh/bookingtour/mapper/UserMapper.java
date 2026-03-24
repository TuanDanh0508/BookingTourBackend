package com.danh.bookingtour.mapper;

import com.danh.bookingtour.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    Optional<User> findByUsername(String username);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Insert("INSERT INTO users(username, password, email, role) VALUES(#{username}, #{password}, #{email}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);
}
