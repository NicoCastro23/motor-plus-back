package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ServiceCatalog {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;
    private Instant createdAt;


}
