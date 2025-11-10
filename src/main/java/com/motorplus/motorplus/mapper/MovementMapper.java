package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.dto.movementDtos.MovementType;
import com.motorplus.motorplus.model.Movement;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper
public interface MovementMapper {

    @Select("""
            <script>
            SELECT id, part_id AS partId, type, quantity, performed_at AS performedAt, notes
            FROM part_movements
            WHERE part_id = #{partId}
            <if test="type != null">
                AND type = #{type}
            </if>
            <if test="from != null">
                AND performed_at &gt;= #{from}
            </if>
            <if test="to != null">
                AND performed_at &lt;= #{to}
            </if>
            ORDER BY performed_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Movement> findByPart(@Param("partId") UUID partId,
                              @Param("type") MovementType type,
                              @Param("from") Instant from,
                              @Param("to") Instant to,
                              @Param("limit") int limit,
                              @Param("offset") long offset);

    @Insert("""
            INSERT INTO part_movements(id, part_id, type, quantity, performed_at, notes)
            VALUES(#{id}, #{partId}, #{type}, #{quantity}, #{performedAt}, #{notes})
            """)
    void insert(Movement movement);
}
