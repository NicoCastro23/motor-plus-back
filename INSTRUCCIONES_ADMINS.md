# Instrucciones para crear la tabla de administradores

El error "Invalid UUID string: 1" indica que la tabla `admins` no existe en la base de datos o tiene un formato incorrecto.

## Solución

Ejecuta el siguiente script SQL en tu base de datos PostgreSQL:

```sql
-- Crear tabla de administradores
CREATE TABLE IF NOT EXISTS admins (
    id         UUID PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insertar administrador por defecto
-- Usuario: admin
-- Contraseña: admin123
INSERT INTO admins (id, username, password, email, active, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'admin@motorplus.com',
    true,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;
```

## Ejecutar desde la línea de comandos

```bash
psql -h localhost -U postgres -d motorplus -f src/main/resources/init-admins.sql
```

O copia y pega el contenido de `src/main/resources/init-admins.sql` en tu cliente de PostgreSQL.

## Verificar que la tabla existe

```sql
SELECT * FROM admins WHERE username = 'admin';
```

Deberías ver una fila con el usuario 'admin'.

