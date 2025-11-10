package com.motorplus.motorplus.dto.invoiceDtos;

import java.time.Instant;
import java.util.UUID;

public record InvoiceFilter(
        UUID orderId,
        InvoiceStatus status,
        Instant from,
        Instant to
) {
}
