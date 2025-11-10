package com.motorplus.motorplus.dto.vehicleDtos;

import jakarta.validation.constraints.*;

public record VehicleUpdateDto(
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String licensePlate,
        @NotNull @Min(1900) @Max(9999) Integer modelYear
) {
}
