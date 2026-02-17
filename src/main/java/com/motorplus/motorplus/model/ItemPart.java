package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
public class ItemPart {
    private UUID orderItemId;
    private UUID partId;
    private int quantity;
    private BigDecimal unitPrice;
}


