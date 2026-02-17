package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class SupplierPart {
    private UUID supplierId;
    private UUID partId;
    private BigDecimal price;
    private Integer minQuantity;


}
