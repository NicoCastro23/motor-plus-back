-- =========================================
--  MotorPlus - Modelo Mejorado (PostgreSQL)
-- =========================================

-- Enums (catálogos)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_orden') THEN
CREATE TYPE estado_orden AS ENUM ('pendiente','en_proceso','cerrada','anulada');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_factura') THEN
CREATE TYPE estado_factura AS ENUM ('pendiente','pagada','anulada');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'metodo_pago') THEN
CREATE TYPE metodo_pago AS ENUM ('efectivo','transferencia','tarjeta','cheque');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_mov') THEN
CREATE TYPE tipo_mov AS ENUM ('ingreso','salida','ajuste');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'motivo_mov') THEN
CREATE TYPE motivo_mov AS ENUM ('compra','consumo','devolucion','ajuste');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_linea_fact') THEN
CREATE TYPE tipo_linea_fact AS ENUM ('servicio','repuesto');
END IF;
END$$;

-- ==============
-- ENTIDADES
-- ==============

CREATE TABLE IF NOT EXISTS cliente (
                                       cliente_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       nombre       VARCHAR(120) NOT NULL,
    correo       VARCHAR(120),
    telefono     VARCHAR(40)
    );

CREATE TABLE IF NOT EXISTS vehiculo (
                                        placa        VARCHAR(15) PRIMARY KEY,
    marca        VARCHAR(80)  NOT NULL,
    modelo       VARCHAR(80)  NOT NULL,
    anio         INT,
    tipo         VARCHAR(60),
    cliente_id   INT NOT NULL REFERENCES cliente(cliente_id) ON DELETE RESTRICT
    );
-- Índices vehiculo
CREATE INDEX IF NOT EXISTS idx_vehiculo_cliente ON vehiculo(cliente_id);
-- Check del año
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'chk_vehiculo_anio_rango'
  ) THEN
ALTER TABLE vehiculo
    ADD CONSTRAINT chk_vehiculo_anio_rango
        CHECK (anio BETWEEN 1950 AND EXTRACT(YEAR FROM now())::INT + 1);
END IF;
END$$;

CREATE TABLE IF NOT EXISTS mecanico (
                                        mecanico_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        nombre         VARCHAR(120) NOT NULL,
    especializacion VARCHAR(120)
    );

-- Catálogo de servicios
CREATE TABLE IF NOT EXISTS servicio (
                                        servicio_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        tipo_servicio VARCHAR(120) NOT NULL,
    descripcion   TEXT,
    precio_base   NUMERIC(12,2),
    activo        BOOLEAN DEFAULT TRUE
    );

-- Orden (work order)
CREATE TABLE IF NOT EXISTS orden (
                                     orden_id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     vehiculo_placa     VARCHAR(15) NOT NULL REFERENCES vehiculo(placa) ON DELETE RESTRICT,
    fecha_ingreso      TIMESTAMP   NOT NULL,
    diagnostico_inicial TEXT,
    estado             estado_orden NOT NULL DEFAULT 'pendiente'
    );
CREATE INDEX IF NOT EXISTS idx_orden_placa   ON orden(vehiculo_placa);
CREATE INDEX IF NOT EXISTS idx_orden_estado  ON orden(estado);
CREATE INDEX IF NOT EXISTS idx_orden_fecha   ON orden(fecha_ingreso);

-- Ejecución de servicios dentro de una orden
CREATE TABLE IF NOT EXISTS orden_servicio (
                                              orden_servicio_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              orden_id    INT NOT NULL REFERENCES orden(orden_id) ON DELETE CASCADE,
    servicio_id INT NOT NULL REFERENCES servicio(servicio_id) ON DELETE RESTRICT,
    estado      estado_orden NOT NULL DEFAULT 'pendiente',
    inicio_ts   TIMESTAMP,
    fin_ts      TIMESTAMP,
    precio_unit NUMERIC(12,2),
    cantidad    INT NOT NULL DEFAULT 1
    );
CREATE INDEX IF NOT EXISTS idx_ordsrv_orden     ON orden_servicio(orden_id);
CREATE INDEX IF NOT EXISTS idx_ordsrv_servicio  ON orden_servicio(servicio_id);
CREATE INDEX IF NOT EXISTS idx_ordsrv_estado    ON orden_servicio(estado);

-- Asignación de mecánicos por ítem (compuesta)
CREATE TABLE IF NOT EXISTS asignacion_mecanico (
                                                   orden_servicio_id INT NOT NULL REFERENCES orden_servicio(orden_servicio_id) ON DELETE CASCADE,
    mecanico_id       INT NOT NULL REFERENCES mecanico(mecanico_id) ON DELETE RESTRICT,
    rol               VARCHAR(40),
    horas             DECIMAL(6,2),
    observacion       TEXT,
    PRIMARY KEY (orden_servicio_id, mecanico_id)
    );
CREATE INDEX IF NOT EXISTS idx_asig_mec_id ON asignacion_mecanico(mecanico_id);

