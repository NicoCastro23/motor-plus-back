package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.reportDtos.MechanicPerformanceReport;
import com.motorplus.motorplus.dto.reportDtos.OrderMarginReport;
import com.motorplus.motorplus.dto.reportDtos.PartTraceabilityReport;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleHistoryDto;
import com.motorplus.motorplus.services.ServicioReporte;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.motorplus.motorplus.dto.reportDtos.*;


import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ServicioReporte servicioReporte;

    public ReportController(ServicioReporte servicioReporte) {
        this.servicioReporte = servicioReporte;
    }

    @GetMapping("/vehicles/{plate}")
    public VehicleHistoryDto vehicleHistory(@PathVariable("plate") String plate) {
        return servicioReporte.vehicleHistory(plate);
    }

    @GetMapping("/mechanics/performance")
    public MechanicPerformanceReport performance(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                 @RequestParam(required = false) UUID mechanicId) {
        return servicioReporte.performance(from, to, mechanicId);
    }

    @GetMapping("/parts/{partId}/traceability")
    public PartTraceabilityReport traceability(@PathVariable UUID partId,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return servicioReporte.traceability(partId, from, to);
    }

    @GetMapping("/orders/margin")
    public OrderMarginReport margin(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                    @RequestParam(required = false) UUID clientId,
                                    @RequestParam(required = false) String plate) {
        return servicioReporte.margin(from, to, clientId, plate);
    }

    @GetMapping("/clients/activity")
    public ClientActivityReport clientActivity() {
        return servicioReporte.clientActivity();
    }

    @GetMapping("/parts/stock-status")
    public PartStockReport partStockStatus() {
        return servicioReporte.partStockStatus();
    }

    @GetMapping("/services/popularity")
    public ServicePopularityReport servicePopularity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return servicioReporte.servicePopularity(from, to);
    }

    @GetMapping("/invoices/pending")
    public PendingInvoicesReport pendingInvoices() {
        return servicioReporte.pendingInvoices();
    }

    @GetMapping("/clients/profitability")
    public ClientProfitabilityReport clientProfitability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return servicioReporte.clientProfitability(from, to);
    }

    @GetMapping("/mechanics/productivity")
    public MechanicProductivityReport mechanicProductivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return servicioReporte.mechanicProductivity(from, to);
    }
}
