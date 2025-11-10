package com.motorplus.motorplus.dto.supplierDtos;

import jakarta.validation.constraints.NotBlank;

public record SupplierCreateDto(
        @NotBlank String name,
        String email,
        String phone
) {
}
