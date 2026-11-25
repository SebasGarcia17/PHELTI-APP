# PHELTI-APP

API REST de PHELTI para el intercambio de conocimientos y servicios mediante phelts.

## Tecnologías y estructura

Java 25, Spring Boot 4.1.1, Maven, PostgreSQL, Spring Data JPA y JdbcTemplate. Controladores REST, DTOs, servicios y repositorios. El esquema se encuentra en `database/schema.sql` y la migración para acuerdos de horario en `database/migrations/001_programacion.sql`.

## Ejecución local

Configura `DB_URL`, `DB_USER`, `DB_PASSWORD` y `PHELTI_DEMO_KEY` (clave de al menos 32 caracteres, compartida con el frontend). Ejecuta `./mvnw spring-boot:run` o `.\mvnw.cmd spring-boot:run` en Windows. Puerto 8080. Swagger: `/swagger-ui/index.html`. Pruebas: `./mvnw clean verify`.

## Flujo de intercambio

El registro crea la billetera. Las publicaciones contienen condiciones y duración estimada, sin fecha ni hora; los usuarios pueden enviar propuestas, negociar cuando se permite y aceptar o rechazar. El solicitante propone el horario y el prestador lo acepta. La aceptación crea la actividad y reserva el importe acordado. Dos confirmaciones después del servicio completan la transferencia; una cancelación válida libera la reserva. Las calificaciones se registran una vez por participante.

Las rutas de programación son `GET /api/activities/schedule/{proposalId}`, `POST /api/activities/schedule`, `PUT /api/activities/schedule/{id}/accept`, `PUT /api/activities/schedule/{id}/decline` y `PUT /api/activities/schedule/proposals/{proposalId}/cancel`. Las operaciones protegidas requieren `X-Phelti-Key` y, según la operación, `X-Phelti-Actor`, enviados por el frontend. La consulta de métricas (`GET /api/users/{id}/metrics`) exige la clave del servidor y mantiene disponibles las métricas de reputación en los perfiles. Es una interfaz de demostración local, no autenticación real.

## Opciones predeterminadas

El esquema inicial contiene diez categorías, diez habilidades y diez intereses. Las rutas de consulta se conservan; no existen operaciones públicas para crear o eliminar estas opciones. Los perfiles pueden asociar y quitar habilidades e intereses. Para bases anteriores, aplicar la migración `database/migrations/002_catalogos.sql` después de 001, con respaldo y servicios detenidos. Se conservan las opciones históricas para no perder vínculos. Para una base nueva, `database/demo_catalogos.sql` carga las tres cuentas de demostración.
