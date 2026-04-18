package com.motorplus.motorplus.dto.purchaseOrderDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchaseOrderReturnDto(
        @NotNull @Min(1) Integer quantity,
        String notes
) {}
