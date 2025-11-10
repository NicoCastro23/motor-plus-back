package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Supervision;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SupervisionMapper {

    @Select("""
            <script>
            SELECT supervisor_id AS supervisorId, supervisado_id AS supervisadoId, order_id AS orderId, created_at AS createdAt, notes
            FROM supervisions
            WHERE 1 = 1
            <if test="supervisorId != null">
                AND supervisor_id = #{supervisorId}
            </if>
            <if test="supervisadoId != null">
                AND supervisado_id = #{supervisadoId}
            </if>
            <if test="orderId != null">
                AND order_id = #{orderId}
            </if>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Supervision> findAll(@Param("supervisorId") UUID supervisorId,
                              @Param("supervisadoId") UUID supervisadoId,
                              @Param("orderId") UUID orderId,
                              @Param("limit") int limit,
                              @Param("offset") long offset);

    @Select("""
            SELECT supervisor_id AS supervisorId, supervisado_id AS supervisadoId, order_id AS orderId, created_at AS createdAt, notes
            FROM supervisions
            WHERE supervisor_id = #{supervisorId} AND supervisado_id = #{supervisadoId} AND order_id = #{orderId}
            """)
    Supervision find(@Param("supervisorId") UUID supervisorId,
                     @Param("supervisadoId") UUID supervisadoId,
                     @Param("orderId") UUID orderId);

    @Insert("""
            INSERT INTO supervisions(supervisor_id, supervisado_id, order_id, created_at, notes)
            VALUES(#{supervisorId}, #{supervisadoId}, #{orderId}, #{createdAt}, #{notes})
            """)
    void insert(Supervision supervision);

    @Delete("DELETE FROM supervisions WHERE supervisor_id = #{supervisorId} AND supervisado_id = #{supervisadoId} AND order_id = #{orderId}")
    int delete(@Param("supervisorId") UUID supervisorId,
               @Param("supervisadoId") UUID supervisadoId,
               @Param("orderId") UUID orderId);
}
