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
    estado VARCHAR(30) NOT NULL DEFAULT 'PUBLICADA',

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
        REFERENCES publicacion(id_publicacion),

    CONSTRAINT fk_propuesta_usuario
        FOREIGN KEY (id_usuario_proponente)
        REFERENCES usuario(id_usuario)
);
