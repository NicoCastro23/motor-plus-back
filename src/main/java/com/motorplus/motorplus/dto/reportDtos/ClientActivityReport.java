package com.motorplus.motorplus.dto.reportDtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClientActivityReport(
        List<ClientActivityEntry> entries
) {
    public record ClientActivityEntry(
            UUID clientId,
            String clientName,
            String email,
            String phone,
            int vehicleCount,
            Instant lastOrderDate
    ) { }
}