package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderMarginReport(
        Instant from,
        Instant to,
        UUID clientId,
        String licensePlate,
        List<OrderMarginEntry> entries
) {
    public record OrderMarginEntry(
            UUID orderId,
            String orderNumber,
            BigDecimal revenue,
            BigDecimal cost,
            BigDecimal margin
    ) { }
}
