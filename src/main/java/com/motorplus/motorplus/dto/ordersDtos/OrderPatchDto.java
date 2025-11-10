package com.motorplus.motorplus.dto.ordersDtos;

import jakarta.validation.constraints.NotBlank;

public record OrderPatchDto(
        String description,
        OrderStatus status,
        @NotBlank(message = "La placa es obligatoria") String licensePlate
) {
}
