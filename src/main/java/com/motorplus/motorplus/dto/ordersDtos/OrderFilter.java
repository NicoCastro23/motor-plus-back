package com.motorplus.motorplus.dto.ordersDtos;

import java.time.Instant;
import java.util.UUID;

public record OrderFilter(
        UUID clientId,
        String licensePlate,
        OrderStatus status,
        Instant from,
        Instant to
) {
}
