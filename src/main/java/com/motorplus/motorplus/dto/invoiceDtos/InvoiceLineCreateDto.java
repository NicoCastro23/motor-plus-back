package com.motorplus.motorplus.dto.invoiceDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceLineCreateDto(
        @NotNull LineType type,
        UUID referenceId,
        @NotBlank String description,
        @NotNull BigDecimal amount
) {
}
