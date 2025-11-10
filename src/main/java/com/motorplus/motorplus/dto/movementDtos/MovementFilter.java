package com.motorplus.motorplus.dto.movementDtos;

import java.time.Instant;

public record MovementFilter(
        MovementType type,
        Instant from,
        Instant to
) {
}
