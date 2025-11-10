package com.motorplus.motorplus.dto.partDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PartCreateDto(
        @NotBlank String name,
        @NotBlank String sku,
        String description,
        @NotNull @Min(0) BigDecimal unitPrice,
        @Min(0) int stock
) {
}
