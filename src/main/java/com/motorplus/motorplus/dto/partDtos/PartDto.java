package com.motorplus.motorplus.dto.partDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PartDto(
        UUID id,
        String name,
        String sku,
        String description,
        BigDecimal unitPrice,
        int stock,
        int minStock,
        boolean active,
        Instant createdAt
) {
}
