-- =======================================================
--  BASE DE DATOS: Panadería y Pastelería Taty's  
--  Sistema de Gestión de Materia Prima y Valoración
--  Proyecto Integrador - Base de Datos 411
--  Autores: Camila Rodríguez | Juan Manuel Daza
-- =======================================================

CREATE DATABASE IF NOT EXISTS panaderia_tatys
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_spanish_ci;

USE panaderia_tatys;


-- =======================================================
CREATE TABLE tercero (
    id_tercero  INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(150) NOT NULL,
    telefono    VARCHAR(20)           DEFAULT NULL,
    direccion   VARCHAR(255)          DEFAULT NULL,
    correo      VARCHAR(100)          DEFAULT NULL,
    tipo        ENUM('empleado','proveedor') NOT NULL
                COMMENT 'Discriminador del supertipo',
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_tercero PRIMARY KEY (id_tercero)
);


-- =======================================================
CREATE TABLE empleado (
    id_empleado INT          NOT NULL,
    cargo       VARCHAR(100) NOT NULL,
    usuario     VARCHAR(50)  NOT NULL,
    contrasena  VARCHAR(255) NOT NULL COMMENT 'Hash bcrypt',
    rol         ENUM('administrador','auxiliar_operativo') NOT NULL,
    CONSTRAINT pk_empleado   PRIMARY KEY (id_empleado),
    CONSTRAINT fk_emp_tercero FOREIGN KEY (id_empleado)
        REFERENCES tercero(id_tercero)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uq_emp_usuario UNIQUE (usuario)
);


-- =======================================================
CREATE TABLE proveedor (
    id_proveedor  INT          NOT NULL,
    nit           VARCHAR(20)           DEFAULT NULL,
    contacto      VARCHAR(100)          DEFAULT NULL,
    CONSTRAINT pk_proveedor    PRIMARY KEY (id_proveedor),
    CONSTRAINT fk_prov_tercero FOREIGN KEY (id_proveedor)
        REFERENCES tercero(id_tercero)
        ON UPDATE CASCADE ON DELETE CASCADE
);


-- =======================================================
CREATE TABLE tipo_documento (
    id_tipo_doc  INT          NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100) NOT NULL
                 COMMENT 'Ej: Orden de compra, Orden de producción, Devolución a proveedor',
    descripcion  TEXT                  DEFAULT NULL,
    afecta_stock ENUM('entrada','salida','ajuste') NOT NULL
                 COMMENT 'Indica cómo afecta el inventario',
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_tipo_doc    PRIMARY KEY (id_tipo_doc),
    CONSTRAINT uq_tipo_doc_nb UNIQUE (nombre)
);


-- =======================================================
CREATE TABLE documento (
    id_documento   INT          NOT NULL AUTO_INCREMENT,
    id_tipo_doc    INT          NOT NULL,
    id_tercero     INT          NOT NULL,
    numero_doc     VARCHAR(50)           DEFAULT NULL
                   COMMENT 'Número externo: factura, orden, remisión...',
    fecha          DATE         NOT NULL,
    observaciones  TEXT                  DEFAULT NULL,
    estado         ENUM('borrador','confirmado','anulado') NOT NULL DEFAULT 'borrador',
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_documento     PRIMARY KEY (id_documento),
    CONSTRAINT fk_doc_tipo      FOREIGN KEY (id_tipo_doc)
        REFERENCES tipo_documento(id_tipo_doc)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_doc_tercero   FOREIGN KEY (id_tercero)
        REFERENCES tercero(id_tercero)
        ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =======================================================
CREATE TABLE insumo (
    id_insumo           INT           NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(150)  NOT NULL,
    unidad_medida       VARCHAR(30)   NOT NULL COMMENT 'kg, litro, unidad, bulto...',
    cantidad_disponible DECIMAL(10,3) NOT NULL DEFAULT 0,
    stock_minimo        DECIMAL(10,3) NOT NULL DEFAULT 0
                        COMMENT 'Para alertas de escasez (R - Reporte)',
    precio_unitario     DECIMAL(10,2) NOT NULL DEFAULT 0.00
                        COMMENT 'Último precio de compra, base para costeo',
    estado              ENUM('disponible','agotado','vencido') NOT NULL DEFAULT 'disponible',
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_insumo       PRIMARY KEY (id_insumo),
    CONSTRAINT uq_insumo_nombre UNIQUE (nombre)
);

-- =======================================================
CREATE TABLE producto (
    id_producto  INT           NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100)  NOT NULL,
    descripcion  TEXT                   DEFAULT NULL,
    precio_venta DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo       BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_producto      PRIMARY KEY (id_producto),
    CONSTRAINT uq_producto_nombre UNIQUE (nombre)
);


