package com.motorplus.motorplus.dto.invoiceDtos;

import java.time.Instant;

public record InvoicePatchDto(
        Instant dueDate,
        InvoiceStatus status
) {
}
