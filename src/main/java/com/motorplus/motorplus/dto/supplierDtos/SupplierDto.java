package com.motorplus.motorplus.dto.supplierDtos;

import java.time.Instant;
import java.util.UUID;

public record SupplierDto(
        UUID id,
        String name,
        String email,
        String phone,
        boolean active,
        Instant createdAt
) {
}
