package com.motorplus.motorplus.dto.invoiceDtos;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceLineDto(
        UUID invoiceId,
        LineType type,
        UUID referenceId,
        String description,
        BigDecimal amount
) {
}
