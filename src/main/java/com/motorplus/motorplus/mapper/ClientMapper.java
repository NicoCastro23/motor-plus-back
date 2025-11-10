package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Client;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ClientMapper {

    @Select("""
            <script>
            SELECT id, first_name AS firstName, last_name AS lastName, email, phone, created_at AS createdAt
            FROM clients
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(first_name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(last_name) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            <if test="email != null and email != ''">
                AND email = #{email}
            </if>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    @Results(id = "clientResult", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "firstName", property = "firstName"),
            @Result(column = "lastName", property = "lastName"),
            @Result(column = "email", property = "email"),
            @Result(column = "phone", property = "phone"),
            @Result(column = "createdAt", property = "createdAt")
    })
    List<Client> findAll(@Param("q") String q,
                         @Param("email") String email,
                         @Param("limit") int limit,
                         @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM clients
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(first_name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(last_name) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            <if test="email != null and email != ''">
                AND email = #{email}
            </if>
            </script>
            """)
    long count(@Param("q") String q, @Param("email") String email);

    @Select("""
            SELECT id, first_name AS firstName, last_name AS lastName, email, phone, created_at AS createdAt
            FROM clients
            WHERE id = #{id}
            """)
    @ResultMap("clientResult")
    Client findById(@Param("id") UUID id);

    @Insert("""
            INSERT INTO clients(id, first_name, last_name, email, phone, created_at)
            VALUES(#{id}, #{firstName}, #{lastName}, #{email}, #{phone}, #{createdAt})
            """)
    void insert(Client client);

    @Update("""
            UPDATE clients
            SET first_name = #{firstName},
                last_name = #{lastName},
                email = #{email},
                phone = #{phone}
            WHERE id = #{id}
            """)
    int update(Client client);

    @Delete("DELETE FROM clients WHERE id = #{id}")
    int delete(@Param("id") UUID id);

}
