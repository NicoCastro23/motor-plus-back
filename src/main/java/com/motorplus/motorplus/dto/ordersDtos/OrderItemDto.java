package com.motorplus.motorplus.dto.ordersDtos;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        UUID id,
        UUID orderId,
        UUID serviceId,
        String description,
        int quantity,
        BigDecimal unitPrice
) {
}
