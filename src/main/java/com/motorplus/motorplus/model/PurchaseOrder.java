package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class PurchaseOrder {
    private UUID id;
    private UUID partId;
    private UUID supplierId;
    private int quantity;
    private BigDecimal unitCost;
    private String status;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
