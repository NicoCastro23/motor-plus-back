package com.motorplus.motorplus.dto.itemPartDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPartCreateDto(
        @NotNull UUID partId,
        @Min(1) int quantity,
        @NotNull BigDecimal unitPrice
) {
}
