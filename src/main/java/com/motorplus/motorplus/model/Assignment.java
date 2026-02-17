package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Assignment {
    private UUID orderItemId;
    private UUID mechanicId;
    private Instant assignedAt;
    private Integer estimatedHours;
}
