package com.motorplus.motorplus.dto.supplierDtos;

public record SupplierUpdateDto(
        String name,
        String email,
        String phone,
        Boolean active
) {
}
