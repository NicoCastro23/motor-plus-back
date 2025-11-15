package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.reportDtos.*;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleHistoryDto;
import java.time.LocalDate;
import java.util.UUID;

public interface ServicioReporte {
    VehicleHistoryDto vehicleHistory(String placa);
    MechanicPerformanceReport performance(LocalDate from, LocalDate to, UUID mecanicoId);
    PartTraceabilityReport traceability(UUID repuestoId, LocalDate from, LocalDate to);
    OrderMarginReport margin(LocalDate from, LocalDate to, UUID clienteId, String placa);
    ClientActivityReport clientActivity();
    PartStockReport partStockStatus();
    ServicePopularityReport servicePopularity(LocalDate from, LocalDate to);
    PendingInvoicesReport pendingInvoices();
    ClientProfitabilityReport clientProfitability(LocalDate from, LocalDate to);
    MechanicProductivityReport mechanicProductivity(LocalDate from, LocalDate to);
}
