package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MechanicProductivityReport(
        Instant from,
        Instant to,
        List<MechanicProductivityEntry> entries
) {
    public record MechanicProductivityEntry(
            UUID mechanicId,
            String mechanicName,
            String specialization,
            int assignedOrders,
            int completedOrders,
            BigDecimal completionRate,
            BigDecimal totalHours,
            BigDecimal avgHoursPerOrder,
            BigDecimal revenueGenerated
    ) { }
}