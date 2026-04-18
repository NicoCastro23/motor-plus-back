package com.motorplus.motorplus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Admin {
    private UUID id;
    private String username;
    private String password;
    private String email;
    private boolean active;
    private Instant createdAt;
}
