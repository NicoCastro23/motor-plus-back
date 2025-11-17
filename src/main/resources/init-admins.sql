-- Script para inicializar la tabla de administradores
-- Ejecutar este script si la tabla no existe o necesita ser recreada

-- Crear tabla de administradores si no existe
CREATE TABLE IF NOT EXISTS admins (
    id         UUID PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insertar administrador por defecto (username: admin, password: admin123)
-- La contraseña es el hash BCrypt de "admin123"
INSERT INTO admins (id, username, password, email, active, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- admin123
    'admin@motorplus.com',
    true,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

