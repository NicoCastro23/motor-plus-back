package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;
import java.util.UUID;

@Mapper
public interface AdminMapper {

    @Select("""
            SELECT id, username, password, email, active, created_at, verification_token
            FROM admins
            WHERE username = #{username}
            AND active = true
            """)
    @Results(id = "adminResults", value = {
        @Result(property = "id", column = "id", javaType = UUID.class, jdbcType = JdbcType.OTHER),
        @Result(property = "username", column = "username"),
        @Result(property = "password", column = "password"),
        @Result(property = "email", column = "email"),
        @Result(property = "active", column = "active"),
        @Result(property = "createdAt", column = "created_at", javaType = Instant.class, jdbcType = JdbcType.TIMESTAMP),
        @Result(property = "verificationToken", column = "verification_token")
    })
    Admin findByUsername(@Param("username") String username);

    @Select("""
            SELECT id, username, password, email, active, created_at, verification_token
            FROM admins
            WHERE username = #{username}
            """)
    @ResultMap("adminResults")
    Admin findByUsernameAny(@Param("username") String username);

    @Select("""
            SELECT id, username, password, email, active, created_at, verification_token
            FROM admins
            WHERE email = #{email}
            """)
    @ResultMap("adminResults")
    Admin findByEmail(@Param("email") String email);

    @Insert("""
            INSERT INTO admins(id, username, password, email, active, created_at, verification_token)
            VALUES(#{id}::uuid, #{username}, #{password}, #{email}, #{active}, #{createdAt}, #{verificationToken})
            """)
    void insert(Admin admin);

    @Update("""
            UPDATE admins
            SET active = true, verification_token = NULL
            WHERE id = #{id}::uuid
            """)
    void activateAdmin(@Param("id") UUID id);

    @Update("""
            UPDATE admins
            SET password = #{newPassword}
            WHERE username = #{username}
            AND active = true
            """)
    int updatePassword(@Param("username") String username, @Param("newPassword") String newPassword);
}

