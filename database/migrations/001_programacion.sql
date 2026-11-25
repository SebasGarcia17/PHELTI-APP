BEGIN;
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
COMMIT;
