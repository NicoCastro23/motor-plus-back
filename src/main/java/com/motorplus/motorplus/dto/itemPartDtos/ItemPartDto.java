package com.motorplus.motorplus.dto.itemPartDtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPartDto(
        UUID orderItemId,
        UUID partId,
        int quantity,
        BigDecimal unitPrice
) {
}
