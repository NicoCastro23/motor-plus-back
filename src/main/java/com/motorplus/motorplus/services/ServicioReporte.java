package com.motorplus.motorplus.services;

import java.time.LocalDate;
import java.util.UUID;

public interface ServicioReporte {
    VehicleHistoryDto vehicleHistory(String placa);
    MechanicPerformanceReport performance(LocalDate from, LocalDate to, Long mecanicoId);
    PartTraceabilityReport traceability(UUID repuestoId, LocalDate from, LocalDate to);
    OrderMarginReport margin(LocalDate from, LocalDate to, UUID clienteId, String placa);
}
