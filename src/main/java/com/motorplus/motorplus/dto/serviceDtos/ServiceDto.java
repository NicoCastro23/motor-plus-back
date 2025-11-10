package com.motorplus.motorplus.dto.serviceDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServiceDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        Instant createdAt
) {
}
