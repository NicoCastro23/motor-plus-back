package com.motorplus.motorplus.model;

import com.motorplus.motorplus.dto.invoiceDtos.LineType;

import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceLine {
    private UUID invoiceId;
    private LineType type;
    private UUID referenceId;
    private String description;
    private BigDecimal amount;

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public LineType getType() {
        return type;
    }

    public void setType(LineType type) {
        this.type = type;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
