package com.motorplus.motorplus.dto.assigmentDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignmentCreateDto(
        @NotNull UUID mechanicId,
        @Min(1) Integer estimatedHours
) {
}
