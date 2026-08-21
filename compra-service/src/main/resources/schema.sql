-- Esquema de cache/historico para compra-service.
-- compra-service es un proxy sobre las APIs de Mercado Publico (Licitacion,
-- Compra Agil, Adjuntos); estas tablas guardan una copia local de lo que se
-- va consultando, para poder servir listados sin pegarle siempre a la API
-- externa y para conservar historico aunque la API deje de exponerlo.
--
-- ddl-auto esta en "none" (ver application.yml) a proposito: el esquema se
-- controla a mano con este script. Se ejecuta solo con
-- spring.sql.init.mode=always y usa CREATE TABLE IF NOT EXISTS para poder
-- correr en cada arranque sin romper si ya existe.

CREATE TABLE IF NOT EXISTS licitaciones (
    codigo_externo                      VARCHAR(50) PRIMARY KEY,
    nombre                              TEXT,
    codigo_estado                       INTEGER,
    estado                              VARCHAR(100),
    descripcion                         TEXT,
    moneda                              VARCHAR(10),
    monto_estimado                      NUMERIC(18, 2),
    tipo                                VARCHAR(100),
    dias_cierre_licitacion              VARCHAR(20),
    modalidad                           INTEGER,
    tipo_pago                           VARCHAR(20),
    tiempo                              VARCHAR(20),
    unidad_tiempo                       VARCHAR(20),
    tiempo_duracion_contrato            VARCHAR(20),
    unidad_tiempo_duracion_contrato     INTEGER,
    es_renovable                        INTEGER,
    fuente_financiamiento               TEXT,
    nombre_responsable_pago             TEXT,
    email_responsable_pago              TEXT,
    nombre_responsable_contrato         TEXT,
    email_responsable_contrato          TEXT,
    fono_responsable_contrato           VARCHAR(50),
    -- Comprador
    codigo_organismo                    VARCHAR(50),
    nombre_organismo                    TEXT,
    rut_unidad                          VARCHAR(20),
    codigo_unidad                       VARCHAR(50),
    nombre_unidad                       TEXT,
    direccion_unidad                    TEXT,
    comuna_unidad                       VARCHAR(100),
    region_unidad                       VARCHAR(100),
    rut_usuario                         VARCHAR(20),
    codigo_usuario                      VARCHAR(50),
    nombre_usuario                      TEXT,
    cargo_usuario                       TEXT,
    -- Fechas
    fecha_creacion                      TIMESTAMP,
    fecha_publicacion                   TIMESTAMP,
    fecha_cierre                        TIMESTAMP,
    fecha_inicio                        TIMESTAMP,
    fecha_final                         TIMESTAMP,
    fecha_pub_respuestas                TIMESTAMP,
    fecha_acto_apertura_tecnica         TIMESTAMP,
    fecha_acto_apertura_economica       TIMESTAMP,
    fecha_visita_terreno                TIMESTAMP,
    fecha_entrega_antecedentes          TIMESTAMP,
    fecha_estimada_adjudicacion         TIMESTAMP,
    fecha_adjudicacion                  TIMESTAMP,
    fecha_estimada_firma                TIMESTAMP,
    fecha_soporte_fisico                TIMESTAMP,
    fecha_sync                          TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_licitaciones_fecha_publicacion ON licitaciones (fecha_publicacion);

-- CREATE TABLE IF NOT EXISTS no toca una tabla que ya existe, asi que las
-- columnas agregadas despues del deploy inicial de "licitaciones" se
-- suman a mano aca para que las instalaciones existentes tambien las reciban.
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS dias_cierre_licitacion VARCHAR(20);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS modalidad INTEGER;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS tipo_pago VARCHAR(20);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS tiempo VARCHAR(20);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS unidad_tiempo VARCHAR(20);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS tiempo_duracion_contrato VARCHAR(20);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS unidad_tiempo_duracion_contrato INTEGER;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS es_renovable INTEGER;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fuente_financiamiento TEXT;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS nombre_responsable_pago TEXT;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS email_responsable_pago TEXT;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS nombre_responsable_contrato TEXT;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS email_responsable_contrato TEXT;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fono_responsable_contrato VARCHAR(50);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS rut_unidad VARCHAR(20);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS codigo_unidad VARCHAR(50);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS direccion_unidad TEXT;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS rut_usuario VARCHAR(20);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS codigo_usuario VARCHAR(50);
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS nombre_usuario TEXT;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS cargo_usuario TEXT;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_inicio TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_final TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_pub_respuestas TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_acto_apertura_tecnica TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_acto_apertura_economica TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_visita_terreno TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_entrega_antecedentes TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_estimada_adjudicacion TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_estimada_firma TIMESTAMP;
ALTER TABLE licitaciones ADD COLUMN IF NOT EXISTS fecha_soporte_fisico TIMESTAMP;

-- Items (productos/servicios solicitados) del detalle de una licitacion
-- (Listado[].Items.Listado). Solo llega en el detalle por codigo; se
-- resincroniza entero en cada guardado (ver LicitacionMapper).
CREATE TABLE IF NOT EXISTS licitacion_items (
    id                          BIGSERIAL PRIMARY KEY,
    licitacion_codigo_externo   VARCHAR(50) NOT NULL REFERENCES licitaciones (codigo_externo) ON DELETE CASCADE,
    correlativo                 INTEGER,
    codigo_producto              BIGINT,
    codigo_categoria             VARCHAR(50),
    categoria                    TEXT,
    nombre_producto               TEXT,
    descripcion                  TEXT,
    unidad_medida                 VARCHAR(50),
    cantidad                     NUMERIC(18, 4)
);
CREATE INDEX IF NOT EXISTS idx_licitacion_items_codigo ON licitacion_items (licitacion_codigo_externo);

CREATE TABLE IF NOT EXISTS compras_agiles (
    codigo                          VARCHAR(50) PRIMARY KEY,
    nombre                          TEXT,
    descripcion                     TEXT,
    id_estado                       INTEGER,
    estado_codigo                   VARCHAR(50),
    estado_glosa                    VARCHAR(200),
    convocatoria_estado             INTEGER,
    convocatoria_descripcion        TEXT,
    fecha_publicacion               TIMESTAMP,
    fecha_cierre                    TIMESTAMP,
    fecha_ultimo_cambio             TIMESTAMP,
    fecha_cancelacion               TIMESTAMP,
    fecha_cierre_primer_llamado     TIMESTAMP,
    fecha_cierre_segundo_llamado    TIMESTAMP,
    direccion_entrega               TEXT,
    plazo_entrega_dias              INTEGER,
    tipo_presupuesto                VARCHAR(50),
    moneda                          VARCHAR(10),
    presupuesto_estimado            NUMERIC(18, 2),
    monto_disponible                NUMERIC(18, 2),
    monto_disponible_clp            NUMERIC(18, 2),
    valor_cambio_moneda             NUMERIC(18, 4),
    fecha_cambio_moneda             VARCHAR(50),
    id_orden_compra                 BIGINT,
    organismo_comprador             TEXT,
    rut_institucion                 VARCHAR(20),
    unidad_compra                   TEXT,
    region                          INTEGER,
    nombre_region                   VARCHAR(100),
    multa_sancion                   NUMERIC(18, 2),
    total_ofertas_recibidas         INTEGER,
    total_demandas                  INTEGER,
    motivo_cancelacion              TEXT,
    motivo_desierta                 TEXT,
    motivo_seleccion                TEXT,
    considera_req_medioambientales  BOOLEAN,
    considera_req_impacto_social    BOOLEAN,
    -- true solo cuando la fila viene de GET /compra-agil/{codigo} (Detalle);
    -- el listado (Item) no trae productos_solicitados/proveedores_cotizando/etc,
    -- asi que una fila cacheada solo desde el listado no debe servirse como si
    -- fuera el detalle completo.
    detalle_completo                BOOLEAN NOT NULL DEFAULT false,
    fecha_sync                      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_compras_agiles_fecha_publicacion ON compras_agiles (fecha_publicacion);

CREATE TABLE IF NOT EXISTS compra_agil_productos_solicitados (
    id                  BIGSERIAL PRIMARY KEY,
    compra_agil_codigo  VARCHAR(50) NOT NULL REFERENCES compras_agiles (codigo) ON DELETE CASCADE,
    codigo_producto     VARCHAR(100),
    nombre              TEXT,
    descripcion         TEXT,
    cantidad            NUMERIC(18, 4),
    unidad_medida       VARCHAR(50)
);
CREATE INDEX IF NOT EXISTS idx_ca_prod_solicitados_codigo ON compra_agil_productos_solicitados (compra_agil_codigo);

CREATE TABLE IF NOT EXISTS compra_agil_proveedores_cotizando (
    id_cotizacion               BIGINT PRIMARY KEY,
    compra_agil_codigo          VARCHAR(50) NOT NULL REFERENCES compras_agiles (codigo) ON DELETE CASCADE,
    codigo_empresa               VARCHAR(50),
    codigo_sucursal_empresa      VARCHAR(50),
    es_emt                       INTEGER,
    razon_social                 TEXT,
    rut_proveedor                VARCHAR(20),
    descripcion                  TEXT,
    fecha_vigencia               VARCHAR(50),
    fecha_creacion                VARCHAR(50),
    valor_neto                   NUMERIC(18, 2),
    total_impuesto                NUMERIC(18, 2),
    monto_despacho                NUMERIC(18, 2),
    monto_total                   NUMERIC(18, 2),
    proveedor_seleccionado        INTEGER,
    descripcion_cotizacion        TEXT,
    estado                        INTEGER,
    justificacion_inadmisibilidad TEXT,
    estado_por_comprador          INTEGER,
    activo                         INTEGER,
    id_oc                         BIGINT,
    nombre_impuesto                VARCHAR(100),
    porcentaje_impuesto            INTEGER
);
CREATE INDEX IF NOT EXISTS idx_ca_proveedores_codigo ON compra_agil_proveedores_cotizando (compra_agil_codigo);

CREATE TABLE IF NOT EXISTS compra_agil_productos_cotizados (
    id                     BIGSERIAL PRIMARY KEY,
    id_cotizacion          BIGINT NOT NULL REFERENCES compra_agil_proveedores_cotizando (id_cotizacion) ON DELETE CASCADE,
    codigo_producto        VARCHAR(100),
    nombre_producto        TEXT,
    descripcion            TEXT,
    cantidad               NUMERIC(18, 4),
    precio_unitario        NUMERIC(18, 2),
    monto_total_producto   NUMERIC(18, 2)
);
CREATE INDEX IF NOT EXISTS idx_ca_prod_cotizados_cotizacion ON compra_agil_productos_cotizados (id_cotizacion);

CREATE TABLE IF NOT EXISTS compra_agil_documentos (
    id                  BIGSERIAL PRIMARY KEY,
    compra_agil_codigo  VARCHAR(50) NOT NULL REFERENCES compras_agiles (codigo) ON DELETE CASCADE,
    id_externo          VARCHAR(100),
    nombre              TEXT
);
CREATE INDEX IF NOT EXISTS idx_ca_documentos_codigo ON compra_agil_documentos (compra_agil_codigo);

-- Sin FK dura a compras_agiles: los adjuntos se listan por codigo bajo demanda
-- (AdjuntoController) y pueden consultarse antes de que ese codigo tenga fila
-- propia en compras_agiles. compra_agil_codigo queda como referencia logica.
CREATE TABLE IF NOT EXISTS adjuntos (
    id                  VARCHAR(100) PRIMARY KEY,
    compra_agil_codigo  VARCHAR(50) NOT NULL,
    nombre_archivo      TEXT,
    fecha_sync          TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_adjuntos_compra_agil_codigo ON adjuntos (compra_agil_codigo);

-- Adjuntos de licitaciones normales (LS/LP/LE), bajados por
-- LicitacionAttachmentPythonScraper (script Playwright) y orquestados por
-- LicitacionSyncScheduler. A diferencia de "adjuntos" (Compra Agil, que
-- proxea el binario desde la API externa en cada descarga), acá el binario
-- se guarda completo en "contenido": no hay copia en disco, la BD es la
-- unica fuente de verdad.
CREATE TABLE IF NOT EXISTS adjunto_licitacion (
    id                  BIGSERIAL PRIMARY KEY,
    codigo_licitacion   VARCHAR(50) NOT NULL REFERENCES licitaciones (codigo_externo) ON DELETE CASCADE,
    nombre_archivo      TEXT NOT NULL,
    tipo_contenido      VARCHAR(150),
    tamano_bytes        INTEGER,
    contenido           BYTEA NOT NULL,
    fecha_sync          TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_adjunto_licitacion_codigo ON adjunto_licitacion (codigo_licitacion);
