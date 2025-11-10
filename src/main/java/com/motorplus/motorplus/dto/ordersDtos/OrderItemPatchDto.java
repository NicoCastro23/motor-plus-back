package com.motorplus.motorplus.dto.ordersDtos;

import java.math.BigDecimal;

public record OrderItemPatchDto(
        String description,
        Integer quantity,
        BigDecimal unitPrice
) {
}
