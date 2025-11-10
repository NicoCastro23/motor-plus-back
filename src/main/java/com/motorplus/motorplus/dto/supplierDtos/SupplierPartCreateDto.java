package com.motorplus.motorplus.dto.supplierDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SupplierPartCreateDto(
        @NotNull UUID partId,
        @NotNull BigDecimal price,
        @Min(1) Integer minQuantity
) {
}
