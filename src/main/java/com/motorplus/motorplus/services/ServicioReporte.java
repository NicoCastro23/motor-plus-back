package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.reportDtos.MechanicPerformanceReport;
import com.motorplus.motorplus.dto.reportDtos.OrderMarginReport;
import com.motorplus.motorplus.dto.reportDtos.PartTraceabilityReport;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleHistoryDto;
import java.time.LocalDate;
import java.util.UUID;

public interface ServicioReporte {
    VehicleHistoryDto vehicleHistory(String placa);
    MechanicPerformanceReport performance(LocalDate from, LocalDate to, UUID mecanicoId);
    PartTraceabilityReport traceability(UUID repuestoId, LocalDate from, LocalDate to);
    OrderMarginReport margin(LocalDate from, LocalDate to, UUID clienteId, String placa);
}
