package com.motorplus.motorplus.dto.itemPartDtos;

import java.math.BigDecimal;

public record ItemPartPatchDto(
        Integer quantity,
        BigDecimal unitPrice
) {
}
