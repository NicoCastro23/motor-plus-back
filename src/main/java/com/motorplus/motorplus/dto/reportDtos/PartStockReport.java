package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PartStockReport(
        List<PartStockEntry> entries,
        BigDecimal totalInventoryValue
) {
    public record PartStockEntry(
            UUID partId,
            String partName,
            String sku,
            int currentStock,
            BigDecimal unitPrice,
            BigDecimal stockValue,
            String status // "OK", "LOW_STOCK", "OUT_OF_STOCK"
    ) { }
}