-- Clients and vehicles -----------------------------------------------------
CREATE TABLE IF NOT EXISTS clients (
                                       id          UUID PRIMARY KEY,
                                       first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    phone       VARCHAR(50),
    created_at  TIMESTAMPTZ NOT NULL
    );

CREATE TABLE IF NOT EXISTS vehicles (
                                        id            UUID PRIMARY KEY,
                                        client_id     UUID REFERENCES clients(id) ON DELETE SET NULL,
    brand         VARCHAR(100) NOT NULL,
    model         VARCHAR(100) NOT NULL,
    license_plate VARCHAR(50) NOT NULL,
    model_year    INTEGER,
    created_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_vehicles_license UNIQUE (license_plate)
    );

-- Mechanics and catalog ----------------------------------------------------
CREATE TABLE IF NOT EXISTS mechanics (
                                         id             UUID PRIMARY KEY,
                                         first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    specialization VARCHAR(150),
    phone          VARCHAR(50),
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL
    );

CREATE TABLE IF NOT EXISTS services_catalog (
                                                id          UUID PRIMARY KEY,
                                                name        VARCHAR(150) NOT NULL,
    description TEXT,
    price       NUMERIC(12, 2) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL
    );

-- Suppliers and parts ------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
                                         id         UUID PRIMARY KEY,
                                         name       VARCHAR(150) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    phone      VARCHAR(50),
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_suppliers_email UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS parts (
                                     id          UUID PRIMARY KEY,
                                     name        VARCHAR(150) NOT NULL,
    sku         VARCHAR(100) NOT NULL,
    description TEXT,
    unit_price  NUMERIC(12, 2) NOT NULL,
    stock       INTEGER NOT NULL DEFAULT 0,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_parts_sku UNIQUE (sku)
    );

CREATE TABLE IF NOT EXISTS supplier_parts (
                                              supplier_id  UUID NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
    part_id      UUID NOT NULL REFERENCES parts(id) ON DELETE CASCADE,
    price        NUMERIC(12, 2) NOT NULL,
    min_quantity INTEGER,
    PRIMARY KEY (supplier_id, part_id)
    );

-- Orders -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
                                      id            UUID PRIMARY KEY,
                                      client_id     UUID REFERENCES clients(id) ON DELETE SET NULL,
    license_plate VARCHAR(50) NOT NULL,
    status        VARCHAR(32) NOT NULL CHECK (status IN ('DRAFT', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    description   TEXT,
    total         NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_orders_vehicle FOREIGN KEY (license_plate) REFERENCES vehicles(license_plate) ON UPDATE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_orders_client ON orders(client_id);
CREATE INDEX IF NOT EXISTS idx_orders_license_plate ON orders(license_plate);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

CREATE TABLE IF NOT EXISTS order_items (
                                           id          UUID PRIMARY KEY,
                                           order_id    UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    service_id  UUID REFERENCES services_catalog(id) ON DELETE SET NULL,
    description TEXT,
    quantity    INTEGER NOT NULL,
    unit_price  NUMERIC(12, 2) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);

CREATE TABLE IF NOT EXISTS order_item_parts (
                                                order_item_id UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    part_id       UUID NOT NULL REFERENCES parts(id) ON DELETE RESTRICT,
    quantity      INTEGER NOT NULL,
    unit_price    NUMERIC(12, 2) NOT NULL,
    PRIMARY KEY (order_item_id, part_id)
    );

CREATE TABLE IF NOT EXISTS assignments (
                                           order_item_id   UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    mechanic_id     UUID NOT NULL REFERENCES mechanics(id) ON DELETE CASCADE,
    assigned_at     TIMESTAMPTZ NOT NULL,
    estimated_hours INTEGER,
    PRIMARY KEY (order_item_id, mechanic_id)
    );

CREATE TABLE IF NOT EXISTS supervisions (
                                            supervisor_id  UUID NOT NULL REFERENCES mechanics(id) ON DELETE CASCADE,
    supervisado_id UUID NOT NULL REFERENCES mechanics(id) ON DELETE CASCADE,
    order_id       UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    created_at     TIMESTAMPTZ NOT NULL,
    notes          TEXT,
    PRIMARY KEY (supervisor_id, supervisado_id, order_id)
    );

-- Inventory movements ------------------------------------------------------
CREATE TABLE IF NOT EXISTS part_movements (
                                              id           UUID PRIMARY KEY,
                                              part_id      UUID NOT NULL REFERENCES parts(id) ON DELETE CASCADE,
    type         VARCHAR(16) NOT NULL CHECK (type IN ('IN', 'OUT')),
    quantity     INTEGER NOT NULL,
    performed_at TIMESTAMPTZ NOT NULL,
    notes        TEXT
    );

CREATE INDEX IF NOT EXISTS idx_part_movements_part ON part_movements(part_id);
CREATE INDEX IF NOT EXISTS idx_part_movements_date ON part_movements(part_id, performed_at DESC);

-- Invoices -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
                                        id         UUID PRIMARY KEY,
                                        order_id   UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    number     VARCHAR(100) NOT NULL UNIQUE,
    status     VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'ISSUED', 'PAID', 'CANCELLED')),
    issue_date TIMESTAMPTZ NOT NULL,
    due_date   TIMESTAMPTZ,
    total      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    balance    NUMERIC(12, 2) NOT NULL DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_invoices_order ON invoices(order_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);

CREATE TABLE IF NOT EXISTS invoice_lines (
                                             invoice_id   UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    type         VARCHAR(16) NOT NULL CHECK (type IN ('SERVICE', 'PART', 'MANUAL')),
    reference_id UUID NOT NULL,
    description  TEXT NOT NULL,
    amount       NUMERIC(12, 2) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (invoice_id, type, reference_id)
    );

CREATE TABLE IF NOT EXISTS invoice_payments (
                                                id           UUID PRIMARY KEY,
                                                invoice_id   UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    amount       NUMERIC(12, 2) NOT NULL,
    method       VARCHAR(50) NOT NULL,
    payment_date TIMESTAMPTZ NOT NULL,
    reference    VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_invoice_payments_invoice ON invoice_payments(invoice_id);

-- Administrators ------------------------------------------------------------
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
    '$2a$10$Q2PLxAvYV5K675nV1mbdn.EhB43iDd0Ks3LxeUZGLpuquzu0p7yUC', -- admin123
    'admin@motorplus.com',
    true,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;