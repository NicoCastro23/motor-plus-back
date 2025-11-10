package com.motorplus.motorplus.dto.supervisionDtos;

import java.time.Instant;
import java.util.UUID;

public record SupervisionDto(
        UUID supervisorId,
        UUID supervisadoId,
        UUID orderId,
        Instant createdAt,
        String notes
) {
}
