package com.motorplus.motorplus.dto.invoiceDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceDto(
        UUID id,
        UUID orderId,
        String number,
        InvoiceStatus status,
        Instant issueDate,
        Instant dueDate,
        BigDecimal total,
        BigDecimal balance
) {
}
