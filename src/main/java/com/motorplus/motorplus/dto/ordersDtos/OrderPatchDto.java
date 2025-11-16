package com.motorplus.motorplus.dto.ordersDtos;

public record OrderPatchDto(
        String description,
        OrderStatus status,
        String licensePlate
) {
}
