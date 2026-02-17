package com.motorplus.motorplus.model;

import com.motorplus.motorplus.dto.ordersDtos.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Order {
    private UUID id;
    private UUID clientId;
    private String licensePlate;
    private OrderStatus status;
    private String description;
    private BigDecimal total;
    private Instant createdAt;
    private Instant updatedAt;


}
