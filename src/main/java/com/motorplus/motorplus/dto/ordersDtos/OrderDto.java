package com.motorplus.motorplus.dto.ordersDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID clientId,
        String licensePlate,
        OrderStatus status,
        String description,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt
) {
}
