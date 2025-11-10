package com.motorplus.motorplus.dto.reportDtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PartTraceabilityReport(
        UUID partId,
        Instant from,
        Instant to,
        List<TraceabilityEntry> entries
) {
    public record TraceabilityEntry(
            String orderNumber,
            String vehiclePlate,
            int quantityUsed,
            Instant usedAt
    ) { }
}
