package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ServicePopularityReport(
        Instant from,
        Instant to,
        List<ServicePopularityEntry> entries
) {
    public record ServicePopularityEntry(
            UUID serviceId,
            String serviceName,
            long timesRequested,
            BigDecimal totalRevenue,
            BigDecimal averagePrice
    ) { }
}