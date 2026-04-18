package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InventoryAvgCostReport(
        List<InventoryAvgCostEntry> entries,
        BigDecimal totalInventoryValue
) {
    public record InventoryAvgCostEntry(
            UUID partId,
            String partName,
            String sku,
            int currentStock,
            BigDecimal currentUnitPrice,
            BigDecimal avgEntryCost,
            BigDecimal inventoryValue
    ) {}
}
