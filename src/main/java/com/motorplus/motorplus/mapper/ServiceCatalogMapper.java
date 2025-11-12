package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.ServiceCatalog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ServiceCatalogMapper {

    @Select("""
            <script>
            SELECT id, name, description, price, active, created_at AS createdAt
            FROM services_catalog
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            <if test="active != null">
                AND active = #{active}
            </if>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<ServiceCatalog> findAll(@Param("q") String q,
                                 @Param("active") Boolean active,
                                 @Param("limit") int limit,
                                 @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM services_catalog
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            <if test="active != null">
                AND active = #{active}
            </if>
            </script>
            """)
    long count(@Param("q") String q, @Param("active") Boolean active);

    @Select("""
        SELECT id, name, description, price, active, created_at AS createdAt
        FROM services_catalog
        WHERE id = #{id,jdbcType=OTHER}   -- ⬅️ agrega jdbcType=OTHER
        """)
    ServiceCatalog findById(@Param("id") UUID id);

    @Insert("""
        INSERT INTO services_catalog(id, name, description, price, active, created_at)
        VALUES(#{id,jdbcType=OTHER}, #{name}, #{description}, #{price}, #{active}, #{createdAt})
        """)
    void insert(ServiceCatalog catalog);

    @Update("""
        UPDATE services_catalog
        SET name = #{name},
            description = #{description},
            price = #{price},
            active = #{active}
        WHERE id = #{id,jdbcType=OTHER}
        """)
    int update(ServiceCatalog catalog);

    @Delete("DELETE FROM services_catalog WHERE id = #{id,jdbcType=OTHER}")
    int delete(@Param("id") UUID id);

}
