package com.motorplus.motorplus.dto.movementDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovementDto(
        UUID id,
        UUID partId,
        MovementType type,
        int quantity,
        Instant performedAt,
        String notes,
        UUID supplierId,
        BigDecimal entryCost
) {
}
