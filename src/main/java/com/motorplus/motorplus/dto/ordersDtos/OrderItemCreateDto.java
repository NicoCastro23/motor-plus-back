package com.motorplus.motorplus.dto.ordersDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemCreateDto(
        @NotNull UUID serviceId,
        String description,
        @Min(1) int quantity,
        @NotNull BigDecimal unitPrice
) {
}
