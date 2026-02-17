package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
@Getter
@Setter
public class Mechanic {
    private UUID id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String phone;
    private boolean active;
    private Instant createdAt;

}
