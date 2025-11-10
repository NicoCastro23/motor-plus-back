package com.motorplus.motorplus.dto.supervisionDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SupervisionCreateDto(
        @NotNull UUID supervisorId,
        @NotNull UUID supervisadoId,
        @NotNull UUID orderId,
        @NotBlank String notes
) {
}
