package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
public class Supplier {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private boolean active;
    private Instant createdAt;


}
