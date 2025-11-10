package com.motorplus.motorplus.dto.mechanicDtos;

import jakarta.validation.constraints.NotBlank;

public record MechanicCreateDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String specialization,
        @NotBlank String phone
) {
}
