package com.motorplus.motorplus.dto.supplierDtos;

import java.math.BigDecimal;
import java.util.UUID;

public record SupplierPartDto(
        UUID supplierId,
        UUID partId,
        BigDecimal price,
        Integer minQuantity
) {
}
