-- Ejecutar una sola vez sobre una base existente, con ambos servidores detenidos.
BEGIN;
LOCK TABLE categoria, habilidad, interes, usuario_habilidad, usuario_interes, publicacion IN ACCESS EXCLUSIVE MODE;
ALTER TABLE usuario_habilidad DROP CONSTRAINT IF EXISTS fk_usuario_habilidad_habilidad;
ALTER TABLE usuario_habilidad ADD CONSTRAINT fk_usuario_habilidad_habilidad FOREIGN KEY (id_habilidad) REFERENCES habilidad(id_habilidad) ON DELETE RESTRICT;
ALTER TABLE usuario_interes DROP CONSTRAINT IF EXISTS fk_usuario_interes_interes;
ALTER TABLE usuario_interes ADD CONSTRAINT fk_usuario_interes_interes FOREIGN KEY (id_interes) REFERENCES interes(id_interes) ON DELETE RESTRICT;
-- Conservar los vínculos al normalizar el nombre del interés anterior.
DO $$
DECLARE old_id integer; new_id integer;
BEGIN
    SELECT id_interes INTO new_id FROM interes WHERE lower(nombre) = lower('Programación') ORDER BY id_interes LIMIT 1;
    FOR old_id IN SELECT id_interes FROM interes WHERE lower(nombre) = lower('Programación en Java') ORDER BY id_interes LOOP
        IF new_id IS NULL THEN
            UPDATE interes SET nombre = 'Programación' WHERE id_interes = old_id;
            new_id := old_id;
        ELSE
            INSERT INTO usuario_interes (id_usuario, id_interes)
            SELECT id_usuario, new_id FROM usuario_interes WHERE id_interes = old_id ON CONFLICT DO NOTHING;
            DELETE FROM usuario_interes WHERE id_interes = old_id;
            DELETE FROM interes WHERE id_interes = old_id;
        END IF;
    END LOOP;
END $$;
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
COMMIT;
