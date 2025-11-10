package com.motorplus.motorplus.mapper;

import com.motorplus.motorplus.model.Assignment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface AssignmentMapper {

    @Select("""
            SELECT order_item_id AS orderItemId, mechanic_id AS mechanicId, assigned_at AS assignedAt, estimated_hours AS estimatedHours
            FROM assignments
            WHERE order_item_id = #{orderItemId}
            ORDER BY assigned_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Assignment> findByOrderItem(@Param("orderItemId") UUID orderItemId,
                                     @Param("limit") int limit,
                                     @Param("offset") long offset);

    @Select("""
            SELECT COUNT(*)
            FROM assignments
            WHERE order_item_id = #{orderItemId}
            """)
    long countByOrderItem(@Param("orderItemId") UUID orderItemId);

    @Select("""
            SELECT order_item_id AS orderItemId, mechanic_id AS mechanicId, assigned_at AS assignedAt, estimated_hours AS estimatedHours
            FROM assignments
            WHERE order_item_id = #{orderItemId} AND mechanic_id = #{mechanicId}
            """)
    Assignment find(@Param("orderItemId") UUID orderItemId, @Param("mechanicId") UUID mechanicId);

    @Insert("""
            INSERT INTO assignments(order_item_id, mechanic_id, assigned_at, estimated_hours)
            VALUES(#{orderItemId}, #{mechanicId}, #{assignedAt}, #{estimatedHours})
            """)
    void insert(Assignment assignment);

    @Update("""
            UPDATE assignments
            SET estimated_hours = #{estimatedHours}
            WHERE order_item_id = #{orderItemId} AND mechanic_id = #{mechanicId}
            """)
    int update(Assignment assignment);

    @Delete("DELETE FROM assignments WHERE order_item_id = #{orderItemId} AND mechanic_id = #{mechanicId}")
    int delete(@Param("orderItemId") UUID orderItemId, @Param("mechanicId") UUID mechanicId);
}
