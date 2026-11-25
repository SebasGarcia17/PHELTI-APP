CREATE TABLE usuario (
    id_usuario INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT,
    promedio_calificacion NUMERIC(3,2) DEFAULT 0,
    ubicacion VARCHAR(150),
    modalidad_preferida VARCHAR(20)
);

CREATE TABLE categoria (
    id_categoria INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE publicacion (
    id_publicacion INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    id_categoria INTEGER NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    modalidad VARCHAR(20) NOT NULL,
    valor_tiempo NUMERIC(10,2) NOT NULL,
    negociable BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    duracion INTEGER,

    CONSTRAINT ck_publicacion_duracion
        CHECK (duracion IS NULL OR duracion > 0),

    CONSTRAINT fk_publicacion_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario),

    CONSTRAINT fk_publicacion_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categoria(id_categoria)
);

CREATE TABLE propuesta (
    id_propuesta INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_publicacion INTEGER NOT NULL,
    id_usuario_proponente INTEGER NOT NULL,
    mensaje TEXT,
    monto_tiempo NUMERIC(10,2),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_propuesta_publicacion
        FOREIGN KEY (id_publicacion)
        REFERENCES publicacion(id_publicacion)
        ON DELETE CASCADE,

    CONSTRAINT fk_propuesta_usuario
        FOREIGN KEY (id_usuario_proponente)
        REFERENCES usuario(id_usuario)
);

CREATE TABLE billetera (
    id_billetera INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario INTEGER NOT NULL UNIQUE,
    saldo_disponible NUMERIC(10,2) NOT NULL DEFAULT 0,
    saldo_reservado NUMERIC(10,2) NOT NULL DEFAULT 0,

    CONSTRAINT ck_billetera_saldos
        CHECK (saldo_disponible >= 0 AND saldo_reservado >= 0),

    CONSTRAINT fk_billetera_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
);

CREATE TABLE actividad (
    id_actividad INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_publicacion INTEGER NOT NULL,
    id_propuesta INTEGER NOT NULL UNIQUE,
    id_solicitante INTEGER NOT NULL,
    id_participante INTEGER NOT NULL,
    modalidad VARCHAR(20) NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    duracion INTEGER NOT NULL,
    lugar VARCHAR(200),
    enlace_virtual VARCHAR(300),
    valor_acordado NUMERIC(10,2) NOT NULL,
    codigo_checkin VARCHAR(100),
    estado VARCHAR(30) NOT NULL,

    CONSTRAINT fk_actividad_publicacion
        FOREIGN KEY (id_publicacion)
        REFERENCES publicacion(id_publicacion),

    CONSTRAINT fk_actividad_propuesta
        FOREIGN KEY (id_propuesta)
        REFERENCES propuesta(id_propuesta),

    CONSTRAINT fk_actividad_solicitante
        FOREIGN KEY (id_solicitante)
        REFERENCES usuario(id_usuario),

    CONSTRAINT fk_actividad_participante
        FOREIGN KEY (id_participante)
        REFERENCES usuario(id_usuario)
);

