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
    private final EmailService emailService;

    public AuthService(AdminMapper adminMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public LoginResponse login(LoginRequest request) {
        Admin admin = adminMapper.findByUsername(request.username());
        
        if (admin == null) {
            throw new ResourceNotFoundException("Credenciales inválidas");
        }

        if (!admin.isActive()) {
            throw new ResourceNotFoundException("Cuenta pendiente de verificación. Revisa tu correo electrónico.");
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

        String code = String.format("%06d", (int)(Math.random() * 1_000_000));

        Admin admin = new Admin();
        admin.setId(UUID.randomUUID());
        admin.setUsername(request.username());
        admin.setEmail(request.email());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setActive(false);
        admin.setCreatedAt(Instant.now());
        admin.setVerificationToken(code);

        adminMapper.insert(admin);
        emailService.sendVerificationEmail(request.email(), code);
    }

    public void verifyCode(String email, String code) {
        Admin admin = adminMapper.findByEmail(email);
        if (admin == null) {
            throw new ResourceNotFoundException("No se encontró una cuenta pendiente con ese correo");
        }
        if (admin.isActive()) {
            throw new ResourceConflictException("La cuenta ya está activa");
        }
        if (!code.equals(admin.getVerificationToken())) {
            throw new ResourceConflictException("Código de verificación incorrecto");
        }
        adminMapper.activateAdmin(admin.getId());
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

