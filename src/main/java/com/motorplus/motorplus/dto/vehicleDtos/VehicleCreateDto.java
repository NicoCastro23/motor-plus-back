package com.motorplus.motorplus.dto.vehicleDtos;

import jakarta.validation.constraints.*;

import java.time.Year;

public record VehicleCreateDto(
        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 60, message = "La marca no puede exceder 60 caracteres")
        String brand,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 60, message = "El modelo no puede exceder 60 caracteres")
        String model,

        @NotBlank(message = "La placa es obligatoria")
        @Size(max = 15, message = "La placa no puede exceder 15 caracteres")
        String licensePlate,

        @NotNull(message = "El año del modelo es obligatorio")
        @Min(value = 1900, message = "El año del modelo debe ser mayor o igual a 1900")
        @Max(value = Year.MAX_VALUE, message = "El año del modelo es inválido")
        Integer modelYear
) { }
