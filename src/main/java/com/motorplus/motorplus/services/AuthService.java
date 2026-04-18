package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.authDtos.ChangePasswordRequest;
import com.motorplus.motorplus.dto.authDtos.LoginRequest;
import com.motorplus.motorplus.dto.authDtos.LoginResponse;
import com.motorplus.motorplus.dto.authDtos.RegisterRequest;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.AdminMapper;
import com.motorplus.motorplus.model.Admin;
import com.motorplus.motorplus.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AdminMapper adminMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        Admin admin = adminMapper.findByUsername(request.username());

        if (admin == null) {
            throw new ResourceNotFoundException("Credenciales inválidas");
        }

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new ResourceNotFoundException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(admin.getUsername());

        return new LoginResponse(token, admin.getUsername(), admin.getEmail());
    }

    public void register(RegisterRequest request) {
        if (adminMapper.findByUsernameAny(request.username()) != null) {
            throw new ResourceConflictException("El nombre de usuario ya está en uso");
        }
        if (adminMapper.findByEmail(request.email()) != null) {
            throw new ResourceConflictException("El correo electrónico ya está en uso");
        }

        Admin admin = new Admin();
        admin.setId(UUID.randomUUID());
        admin.setUsername(request.username());
        admin.setEmail(request.email());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setActive(true);
        admin.setCreatedAt(Instant.now());

        adminMapper.insert(admin);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        Admin admin = adminMapper.findByUsername(username);

        if (admin == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(request.currentPassword(), admin.getPassword())) {
            throw new ResourceConflictException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(request.newPassword(), admin.getPassword())) {
            throw new ResourceConflictException("La nueva contraseña debe ser diferente a la actual");
        }

        String encodedPassword = passwordEncoder.encode(request.newPassword());

        int updated = adminMapper.updatePassword(username, encodedPassword);

        if (updated == 0) {
            throw new ResourceNotFoundException("No se pudo actualizar la contraseña");
        }
    }
}
