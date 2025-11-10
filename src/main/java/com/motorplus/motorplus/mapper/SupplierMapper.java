package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Supplier;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SupplierMapper {

    @Select("""
            <script>
            SELECT id, name, email, phone, active, created_at AS createdAt
            FROM suppliers
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(email) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Supplier> findAll(@Param("q") String q,
                           @Param("limit") int limit,
                           @Param("offset") long offset);

    @Select("""
            SELECT COUNT(*)
            FROM suppliers
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(email) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            </script>
            """)
    long count(@Param("q") String q);

    @Select("""
            SELECT id, name, email, phone, active, created_at AS createdAt
            FROM suppliers
            WHERE id = #{id}
            """)
    Supplier findById(@Param("id") UUID id);

    @Insert("""
            INSERT INTO suppliers(id, name, email, phone, active, created_at)
            VALUES(#{id}, #{name}, #{email}, #{phone}, #{active}, #{createdAt})
            """)
    void insert(Supplier supplier);

    @Update("""
            UPDATE suppliers
            SET name = #{name},
                email = #{email},
                phone = #{phone},
                active = #{active}
            WHERE id = #{id}
            """)
    int update(Supplier supplier);

    @Delete("DELETE FROM suppliers WHERE id = #{id}")
    int delete(@Param("id") UUID id);
}
