package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Part;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface PartMapper {

    @Select("""
            <script>
            SELECT id, name, sku, description, unit_price AS unitPrice, stock, active, created_at AS createdAt
            FROM parts
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(sku) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            <if test="active != null">
                AND active = #{active}
            </if>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Part> findAll(@Param("q") String q,
                       @Param("active") Boolean active,
                       @Param("limit") int limit,
                       @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM parts
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(sku) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            <if test="active != null">
                AND active = #{active}
            </if>
            </script>
            """)
    long count(@Param("q") String q, @Param("active") Boolean active);

    @Select("""
            SELECT id, name, sku, description, unit_price AS unitPrice, stock, active, created_at AS createdAt
            FROM parts
            WHERE id = #{id}
            """)
    Part findById(@Param("id") UUID id);

    @Insert("""
            INSERT INTO parts(id, name, sku, description, unit_price, stock, active, created_at)
            VALUES(#{id}, #{name}, #{sku}, #{description}, #{unitPrice}, #{stock}, #{active}, #{createdAt})
            """)
    void insert(Part part);

    @Update("""
            UPDATE parts
            SET name = #{name},
                sku = #{sku},
                description = #{description},
                unit_price = #{unitPrice},
                stock = #{stock},
                active = #{active}
            WHERE id = #{id}
            """)
    int update(Part part);

    @Delete("DELETE FROM parts WHERE id = #{id}")
    int delete(@Param("id") UUID id);

    @Update("""
            UPDATE parts
            SET stock = stock + #{delta}
            WHERE id = #{id}
            """)
    int updateStock(@Param("id") UUID id, @Param("delta") int delta);
}
