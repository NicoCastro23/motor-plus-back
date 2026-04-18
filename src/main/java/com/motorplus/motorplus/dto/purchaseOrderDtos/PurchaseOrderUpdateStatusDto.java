package com.motorplus.motorplus.dto.purchaseOrderDtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseOrderUpdateStatusDto(
        @NotNull PurchaseOrderStatus status,
        BigDecimal unitCost
) {
}
