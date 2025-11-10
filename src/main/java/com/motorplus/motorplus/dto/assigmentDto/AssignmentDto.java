package com.motorplus.motorplus.dto.assigmentDto;

import java.time.Instant;
import java.util.UUID;

public record AssignmentDto(
        UUID orderItemId,
        UUID mechanicId,
        Instant assignedAt,
        Integer estimatedHours
) {
}
