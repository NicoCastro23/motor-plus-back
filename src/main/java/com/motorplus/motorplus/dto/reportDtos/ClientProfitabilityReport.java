package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClientProfitabilityReport(
        Instant from,
        Instant to,
        List<ClientProfitabilityEntry> entries,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal totalProfit
) {
    public record ClientProfitabilityEntry(
            UUID clientId,
            String clientName,
            int orderCount,
            BigDecimal revenue,
            BigDecimal partsCost,
            BigDecimal laborCost,
            BigDecimal profit,
            BigDecimal profitMargin
    ) { }
}