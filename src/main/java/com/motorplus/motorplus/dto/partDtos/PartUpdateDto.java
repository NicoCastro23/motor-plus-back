package com.motorplus.motorplus.dto.partDtos;

import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record PartUpdateDto(
        String name,
        String sku,
        String description,
        @Min(0) BigDecimal unitPrice,
        Integer stock,
        Boolean active
) {
}
