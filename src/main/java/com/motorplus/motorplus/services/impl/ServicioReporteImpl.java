package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.reportDtos.*;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleHistoryDto;
import com.motorplus.motorplus.mapper.ReportMapper;
import com.motorplus.motorplus.services.ServicioReporte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ServicioReporteImpl implements ServicioReporte {

    private final ReportMapper reportMapper;

    public ServicioReporteImpl(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override
    public VehicleHistoryDto vehicleHistory(String placa) {
        List<Map<String, Object>> rows = reportMapper.vehicleHistory(placa);
        List<VehicleHistoryDto.VehicleHistoryEntry> entries = rows.stream()
                .map(row -> new VehicleHistoryDto.VehicleHistoryEntry(
                        toInstant(value(row, "eventDate", "eventdate")),
                        stringValue(value(row, "description")),
                        stringValue(value(row, "reference"))
                ))
                .toList();
        return new VehicleHistoryDto(placa, entries);
    }

    @Override
    public MechanicPerformanceReport performance(LocalDate from, LocalDate to, UUID mecanicoId) {
        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        List<Map<String, Object>> rows = reportMapper.mechanicPerformance(fromInstant, toInstant, mecanicoId);
        List<MechanicPerformanceReport.MechanicPerformanceEntry> entries = rows.stream()
                .map(row -> new MechanicPerformanceReport.MechanicPerformanceEntry(
                        stringValue(value(row, "mechanicName", "mechanicname")),
                        longValue(value(row, "ordersCompleted", "orderscompleted")),
                        bigDecimal(value(row, "totalHours", "totalhours"))
                ))
                .toList();
        return new MechanicPerformanceReport(mecanicoId, fromInstant, toInstant, entries);
    }

    @Override
    public PartTraceabilityReport traceability(UUID repuestoId, LocalDate from, LocalDate to) {
        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        List<Map<String, Object>> rows = reportMapper.partTraceability(repuestoId, fromInstant, toInstant);
        List<PartTraceabilityReport.TraceabilityEntry> entries = rows.stream()
                .map(row -> new PartTraceabilityReport.TraceabilityEntry(
                        stringValue(value(row, "orderNumber", "ordernumber")),
                        stringValue(value(row, "vehiclePlate", "vehicleplate")),
                        (int) longValue(value(row, "quantityUsed", "quantityused")),
                        toInstant(value(row, "usedAt", "usedat"))
                ))
                .toList();
        return new PartTraceabilityReport(repuestoId, fromInstant, toInstant, entries);
    }

    @Override
    public OrderMarginReport margin(LocalDate from, LocalDate to, UUID clienteId, String placa) {
        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        List<Map<String, Object>> rows = reportMapper.orderMargins(fromInstant, toInstant, clienteId, placa);
        List<OrderMarginReport.OrderMarginEntry> entries = rows.stream()
                .map(row -> new OrderMarginReport.OrderMarginEntry(
                        (UUID) value(row, "orderId", "orderid"),
                        stringValue(value(row, "orderNumber", "ordernumber")),
                        bigDecimal(value(row, "revenue")),
                        bigDecimal(value(row, "partCost", "partcost")),
                        bigDecimal(value(row, "margin"))
                ))
                .toList();
        return new OrderMarginReport(fromInstant, toInstant, clienteId, placa, entries);
    }

    // NUEVOS REPORTES IMPLEMENTADOS

    @Override
    public ClientActivityReport clientActivity() {
        List<Map<String, Object>> rows = reportMapper.clientActivity();
        List<ClientActivityReport.ClientActivityEntry> entries = rows.stream()
                .map(row -> new ClientActivityReport.ClientActivityEntry(
                        (UUID) value(row, "clientId", "clientid"),
                        stringValue(value(row, "clientName", "clientname")),
                        stringValue(value(row, "email")),
                        stringValue(value(row, "phone")),
                        (int) longValue(value(row, "vehicleCount", "vehiclecount")),
                        toInstant(value(row, "lastOrderDate", "lastorderdate"))
                ))
                .toList();
        return new ClientActivityReport(entries);
    }

    @Override
    public PartStockReport partStockStatus() {
        List<Map<String, Object>> rows = reportMapper.partStockStatus();
        BigDecimal totalValue = BigDecimal.ZERO;

        List<PartStockReport.PartStockEntry> entries = rows.stream()
                .map(row -> {
                    BigDecimal stockValue = bigDecimal(value(row, "stockValue", "stockvalue"));
                    return new PartStockReport.PartStockEntry(
                            (UUID) value(row, "partId", "partid"),
                            stringValue(value(row, "partName", "partname")),
                            stringValue(value(row, "sku")),
                            (int) longValue(value(row, "currentStock", "currentstock")),
                            bigDecimal(value(row, "unitPrice", "unitprice")),
                            stockValue,
                            stringValue(value(row, "status"))
                    );
                })
                .toList();

        totalValue = entries.stream()
                .map(PartStockReport.PartStockEntry::stockValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PartStockReport(entries, totalValue);
    }

    @Override
    public ServicePopularityReport servicePopularity(LocalDate from, LocalDate to) {
        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        List<Map<String, Object>> rows = reportMapper.servicePopularity(fromInstant, toInstant);
        List<ServicePopularityReport.ServicePopularityEntry> entries = rows.stream()
                .map(row -> new ServicePopularityReport.ServicePopularityEntry(
                        (UUID) value(row, "serviceId", "serviceid"),
                        stringValue(value(row, "serviceName", "servicename")),
                        longValue(value(row, "timesRequested", "timesrequested")),
                        bigDecimal(value(row, "totalRevenue", "totalrevenue")),
                        bigDecimal(value(row, "averagePrice", "averageprice"))
                ))
                .toList();
        return new ServicePopularityReport(fromInstant, toInstant, entries);
    }

    @Override
    public PendingInvoicesReport pendingInvoices() {
        List<Map<String, Object>> rows = reportMapper.pendingInvoices();
        BigDecimal totalPending = BigDecimal.ZERO;

        List<PendingInvoicesReport.PendingInvoiceEntry> entries = rows.stream()
                .map(row -> new PendingInvoicesReport.PendingInvoiceEntry(
                        (UUID) value(row, "invoiceId", "invoiceid"),
                        stringValue(value(row, "invoiceNumber", "invoicenumber")),
                        (UUID) value(row, "clientId", "clientid"),
                        stringValue(value(row, "clientName", "clientname")),
                        toInstant(value(row, "issueDate", "issuedate")),
                        toInstant(value(row, "dueDate", "duedate")),
                        bigDecimal(value(row, "total")),
                        bigDecimal(value(row, "balance")),
                        (int) longValue(value(row, "daysOverdue", "daysoverdue"))
                ))
                .toList();

        totalPending = entries.stream()
                .map(PendingInvoicesReport.PendingInvoiceEntry::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PendingInvoicesReport(entries, totalPending);
    }

    @Override
    public ClientProfitabilityReport clientProfitability(LocalDate from, LocalDate to) {
        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        List<Map<String, Object>> rows = reportMapper.clientProfitability(fromInstant, toInstant);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        List<ClientProfitabilityReport.ClientProfitabilityEntry> entries = rows.stream()
                .map(row -> {
                    BigDecimal partsCost = bigDecimal(value(row, "partsCost", "partscost"));
                    BigDecimal laborCost = bigDecimal(value(row, "laborCost", "laborcost"));
                    return new ClientProfitabilityReport.ClientProfitabilityEntry(
                            (UUID) value(row, "clientId", "clientid"),
                            stringValue(value(row, "clientName", "clientname")),
                            (int) longValue(value(row, "orderCount", "ordercount")),
                            bigDecimal(value(row, "revenue")),
                            partsCost,
                            laborCost,
                            bigDecimal(value(row, "profit")),
                            bigDecimal(value(row, "profitMargin", "profitmargin"))
                    );
                })
                .toList();

        totalRevenue = entries.stream()
                .map(ClientProfitabilityReport.ClientProfitabilityEntry::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalCost = entries.stream()
                .map(e -> e.partsCost().add(e.laborCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalProfit = entries.stream()
                .map(ClientProfitabilityReport.ClientProfitabilityEntry::profit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ClientProfitabilityReport(fromInstant, toInstant, entries, totalRevenue, totalCost, totalProfit);
    }

    @Override
    public MechanicProductivityReport mechanicProductivity(LocalDate from, LocalDate to) {
        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        List<Map<String, Object>> rows = reportMapper.mechanicProductivity(fromInstant, toInstant);
        List<MechanicProductivityReport.MechanicProductivityEntry> entries = rows.stream()
                .map(row -> new MechanicProductivityReport.MechanicProductivityEntry(
                        (UUID) value(row, "mechanicId", "mechanicid"),
                        stringValue(value(row, "mechanicName", "mechanicname")),
                        stringValue(value(row, "specialization")),
                        (int) longValue(value(row, "assignedOrders", "assignedorders")),
                        (int) longValue(value(row, "completedOrders", "completedorders")),
                        bigDecimal(value(row, "completionRate", "completionrate")),
                        bigDecimal(value(row, "totalHours", "totalhours")),
                        bigDecimal(value(row, "avgHoursPerOrder", "avghoursperorder")),
                        bigDecimal(value(row, "revenueGenerated", "revenuegenerated"))
                ))
                .toList();
        return new MechanicProductivityReport(fromInstant, toInstant, entries);
    }

    // MÉTODOS AUXILIARES

    private Instant toInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        return null;
    }

    private String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    private Object value(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        return null;
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    private BigDecimal bigDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}