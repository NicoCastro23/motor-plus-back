package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Part {
    private UUID id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal unitPrice;
    private int stock;
    private boolean active;
    private Instant createdAt;


}
