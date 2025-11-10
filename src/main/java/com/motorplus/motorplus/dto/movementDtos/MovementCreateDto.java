package com.motorplus.motorplus.dto.movementDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovementCreateDto(
        @NotNull MovementType type,
        @Min(1) int quantity,
        String notes
) {
}
