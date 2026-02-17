package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
public class Supervision {
    private UUID supervisorId;
    private UUID supervisadoId;
    private UUID orderId;
    private Instant createdAt;
    private String notes;


}
