-- Datos ficticios para una base de sustentación, no para producción.
BEGIN;
INSERT INTO usuario (nombre,correo,descripcion,ubicacion,modalidad_preferida) VALUES
('Germin Jaramillo','germin.jaramillo.phelti.demo@gmail.com','Comparto conocimientos de tecnología y hojas de cálculo.','Medellín','VIRTUAL'),
('Sebastian Garcia','sebastian.garcia.phelti.demo@hotmail.com','Me interesa aprender herramientas digitales y ofrecer apoyo en matemáticas.','Medellín','VIRTUAL'),
('Juan Perez','juan.perez.phelti.demo@gmail.com','Disfruto enseñar inglés y aprender música.','Medellín','PRESENTIAL')
ON CONFLICT (correo) DO NOTHING;
INSERT INTO billetera (id_usuario) SELECT id_usuario FROM usuario WHERE correo IN ('germin.jaramillo.phelti.demo@gmail.com','sebastian.garcia.phelti.demo@hotmail.com','juan.perez.phelti.demo@gmail.com') ON CONFLICT (id_usuario) DO NOTHING;
INSERT INTO usuario_habilidad SELECT u.id_usuario,h.id_habilidad FROM usuario u CROSS JOIN habilidad h WHERE (u.correo,h.nombre) IN (('germin.jaramillo.phelti.demo@gmail.com','Hojas de cálculo'),('sebastian.garcia.phelti.demo@hotmail.com','Matemáticas'),('juan.perez.phelti.demo@gmail.com','Inglés')) ON CONFLICT DO NOTHING;
INSERT INTO usuario_interes SELECT u.id_usuario,i.id_interes FROM usuario u CROSS JOIN interes i WHERE (u.correo,i.nombre) IN (('germin.jaramillo.phelti.demo@gmail.com','Idiomas'),('sebastian.garcia.phelti.demo@hotmail.com','Programación'),('juan.perez.phelti.demo@gmail.com','Música')) ON CONFLICT DO NOTHING;
COMMIT;