-- =======================================================
CREATE TABLE receta (
    id_receta          INT           NOT NULL AUTO_INCREMENT,
    id_producto        INT           NOT NULL,
    id_insumo          INT           NOT NULL,
    cantidad_requerida DECIMAL(12,3) NOT NULL COMMENT 'Cantidad de insumo por lote unitario',
    CONSTRAINT pk_receta           PRIMARY KEY (id_receta),
    CONSTRAINT uq_receta_prod_ins  UNIQUE (id_producto, id_insumo),
    CONSTRAINT fk_rec_producto     FOREIGN KEY (id_producto)
        REFERENCES producto(id_producto)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_rec_insumo       FOREIGN KEY (id_insumo)
        REFERENCES insumo(id_insumo)
        ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =======================================================
CREATE TABLE lote_produccion (
    id_lote            INT           NOT NULL AUTO_INCREMENT,
    id_empleado        INT           NOT NULL,
    id_producto        INT           NOT NULL,
    fecha              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cantidad_producida DECIMAL(10,3) NOT NULL,
    costo_total        DECIMAL(10,2) NOT NULL DEFAULT 0.00
                       COMMENT 'Suma de (cantidad_requerida × precio_unitario) por insumo',
    precio_por_lote    DECIMAL(10,2) NOT NULL DEFAULT 0.00
                       COMMENT 'precio_venta × cantidad_producida',
    CONSTRAINT pk_lote          PRIMARY KEY (id_lote),
    CONSTRAINT fk_lote_empleado FOREIGN KEY (id_empleado)
        REFERENCES empleado(id_empleado)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_lote_producto FOREIGN KEY (id_producto)
        REFERENCES producto(id_producto)
        ON UPDATE CASCADE ON DELETE RESTRICT
);


CREATE TABLE movimiento (
    id_movimiento    INT           NOT NULL AUTO_INCREMENT,
    id_insumo        INT           NOT NULL,
    id_tercero       INT           NOT NULL,
    id_documento     INT           NOT NULL,
    id_lote          INT                    DEFAULT NULL
                     COMMENT 'Solo en salidas a producción',
    tipo             ENUM(
                       'entrada_compra',
                       'salida_produccion',
                       'ajuste_perdida',
                       'ajuste_hallazgo',
                       'devolucion_proveedor',
                       'entrada_sobrante_produccion',
                       'inventario_inicial'
                     ) NOT NULL,
    fecha            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cantidad         DECIMAL(10,3) NOT NULL COMMENT 'Siempre positivo',
    fecha_vencimiento DATE                  DEFAULT NULL,
    observacion      TEXT                   DEFAULT NULL,
    CONSTRAINT pk_movimiento       PRIMARY KEY (id_movimiento),
    CONSTRAINT fk_mov_insumo       FOREIGN KEY (id_insumo)
        REFERENCES insumo(id_insumo)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_mov_tercero      FOREIGN KEY (id_tercero)
        REFERENCES tercero(id_tercero)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_mov_documento    FOREIGN KEY (id_documento)
        REFERENCES documento(id_documento)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_mov_lote         FOREIGN KEY (id_lote)
        REFERENCES lote_produccion(id_lote)
        ON UPDATE CASCADE ON DELETE SET NULL
);


-- =======================================================
CREATE TABLE detalle_lote (
    id_detalle    INT           NOT NULL AUTO_INCREMENT,
    id_lote       INT           NOT NULL,
    id_insumo     INT           NOT NULL,
    cantidad_usada DECIMAL(10,3) NOT NULL,
    costo_parcial  DECIMAL(10,2) NOT NULL DEFAULT 0.00
                   COMMENT 'cantidad_usada × precio_unitario del insumo',
    CONSTRAINT pk_detalle_lote    PRIMARY KEY (id_detalle),
    CONSTRAINT uq_det_lote_ins    UNIQUE (id_lote, id_insumo),
    CONSTRAINT fk_det_lote        FOREIGN KEY (id_lote)
        REFERENCES lote_produccion(id_lote)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_det_ins         FOREIGN KEY (id_insumo)
        REFERENCES insumo(id_insumo)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =======================================================
CREATE TABLE reporte (
    id_reporte        INT       NOT NULL AUTO_INCREMENT,
    id_empleado       INT       NOT NULL,
    tipo              ENUM(
                        'inventario',
                        'produccion',
                        'costos',
                        'perdidas',
                        'escasez'
                      ) NOT NULL,
    fecha_generacion  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    contenido         LONGTEXT           DEFAULT NULL
                      COMMENT 'JSON o texto con los datos del reporte',
    CONSTRAINT pk_reporte        PRIMARY KEY (id_reporte),
    CONSTRAINT fk_rep_empleado   FOREIGN KEY (id_empleado)
        REFERENCES empleado(id_empleado)
        ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =======================================================
--  RESUMEN COMPLETO DE RELACIONES
-- =======================================================
--
--  [HERENCIA - Supertipo/Subtipo]
--  TERCERO (1:1) ←──────────── EMPLEADO
--  TERCERO (1:1) ←──────────── PROVEEDOR
--
--  [CLASIFICACIÓN]
--  TIPO_DOCUMENTO (1) ──────── (N) DOCUMENTO
--
--  [DOCUMENTOS Y TERCEROS]
--  TERCERO    (1) ──────────── (N) DOCUMENTO
--  DOCUMENTO  (1) ──────────── (N) MOVIMIENTO
--
--  [INVENTARIO]
--  INSUMO     (1) ──────────── (N) MOVIMIENTO
--  TERCERO    (1) ──────────── (N) MOVIMIENTO
--  LOTE       (1) ──────────── (N) MOVIMIENTO   (nullable)
--
--  [COMPOSICIÓN DE PRODUCTOS - Ajuste #1]
--  PRODUCTO   (1) ──────────── (N) RECETA
--  INSUMO     (1) ──────────── (N) RECETA
--
--  [PRODUCCIÓN]
--  EMPLEADO   (1) ──────────── (N) LOTE_PRODUCCION
--  PRODUCTO   (1) ──────────── (N) LOTE_PRODUCCION
--  LOTE       (1) ──────────── (N) DETALLE_LOTE
--  INSUMO     (1) ──────────── (N) DETALLE_LOTE
--
--  [REPORTES]
--  EMPLEADO   (1) ──────────── (N) REPORTE
--
-- =======================================================