package com.motorplus.motorplus.dto.purchaseOrderDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PurchaseOrderDto(
        UUID id,
        UUID partId,
        String partName,
        String partSku,
        UUID supplierId,
        String supplierName,
        int quantity,
        BigDecimal unitCost,
        PurchaseOrderStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
