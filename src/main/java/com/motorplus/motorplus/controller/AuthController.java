package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.authDtos.ChangePasswordRequest;
import com.motorplus.motorplus.dto.authDtos.LoginRequest;
import com.motorplus.motorplus.dto.authDtos.LoginResponse;
import com.motorplus.motorplus.dto.authDtos.RegisterRequest;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.security.LoginRateLimiter;
import com.motorplus.motorplus.services.AuthService;
import com.motorplus.motorplus.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiter rateLimiter;

    public AuthController(AuthService authService, JwtUtil jwtUtil, LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String ip = resolveClientIp(httpRequest);
        if (rateLimiter.isBlocked(ip)) {
            throw new ResourceConflictException("Demasiados intentos fallidos. Intente de nuevo en 10 minutos.");
        }
        try {
            LoginResponse response = authService.login(request);
            rateLimiter.clearFailures(ip);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            rateLimiter.recordFailure(ip);
            throw ex;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        String username = jwtUtil.extractUsername(token);
        authService.changePassword(username, request);
        return ResponseEntity.ok().build();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("Token no encontrado");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
