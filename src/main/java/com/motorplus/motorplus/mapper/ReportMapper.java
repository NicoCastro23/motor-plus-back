package com.motorplus.motorplus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface ReportMapper {

    @Select("""
            SELECT o.created_at AS eventDate,
                   CONCAT('Orden ', o.id) AS description,
                   o.license_plate AS reference
            FROM orders o
            WHERE o.license_plate = #{licensePlate}
            ORDER BY o.created_at DESC
            """)
    List<Map<String, Object>> vehicleHistory(@Param("licensePlate") String licensePlate);

    @Select("""
            SELECT m.id AS mechanicId,
                   CONCAT(m.first_name, ' ', m.last_name) AS mechanicName,
                   COUNT(DISTINCT a.order_item_id) AS ordersCompleted,
                   COALESCE(SUM(a.estimated_hours), 0) AS totalHours
            FROM mechanics m
                     LEFT JOIN assignments a ON a.mechanic_id = m.id
                     LEFT JOIN order_items oi ON oi.id = a.order_item_id
                     LEFT JOIN orders o ON o.id = oi.order_id AND o.status = 'COMPLETED'
            WHERE (#{mechanicId} IS NULL OR m.id = #{mechanicId})
              AND (#{from} IS NULL OR o.created_at >= #{from})
              AND (#{to} IS NULL OR o.created_at <= #{to})
            GROUP BY m.id, mechanicName
            ORDER BY mechanicName
            """)
    List<Map<String, Object>> mechanicPerformance(@Param("from") Instant from,
                                                  @Param("to") Instant to,
                                                  @Param("mechanicId") UUID mechanicId);

    @Select("""
            SELECT o.id AS orderId,
                   o.license_plate AS vehiclePlate,
                   ip.quantity AS quantityUsed,
                   o.created_at AS usedAt
            FROM order_item_parts ip
                     JOIN order_items oi ON oi.id = ip.order_item_id
                     JOIN orders o ON o.id = oi.order_id
            WHERE ip.part_id = #{partId}
              AND (#{from} IS NULL OR o.created_at >= #{from})
              AND (#{to} IS NULL OR o.created_at <= #{to})
            ORDER BY o.created_at DESC
            """)
    List<Map<String, Object>> partTraceability(@Param("partId") UUID partId,
                                               @Param("from") Instant from,
                                               @Param("to") Instant to);

    @Select("""
            SELECT o.id AS orderId,
                   CONCAT('ORD-', TO_CHAR(o.created_at, 'YYYYMMDD')) AS orderNumber,
                   o.total AS revenue,
                   COALESCE(SUM(ip.quantity * ip.unit_price), 0) AS partCost,
                   o.total - COALESCE(SUM(ip.quantity * ip.unit_price), 0) AS margin
            FROM orders o
                     LEFT JOIN order_items oi ON oi.order_id = o.id
                     LEFT JOIN order_item_parts ip ON ip.order_item_id = oi.id
            WHERE (#{from} IS NULL OR o.created_at >= #{from})
              AND (#{to} IS NULL OR o.created_at <= #{to})
              AND (#{clientId} IS NULL OR o.client_id = #{clientId})
              AND (#{licensePlate} IS NULL OR o.license_plate = #{licensePlate})
            GROUP BY o.id, orderNumber, o.total
            ORDER BY o.created_at DESC
            """)
    List<Map<String, Object>> orderMargins(@Param("from") Instant from,
                                           @Param("to") Instant to,
                                           @Param("clientId") UUID clientId,
                                           @Param("licensePlate") String licensePlate);
}
