package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class StockAlert {
    private UUID id;
    private UUID partId;
    private int currentStock;
    private int minStock;
    private Instant createdAt;
    private boolean resolved;
    private Instant resolvedAt;
}
