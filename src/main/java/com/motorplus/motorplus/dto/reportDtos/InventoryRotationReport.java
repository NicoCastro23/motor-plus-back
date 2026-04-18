package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InventoryRotationReport(
        List<InventoryRotationEntry> entries
) {
    public record InventoryRotationEntry(
            UUID partId,
            String partName,
            String sku,
            int currentStock,
            long totalIn,
            long totalOut,
            BigDecimal rotationRate
    ) {}
}
