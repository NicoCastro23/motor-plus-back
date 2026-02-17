package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Payment {
    private UUID id;
    private UUID invoiceId;
    private BigDecimal amount;
    private String method;
    private Instant paymentDate;
    private String reference;


}
