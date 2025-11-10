package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MechanicPerformanceReport(
        UUID mechanicId,
        Instant from,
        Instant to,
        List<MechanicPerformanceEntry> entries
) {
    public record MechanicPerformanceEntry(
            String mechanicName,
            long ordersCompleted,
            BigDecimal totalHours
    ) { }
}