CREATE TABLE habilidad (
    id_habilidad INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE usuario_habilidad (
    id_usuario INTEGER NOT NULL,
    id_habilidad INTEGER NOT NULL,

    PRIMARY KEY (id_usuario, id_habilidad),

    CONSTRAINT fk_usuario_habilidad_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_habilidad_habilidad
        FOREIGN KEY (id_habilidad)
        REFERENCES habilidad(id_habilidad)
        ON DELETE RESTRICT
);

CREATE TABLE interes (
    id_interes INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE usuario_interes (
    id_usuario INTEGER NOT NULL,
    id_interes INTEGER NOT NULL,

    PRIMARY KEY (id_usuario, id_interes),

    CONSTRAINT fk_usuario_interes_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_interes_interes
        FOREIGN KEY (id_interes)
        REFERENCES interes(id_interes)
        ON DELETE RESTRICT
);

CREATE TABLE disponibilidad (
    id_disponibilidad INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    dia_semana VARCHAR(10) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,

    CONSTRAINT ck_disponibilidad_horas CHECK (hora_fin > hora_inicio),

    CONSTRAINT fk_disponibilidad_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);

CREATE TABLE transaccion (
    id_transaccion INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_billetera INTEGER NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    cantidad NUMERIC(10,2) NOT NULL,
    descripcion VARCHAR(300),
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    id_actividad INTEGER,
    id_usuario_origen INTEGER,
    id_usuario_destino INTEGER,

    CONSTRAINT ck_transaccion_cantidad CHECK (cantidad > 0),

    CONSTRAINT fk_transaccion_billetera
        FOREIGN KEY (id_billetera)
        REFERENCES billetera(id_billetera),

    CONSTRAINT fk_transaccion_actividad
        FOREIGN KEY (id_actividad)
        REFERENCES actividad(id_actividad),

    CONSTRAINT fk_transaccion_usuario_origen
        FOREIGN KEY (id_usuario_origen)
        REFERENCES usuario(id_usuario),

    CONSTRAINT fk_transaccion_usuario_destino
        FOREIGN KEY (id_usuario_destino)
        REFERENCES usuario(id_usuario)
);

CREATE TABLE confirmacion (
    id_confirmacion INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_actividad INTEGER NOT NULL,
    id_usuario INTEGER NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT uq_confirmacion_actividad_usuario
        UNIQUE (id_actividad, id_usuario),

    CONSTRAINT fk_confirmacion_actividad
        FOREIGN KEY (id_actividad)
        REFERENCES actividad(id_actividad),

    CONSTRAINT fk_confirmacion_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
);

CREATE TABLE calificacion (
    id_calificacion INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_actividad INTEGER NOT NULL,
    id_calificador INTEGER NOT NULL,
    id_calificado INTEGER NOT NULL,
    puntuacion INTEGER NOT NULL,
    comentario VARCHAR(500),
    fecha TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT ck_calificacion_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),

    CONSTRAINT uq_calificacion_actividad_calificador
        UNIQUE (id_actividad, id_calificador),

    CONSTRAINT fk_calificacion_actividad
        FOREIGN KEY (id_actividad)
        REFERENCES actividad(id_actividad),

    CONSTRAINT fk_calificacion_calificador
        FOREIGN KEY (id_calificador)
        REFERENCES usuario(id_usuario),

    CONSTRAINT fk_calificacion_calificado
        FOREIGN KEY (id_calificado)
        REFERENCES usuario(id_usuario)
);

INSERT INTO categoria (nombre)
SELECT v.nombre FROM (VALUES ('Tecnología'), ('Educación'), ('Finanzas'), ('Idiomas'), ('Arte y diseño'), ('Deportes'), ('Gastronomía'), ('Música'), ('Hogar'), ('Desarrollo personal')) AS v(nombre)
WHERE NOT EXISTS (SELECT 1 FROM categoria t WHERE lower(t.nombre) = lower(v.nombre))
ON CONFLICT (nombre) DO NOTHING;
INSERT INTO habilidad (nombre)
SELECT v.nombre FROM (VALUES ('Programación en Java'), ('Diseño gráfico'), ('Hojas de cálculo'), ('Inglés'), ('Matemáticas'), ('Fotografía'), ('Cocina'), ('Guitarra'), ('Entrenamiento deportivo'), ('Edición de video')) AS v(nombre)
WHERE NOT EXISTS (SELECT 1 FROM habilidad t WHERE lower(t.nombre) = lower(v.nombre))
ON CONFLICT (nombre) DO NOTHING;
INSERT INTO interes (nombre)
SELECT v.nombre FROM (VALUES ('Programación'), ('Diseño'), ('Finanzas'), ('Idiomas'), ('Ciencia'), ('Fotografía'), ('Gastronomía'), ('Música'), ('Deportes'), ('Desarrollo personal')) AS v(nombre)
WHERE NOT EXISTS (SELECT 1 FROM interes t WHERE lower(t.nombre) = lower(v.nombre))
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS programacion_propuesta (
    id_programacion INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_propuesta INTEGER NOT NULL REFERENCES propuesta(id_propuesta),
    id_solicitante INTEGER NOT NULL REFERENCES usuario(id_usuario),
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    duracion INTEGER NOT NULL CHECK (duracion > 0),
    lugar VARCHAR(200),
    enlace_virtual VARCHAR(300),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDING','ACCEPTED','REJECTED','WITHDRAWN')),
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_decision TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_programacion_pendiente ON programacion_propuesta(id_propuesta) WHERE estado = 'PENDING';
