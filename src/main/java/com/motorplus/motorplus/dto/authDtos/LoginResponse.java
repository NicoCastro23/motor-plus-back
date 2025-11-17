package com.motorplus.motorplus.dto.authDtos;

public record LoginResponse(
        String token,
        String username,
        String email
) {
}

