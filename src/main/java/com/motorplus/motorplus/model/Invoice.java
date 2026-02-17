package com.motorplus.motorplus.model;

import com.motorplus.motorplus.dto.invoiceDtos.InvoiceStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
public class Invoice {
    private UUID id;
    private UUID orderId;
    private String number;
    private InvoiceStatus status;
    private Instant issueDate;
    private Instant dueDate;
    private BigDecimal total;
    private BigDecimal balance;


}
