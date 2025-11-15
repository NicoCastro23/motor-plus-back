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


    // Reporte Simple 1: Actividad de Clientes
    @Select("""
            SELECT c.id AS clientId,
                   CONCAT(c.first_name, ' ', c.last_name) AS clientName,
                   c.email,
                   c.phone,
                   COUNT(DISTINCT v.id) AS vehicleCount,
                   MAX(o.created_at) AS lastOrderDate
            FROM clients c
                     LEFT JOIN vehicles v ON v.client_id = c.id
                     LEFT JOIN orders o ON o.client_id = c.id
            GROUP BY c.id, clientName, c.email, c.phone
            ORDER BY vehicleCount DESC, lastOrderDate DESC NULLS LAST
            """)
    List<Map<String, Object>> clientActivity();

    // Reporte Simple 2: Estado del Inventario
    @Select("""
            SELECT p.id AS partId,
                   p.name AS partName,
                   p.sku,
                   p.stock AS currentStock,
                   p.unit_price AS unitPrice,
                   (p.stock * p.unit_price) AS stockValue,
                   CASE 
                       WHEN p.stock = 0 THEN 'OUT_OF_STOCK'
                       WHEN p.stock < 10 THEN 'LOW_STOCK'
                       ELSE 'OK'
                   END AS status
            FROM parts p
            WHERE p.active = true
            ORDER BY status ASC, p.name ASC
            """)
    List<Map<String, Object>> partStockStatus();

    // Reporte Intermedio 1: Popularidad de Servicios
    @Select("""
            SELECT sc.id AS serviceId,
                   sc.name AS serviceName,
                   COUNT(oi.id) AS timesRequested,
                   SUM(oi.quantity * oi.unit_price) AS totalRevenue,
                   AVG(oi.unit_price) AS averagePrice
            FROM services_catalog sc
                     LEFT JOIN order_items oi ON oi.service_id = sc.id
                     LEFT JOIN orders o ON o.id = oi.order_id
            WHERE (#{from} IS NULL OR o.created_at >= #{from})
              AND (#{to} IS NULL OR o.created_at <= #{to})
            GROUP BY sc.id, sc.name
            HAVING COUNT(oi.id) > 0
            ORDER BY timesRequested DESC
            """)
    List<Map<String, Object>> servicePopularity(@Param("from") Instant from,
                                                @Param("to") Instant to);

    // Reporte Intermedio 2: Facturas Pendientes
    @Select("""
            SELECT i.id AS invoiceId,
                   i.number AS invoiceNumber,
                   c.id AS clientId,
                   CONCAT(c.first_name, ' ', c.last_name) AS clientName,
                   i.issue_date AS issueDate,
                   i.due_date AS dueDate,
                   i.total,
                   i.balance,
                   CASE 
                       WHEN i.due_date IS NULL THEN 0
                       WHEN i.due_date < CURRENT_TIMESTAMP THEN 
                           EXTRACT(DAY FROM (CURRENT_TIMESTAMP - i.due_date))::INTEGER
                       ELSE 0
                   END AS daysOverdue
            FROM invoices i
                     JOIN orders o ON o.id = i.order_id
                     JOIN clients c ON c.id = o.client_id
            WHERE i.status IN ('ISSUED', 'DRAFT')
              AND i.balance > 0
            ORDER BY daysOverdue DESC, i.due_date ASC
            """)
    List<Map<String, Object>> pendingInvoices();

    // Reporte Complejo 1: Rentabilidad por Cliente
    @Select("""
            SELECT c.id AS clientId,
                   CONCAT(c.first_name, ' ', c.last_name) AS clientName,
                   COUNT(DISTINCT o.id) AS orderCount,
                   COALESCE(SUM(o.total), 0) AS revenue,
                   COALESCE(SUM(parts_cost.cost), 0) AS partsCost,
                   COALESCE(SUM(labor_cost.cost), 0) AS laborCost,
                   COALESCE(SUM(o.total), 0) - COALESCE(SUM(parts_cost.cost), 0) - COALESCE(SUM(labor_cost.cost), 0) AS profit,
                   CASE 
                       WHEN SUM(o.total) > 0 THEN
                           ((SUM(o.total) - COALESCE(SUM(parts_cost.cost), 0) - COALESCE(SUM(labor_cost.cost), 0)) / SUM(o.total)) * 100
                       ELSE 0
                   END AS profitMargin
            FROM clients c
                     LEFT JOIN orders o ON o.client_id = c.id
                         AND (#{from} IS NULL OR o.created_at >= #{from})
                         AND (#{to} IS NULL OR o.created_at <= #{to})
                         AND o.status = 'COMPLETED'
                     LEFT JOIN LATERAL (
                         SELECT SUM(ip.quantity * ip.unit_price) AS cost
                         FROM order_items oi2
                                  JOIN order_item_parts ip ON ip.order_item_id = oi2.id
                         WHERE oi2.order_id = o.id
                     ) parts_cost ON true
                     LEFT JOIN LATERAL (
                         SELECT SUM(oi3.quantity * oi3.unit_price) AS cost
                         FROM order_items oi3
                         WHERE oi3.order_id = o.id
                     ) labor_cost ON true
            GROUP BY c.id, clientName
            HAVING COUNT(DISTINCT o.id) > 0
            ORDER BY profit DESC
            """)
    List<Map<String, Object>> clientProfitability(@Param("from") Instant from,
                                                  @Param("to") Instant to);

    // Reporte Complejo 2: Productividad de Mecánicos
    @Select("""
            SELECT m.id AS mechanicId,
                   CONCAT(m.first_name, ' ', m.last_name) AS mechanicName,
                   m.specialization,
                   COUNT(DISTINCT a.order_item_id) AS assignedOrders,
                   COUNT(DISTINCT CASE WHEN o.status = 'COMPLETED' THEN a.order_item_id END) AS completedOrders,
                   CASE 
                       WHEN COUNT(DISTINCT a.order_item_id) > 0 THEN
                           (COUNT(DISTINCT CASE WHEN o.status = 'COMPLETED' THEN a.order_item_id END)::DECIMAL / 
                            COUNT(DISTINCT a.order_item_id)::DECIMAL) * 100
                       ELSE 0
                   END AS completionRate,
                   COALESCE(SUM(a.estimated_hours), 0) AS totalHours,
                   CASE 
                       WHEN COUNT(DISTINCT a.order_item_id) > 0 THEN
                           COALESCE(SUM(a.estimated_hours), 0)::DECIMAL / COUNT(DISTINCT a.order_item_id)::DECIMAL
                       ELSE 0
                   END AS avgHoursPerOrder,
                   COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity * oi.unit_price ELSE 0 END), 0) AS revenueGenerated
            FROM mechanics m
                     LEFT JOIN assignments a ON a.mechanic_id = m.id
                     LEFT JOIN order_items oi ON oi.id = a.order_item_id
                     LEFT JOIN orders o ON o.id = oi.order_id
                         AND (#{from} IS NULL OR o.created_at >= #{from})
                         AND (#{to} IS NULL OR o.created_at <= #{to})
            WHERE m.active = true
            GROUP BY m.id, mechanicName, m.specialization
            ORDER BY completionRate DESC, revenueGenerated DESC
            """)
    List<Map<String, Object>> mechanicProductivity(@Param("from") Instant from,
                                                   @Param("to") Instant to);
}
