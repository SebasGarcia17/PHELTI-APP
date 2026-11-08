CREATE TABLE usuario (
    id_usuario INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT,
    promedio_calificacion NUMERIC(3,2) DEFAULT 0
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

INSERT INTO categoria (nombre) VALUES
    ('Educacion'),
    ('Tecnologia'),
    ('Hogar'),
    ('Arte y musica'),
    ('Deportes'),
    ('Idiomas')
ON CONFLICT (nombre) DO NOTHING;
