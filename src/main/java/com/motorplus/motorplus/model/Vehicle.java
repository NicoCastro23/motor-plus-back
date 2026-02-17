package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Vehicle {
    private UUID id;
    private UUID clientId;
    private String brand;
    private String model;
    private String licensePlate;
    private Integer modelYear;
    private Instant createdAt;
    private String clientName;


}
