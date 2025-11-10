package com.motorplus.motorplus.dto.invoiceDtos;

import java.math.BigDecimal;

public record InvoiceLinePatchDto(
        String description,
        BigDecimal amount
) {
}
