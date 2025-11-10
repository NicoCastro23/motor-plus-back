package com.motorplus.motorplus.services.impl;

import com.motorplus.motorplus.dto.reportDtos.MechanicPerformanceReport;
import com.motorplus.motorplus.dto.reportDtos.OrderMarginReport;
import com.motorplus.motorplus.dto.reportDtos.PartTraceabilityReport;
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
                        stringValue(value(row, "orderId", "orderid")),
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
