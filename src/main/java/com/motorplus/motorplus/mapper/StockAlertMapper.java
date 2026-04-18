package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.StockAlert;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper
public interface StockAlertMapper {

    @Select("""
            <script>
            SELECT sa.id, sa.part_id AS partId, sa.current_stock AS currentStock,
                   sa.min_stock AS minStock, sa.created_at AS createdAt,
                   sa.resolved, sa.resolved_at AS resolvedAt
            FROM stock_alerts sa
            WHERE 1 = 1
            <if test="resolved != null">
                AND sa.resolved = #{resolved}
            </if>
            ORDER BY sa.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<StockAlert> findAll(@Param("resolved") Boolean resolved,
                             @Param("limit") int limit,
                             @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM stock_alerts
            WHERE 1 = 1
            <if test="resolved != null">
                AND resolved = #{resolved}
            </if>
            </script>
            """)
    long count(@Param("resolved") Boolean resolved);

    @Select("""
            SELECT id, part_id AS partId, current_stock AS currentStock,
                   min_stock AS minStock, created_at AS createdAt,
                   resolved, resolved_at AS resolvedAt
            FROM stock_alerts
            WHERE id = #{id}
            """)
    StockAlert findById(@Param("id") UUID id);

    @Select("""
            SELECT EXISTS(
                SELECT 1 FROM stock_alerts
                WHERE part_id = #{partId} AND resolved = false
            )
            """)
    boolean existsActive(@Param("partId") UUID partId);

    @Insert("""
            INSERT INTO stock_alerts(id, part_id, current_stock, min_stock, created_at, resolved)
            VALUES(#{id}, #{partId}, #{currentStock}, #{minStock}, #{createdAt}, #{resolved})
            """)
    void insert(StockAlert alert);

    @Update("""
            UPDATE stock_alerts
            SET resolved = true, resolved_at = #{resolvedAt}
            WHERE id = #{id}
            """)
    int resolve(@Param("id") UUID id, @Param("resolvedAt") Instant resolvedAt);

    @Update("""
            UPDATE stock_alerts
            SET resolved = true, resolved_at = #{resolvedAt}
            WHERE part_id = #{partId} AND resolved = false
            """)
    int resolveByPart(@Param("partId") UUID partId, @Param("resolvedAt") Instant resolvedAt);
}
