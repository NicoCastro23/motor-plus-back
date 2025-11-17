package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.authDtos.ChangePasswordRequest;
import com.motorplus.motorplus.dto.authDtos.LoginRequest;
import com.motorplus.motorplus.dto.authDtos.LoginResponse;
import com.motorplus.motorplus.exceptions.ResourceConflictException;
import com.motorplus.motorplus.exceptions.ResourceNotFoundException;
import com.motorplus.motorplus.mapper.AdminMapper;
import com.motorplus.motorplus.model.Admin;
import com.motorplus.motorplus.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (!admin.isActive()) {
            throw new ResourceNotFoundException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new ResourceNotFoundException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(admin.getUsername());
        
        return new LoginResponse(token, admin.getUsername(), admin.getEmail());
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        Admin admin = adminMapper.findByUsername(username);
        
        if (admin == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        if (!admin.isActive()) {
            throw new ResourceNotFoundException("Usuario inactivo");
        }

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.currentPassword(), admin.getPassword())) {
            throw new ResourceConflictException("La contraseña actual es incorrecta");
        }

        // Verificar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(request.newPassword(), admin.getPassword())) {
            throw new ResourceConflictException("La nueva contraseña debe ser diferente a la actual");
        }

        // Encriptar la nueva contraseña
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        
        // Actualizar la contraseña
        int updated = adminMapper.updatePassword(username, encodedPassword);
        
        if (updated == 0) {
            throw new ResourceNotFoundException("No se pudo actualizar la contraseña");
        }
    }
}

