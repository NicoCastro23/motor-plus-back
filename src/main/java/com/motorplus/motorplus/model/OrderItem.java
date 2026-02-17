package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class OrderItem {
    private UUID id;
    private UUID orderId;
    private UUID serviceId;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;


}
