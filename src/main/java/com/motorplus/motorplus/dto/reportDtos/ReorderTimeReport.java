package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReorderTimeReport(
        List<ReorderTimeEntry> entries,
        BigDecimal overallAvgDays
) {
    public record ReorderTimeEntry(
            UUID partId,
            String partName,
            String sku,
            long totalOrders,
            long receivedOrders,
            BigDecimal avgReorderDays
    ) {}
}
