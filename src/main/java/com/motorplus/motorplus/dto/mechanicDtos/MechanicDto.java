package com.motorplus.motorplus.dto.mechanicDtos;

import java.time.Instant;
import java.util.UUID;

public record MechanicDto(
        UUID id,
        String firstName,
        String lastName,
        String specialization,
        String phone,
        boolean active,
        Instant createdAt
) {
}
