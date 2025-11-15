package com.motorplus.motorplus.dto.reportDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PendingInvoicesReport(
        List<PendingInvoiceEntry> entries,
        BigDecimal totalPending
) {
    public record PendingInvoiceEntry(
            UUID invoiceId,
            String invoiceNumber,
            UUID clientId,
            String clientName,
            Instant issueDate,
            Instant dueDate,
            BigDecimal total,
            BigDecimal balance,
            int daysOverdue
    ) { }
}