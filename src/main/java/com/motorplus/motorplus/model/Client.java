package com.motorplus.motorplus.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Client {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Instant createdAt;


}
