package com.motorplus.motorplus.dto.vehicleDtos;

import java.time.Instant;
import java.util.UUID;

public record VehicleDto(
        UUID id,
        UUID clientId,
        String brand,
        String model,
        String licensePlate,
        Integer modelYear,
        Instant createdAt,
        String clientName
) {
}
