package com.motorplus.motorplus.dto.supplierDtos;

import java.math.BigDecimal;

public record SupplierPartPatchDto(
        BigDecimal price,
        Integer minQuantity
) {
}
