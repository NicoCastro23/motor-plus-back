package com.motorplus.motorplus.dto.vehicleDtos;

import java.time.Instant;
import java.util.List;

public record VehicleHistoryDto(
        String licensePlate,
        List<VehicleHistoryEntry> entries
) {
    public record VehicleHistoryEntry(
            Instant eventDate,
            String description,
            String reference
    ) { }
}
