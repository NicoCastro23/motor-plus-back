package com.motorplus.motorplus.dto.invoiceDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentDto(
        UUID id,
        UUID invoiceId,
        BigDecimal amount,
        String method,
        Instant paymentDate,
        String reference
) {
}
