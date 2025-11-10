package com.motorplus.motorplus.dto.serviceDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ServiceUpdateDto(
        @NotBlank String name,
        String description,
        @Min(0) BigDecimal price,
        Boolean active
) {
}
