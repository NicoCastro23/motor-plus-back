package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Mechanic;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface MechanicMapper {

    @Select("""
            <script>
            SELECT id, first_name AS firstName, last_name AS lastName, specialization, phone, active, created_at AS createdAt
            FROM mechanics
            WHERE 1 = 1
            <if test="q != null and q != ''">
                AND (LOWER(first_name) LIKE LOWER(CONCAT('%', #{q}, '%')) OR LOWER(last_name) LIKE LOWER(CONCAT('%', #{q}, '%')))
            </if>
            <if test="specialization != null and specialization != ''">
                AND LOWER(specialization) LIKE LOWER(CONCAT('%', #{specialization}, '%'))
            </if>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Mechanic> findAll(@Param("q") String q,
                           @Param("specialization") String specialization,
                           @Param("limit") int limit,
                           @Param("offset") long offset);

    @Select("""
            SELECT id, first_name AS firstName, last_name AS lastName, specialization, phone, active, created_at AS createdAt
            FROM mechanics
            WHERE id = #{id}
            """)
    Mechanic findById(@Param("id") UUID id);

    @Insert("""
            INSERT INTO mechanics(id, first_name, last_name, specialization, phone, active, created_at)
            VALUES(#{id}, #{firstName}, #{lastName}, #{specialization}, #{phone}, #{active}, #{createdAt})
            """)
    void insert(Mechanic mechanic);

    @Update("""
            UPDATE mechanics
            SET first_name = #{firstName},
                last_name = #{lastName},
                specialization = #{specialization},
                phone = #{phone},
                active = #{active}
            WHERE id = #{id}
            """)
    int update(Mechanic mechanic);

    @Delete("DELETE FROM mechanics WHERE id = #{id}")
    int delete(@Param("id") UUID id);
}
