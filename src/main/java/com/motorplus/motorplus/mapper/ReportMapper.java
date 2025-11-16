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
                   COUNT(DISTINCT CASE WHEN o.status = 'COMPLETED' THEN a.order_item_id END) AS ordersCompleted,
                   COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN a.estimated_hours ELSE 0 END), 0) AS totalHours
            FROM mechanics m
                     LEFT JOIN assignments a ON a.mechanic_id = m.id
                     LEFT JOIN order_items oi ON oi.id = a.order_item_id
                     LEFT JOIN orders o ON o.id = oi.order_id
                         AND (#{from,jdbcType=TIMESTAMP} IS NULL OR o.created_at >= #{from,jdbcType=TIMESTAMP})
                         AND (#{to,jdbcType=TIMESTAMP} IS NULL OR o.created_at <= #{to,jdbcType=TIMESTAMP})
            WHERE (#{mechanicId,jdbcType=OTHER} IS NULL OR m.id = #{mechanicId,jdbcType=OTHER})
              AND m.active = true
            GROUP BY m.id, m.first_name, m.last_name
            ORDER BY mechanicName
            """)
    List<Map<String, Object>> mechanicPerformance(@Param("from") Instant from,
                                                  @Param("to") Instant to,
                                                  @Param("mechanicId") UUID mechanicId);

    @Select("""
            SELECT o.id AS orderId,
                   CONCAT('ORD-', TO_CHAR(o.created_at::DATE, 'YYYYMMDD')) AS orderNumber,
                   o.license_plate AS vehiclePlate,
                   ip.quantity AS quantityUsed,
                   o.created_at AS usedAt
            FROM order_item_parts ip
                     JOIN order_items oi ON oi.id = ip.order_item_id
                     JOIN orders o ON o.id = oi.order_id
            WHERE ip.part_id = #{partId,jdbcType=OTHER}
              AND (#{from,jdbcType=TIMESTAMP} IS NULL OR o.created_at >= #{from,jdbcType=TIMESTAMP})
              AND (#{to,jdbcType=TIMESTAMP} IS NULL OR o.created_at <= #{to,jdbcType=TIMESTAMP})
            ORDER BY o.created_at DESC
            """)
    List<Map<String, Object>> partTraceability(@Param("partId") UUID partId,
                                               @Param("from") Instant from,
                                               @Param("to") Instant to);

    @Select("""
            SELECT o.id AS orderId,
                   CONCAT('ORD-', TO_CHAR(o.created_at::DATE, 'YYYYMMDD')) AS orderNumber,
                   o.total AS revenue,
                   COALESCE(SUM(ip.quantity * ip.unit_price), 0) AS partCost,
                   o.total - COALESCE(SUM(ip.quantity * ip.unit_price), 0) AS margin
            FROM orders o
                     LEFT JOIN order_items oi ON oi.order_id = o.id
                     LEFT JOIN order_item_parts ip ON ip.order_item_id = oi.id
            WHERE (#{from,jdbcType=TIMESTAMP} IS NULL OR o.created_at >= #{from,jdbcType=TIMESTAMP})
              AND (#{to,jdbcType=TIMESTAMP} IS NULL OR o.created_at <= #{to,jdbcType=TIMESTAMP})
              AND (#{clientId,jdbcType=OTHER} IS NULL OR o.client_id = #{clientId,jdbcType=OTHER})
              AND (#{licensePlate} IS NULL OR o.license_plate = #{licensePlate})
            GROUP BY o.id, o.created_at, o.total
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
                   COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS totalRevenue,
                   COALESCE(AVG(oi.unit_price), 0) AS averagePrice
            FROM services_catalog sc
                     INNER JOIN order_items oi ON oi.service_id = sc.id
                     INNER JOIN orders o ON o.id = oi.order_id
            WHERE sc.active = true
              AND (#{from,jdbcType=TIMESTAMP} IS NULL OR o.created_at >= #{from,jdbcType=TIMESTAMP})
              AND (#{to,jdbcType=TIMESTAMP} IS NULL OR o.created_at <= #{to,jdbcType=TIMESTAMP})
            GROUP BY sc.id, sc.name
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
                   COALESCE(SUM(COALESCE(parts_data.parts_cost, 0)), 0) AS partsCost,
                   COALESCE(SUM(COALESCE(labor_data.labor_cost, 0)), 0) AS laborCost,
                   COALESCE(SUM(o.total), 0) - 
                   COALESCE(SUM(COALESCE(parts_data.parts_cost, 0)), 0) - 
                   COALESCE(SUM(COALESCE(labor_data.labor_cost, 0)), 0) AS profit,
                   CASE 
                       WHEN COALESCE(SUM(o.total), 0) > 0 THEN
                           ((COALESCE(SUM(o.total), 0) - 
                             COALESCE(SUM(COALESCE(parts_data.parts_cost, 0)), 0) - 
                             COALESCE(SUM(COALESCE(labor_data.labor_cost, 0)), 0)) / COALESCE(SUM(o.total), 1)) * 100
                       ELSE 0
                   END AS profitMargin
            FROM clients c
                     INNER JOIN orders o ON o.client_id = c.id
                         AND o.status = 'COMPLETED'
                         AND (#{from,jdbcType=TIMESTAMP} IS NULL OR o.created_at >= #{from,jdbcType=TIMESTAMP})
                         AND (#{to,jdbcType=TIMESTAMP} IS NULL OR o.created_at <= #{to,jdbcType=TIMESTAMP})
                     LEFT JOIN (
                         SELECT oi2.order_id,
                                COALESCE(SUM(ip.quantity * ip.unit_price), 0) AS parts_cost
                         FROM order_items oi2
                                  LEFT JOIN order_item_parts ip ON ip.order_item_id = oi2.id
                         GROUP BY oi2.order_id
                     ) parts_data ON parts_data.order_id = o.id
                     LEFT JOIN (
                         SELECT oi3.order_id,
                                COALESCE(SUM(oi3.quantity * oi3.unit_price), 0) AS labor_cost
                         FROM order_items oi3
                         GROUP BY oi3.order_id
                     ) labor_data ON labor_data.order_id = o.id
            GROUP BY c.id, c.first_name, c.last_name
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
                           (COUNT(DISTINCT CASE WHEN o.status = 'COMPLETED' THEN a.order_item_id END)::NUMERIC / 
                            COUNT(DISTINCT a.order_item_id)::NUMERIC) * 100
                       ELSE 0
                   END AS completionRate,
                   COALESCE(SUM(a.estimated_hours), 0) AS totalHours,
                   CASE 
                       WHEN COUNT(DISTINCT a.order_item_id) > 0 THEN
                           COALESCE(SUM(a.estimated_hours), 0)::NUMERIC / COUNT(DISTINCT a.order_item_id)::NUMERIC
                       ELSE 0
                   END AS avgHoursPerOrder,
                   COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity * oi.unit_price ELSE 0 END), 0) AS revenueGenerated
            FROM mechanics m
                     LEFT JOIN assignments a ON a.mechanic_id = m.id
                     LEFT JOIN order_items oi ON oi.id = a.order_item_id
                     LEFT JOIN orders o ON o.id = oi.order_id
                         AND (#{from,jdbcType=TIMESTAMP} IS NULL OR o.created_at >= #{from,jdbcType=TIMESTAMP})
                         AND (#{to,jdbcType=TIMESTAMP} IS NULL OR o.created_at <= #{to,jdbcType=TIMESTAMP})
            WHERE m.active = true
            GROUP BY m.id, m.first_name, m.last_name, m.specialization
            ORDER BY completionRate DESC, revenueGenerated DESC
            """)
    List<Map<String, Object>> mechanicProductivity(@Param("from") Instant from,
                                                   @Param("to") Instant to);
}
