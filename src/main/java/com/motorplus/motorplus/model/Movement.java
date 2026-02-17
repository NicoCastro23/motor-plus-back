package com.motorplus.motorplus.model;

import com.motorplus.motorplus.dto.movementDtos.MovementType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Movement {
    private UUID id;
    private UUID partId;
    private MovementType type;
    private int quantity;
    private Instant performedAt;
    private String notes;


}
