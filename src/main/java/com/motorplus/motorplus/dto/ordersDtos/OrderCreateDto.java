package com.motorplus.motorplus.dto.ordersDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderCreateDto(
        @NotNull UUID clientId,
        @NotBlank String licensePlate,
        @NotBlank String description
) {
}
