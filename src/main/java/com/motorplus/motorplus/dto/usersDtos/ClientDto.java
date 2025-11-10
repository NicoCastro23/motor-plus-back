package com.motorplus.motorplus.dto.usersDtos;

import java.time.Instant;
import java.util.UUID;

public record ClientDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Instant createdAt
) {
}
