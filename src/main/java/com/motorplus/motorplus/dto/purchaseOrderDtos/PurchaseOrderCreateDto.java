package com.motorplus.motorplus.dto.purchaseOrderDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderCreateDto(
        @NotNull UUID partId,
        UUID supplierId,
        @NotNull @Min(1) int quantity,
        BigDecimal unitCost,
        String notes
) {
}