-- Supervisión entre mecánicos (compuesta; orden_id opcional)
CREATE TABLE IF NOT EXISTS mecanico_supervision (
                                                    supervisor_id  INT NOT NULL REFERENCES mecanico(mecanico_id) ON DELETE RESTRICT,
    supervisado_id INT NOT NULL REFERENCES mecanico(mecanico_id) ON DELETE RESTRICT,
    orden_id       INT REFERENCES orden(orden_id) ON DELETE CASCADE,
    registro       TEXT,
    ts             TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (supervisor_id, supervisado_id, orden_id)
    );
CREATE INDEX IF NOT EXISTS idx_mecsup_orden ON mecanico_supervision(orden_id);

-- Inventario / Proveedores / Repuestos
CREATE TABLE IF NOT EXISTS proveedor (
                                         proveedor_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         nombre       VARCHAR(140) NOT NULL,
    contacto     VARCHAR(140),
    telefono     VARCHAR(40),
    correo       VARCHAR(120)
    );
CREATE UNIQUE INDEX IF NOT EXISTS uq_proveedor_nombre ON proveedor(nombre);

CREATE TABLE IF NOT EXISTS repuesto (
                                        repuesto_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        nombre         VARCHAR(120) NOT NULL,
    costo_unitario NUMERIC(12,2) NOT NULL DEFAULT 0,
    stock          INT NOT NULL DEFAULT 0,
    activo         BOOLEAN DEFAULT TRUE
    );
CREATE INDEX IF NOT EXISTS idx_repuesto_nombre ON repuesto(nombre);

-- Relación proveedor <-> repuesto (compuesta)
CREATE TABLE IF NOT EXISTS proveedor_repuesto (
                                                  proveedor_id  INT NOT NULL REFERENCES proveedor(proveedor_id) ON DELETE CASCADE,
    repuesto_id   INT NOT NULL REFERENCES repuesto(repuesto_id)  ON DELETE RESTRICT,
    precio_compra NUMERIC(12,2),
    plazo_dias    INT,
    PRIMARY KEY (proveedor_id, repuesto_id)
    );

-- Consumo de repuestos por ítem de servicio (compuesta)
CREATE TABLE IF NOT EXISTS consumo_repuesto (
                                                orden_servicio_id INT NOT NULL REFERENCES orden_servicio(orden_servicio_id) ON DELETE CASCADE,
    repuesto_id       INT NOT NULL REFERENCES repuesto(repuesto_id) ON DELETE RESTRICT,
    cantidad          INT NOT NULL DEFAULT 1,
    precio_unit       NUMERIC(12,2),
    PRIMARY KEY (orden_servicio_id, repuesto_id)
    );
CREATE INDEX IF NOT EXISTS idx_conrep_repuesto ON consumo_repuesto(repuesto_id);

-- Kardex / Movimiento de inventario
CREATE TABLE IF NOT EXISTS mov_inventario (
                                              mov_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              repuesto_id INT NOT NULL REFERENCES repuesto(repuesto_id) ON DELETE RESTRICT,
    tipo        tipo_mov NOT NULL,
    motivo      motivo_mov,
    cantidad    INT NOT NULL,
    ref_doc     VARCHAR(40),
    ts          TIMESTAMP NOT NULL DEFAULT now()
    );
CREATE INDEX IF NOT EXISTS idx_movinv_repuesto ON mov_inventario(repuesto_id);
CREATE INDEX IF NOT EXISTS idx_movinv_ts       ON mov_inventario(ts);

-- Facturación
CREATE TABLE IF NOT EXISTS factura (
                                       factura_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       orden_id       INT NOT NULL UNIQUE REFERENCES orden(orden_id) ON DELETE RESTRICT,
    fecha_emision  DATE NOT NULL DEFAULT CURRENT_DATE,
    estado         estado_factura NOT NULL DEFAULT 'pendiente',
    mano_obra      NUMERIC(12,2),
    impuestos      NUMERIC(12,2) DEFAULT 0,
    total          NUMERIC(12,2) DEFAULT 0
    );
CREATE INDEX IF NOT EXISTS idx_fact_estado ON factura(estado);
CREATE INDEX IF NOT EXISTS idx_fact_fecha  ON factura(fecha_emision);

-- Líneas de factura (compuesta)
CREATE TABLE IF NOT EXISTS factura_linea (
                                             factura_id  INT NOT NULL REFERENCES factura(factura_id) ON DELETE CASCADE,
    tipo        tipo_linea_fact NOT NULL,  -- servicio|repuesto
    ref_id      INT NOT NULL,              -- orden_servicio_id o repuesto_id (no FK estricta por ser polimórfica)
    descripcion VARCHAR(200),
    cantidad    INT NOT NULL DEFAULT 1,
    precio_unit NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal    NUMERIC(12,2),
    PRIMARY KEY (factura_id, tipo, ref_id)
    );
CREATE INDEX IF NOT EXISTS idx_factline_factura ON factura_linea(factura_id);

-- Pagos
CREATE TABLE IF NOT EXISTS pago (
                                    pago_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    factura_id  INT NOT NULL REFERENCES factura(factura_id) ON DELETE CASCADE,
    fecha       DATE NOT NULL DEFAULT CURRENT_DATE,
    metodo      metodo_pago NOT NULL,
    monto       NUMERIC(12,2) NOT NULL
    );
