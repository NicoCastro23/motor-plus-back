package com.motorplus.motorplus.dto.stockAlertDtos;

import java.time.Instant;
import java.util.UUID;

public record StockAlertDto(
        UUID id,
        UUID partId,
        String partName,
        String partSku,
        int currentStock,
        int minStock,
        Instant createdAt,
        boolean resolved,
        Instant resolvedAt
) {
}
