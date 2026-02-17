package com.motorplus.motorplus.model;

import com.motorplus.motorplus.dto.invoiceDtos.LineType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
public class InvoiceLine {
    private UUID invoiceId;
    private LineType type;
    private UUID referenceId;
    private String description;
    private BigDecimal amount;


}
