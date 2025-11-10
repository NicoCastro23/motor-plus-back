package com.motorplus.motorplus.dto.mechanicDtos;

import jakarta.validation.constraints.NotBlank;

public record MechanicUpdateDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String specialization,
        String phone,
        Boolean active
) {
}
