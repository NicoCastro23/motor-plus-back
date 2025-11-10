package com.motorplus.motorplus.dto.assigmentDto;

import jakarta.validation.constraints.Min;

public record AssignmentPatchDto(
        @Min(1) Integer estimatedHours
) {
}
