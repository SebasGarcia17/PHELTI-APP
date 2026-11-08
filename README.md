# PHELTI - Plataforma de Intercambio de Tiempo

API REST en Spring Boot que permite intercambiar tiempo (phelts) entre usuarios.

## Arquitectura

- `controller` -> `service` (interfaces + implementaciones) -> `repository`
- Publicaciones y propuestas: JDBC con `JdbcTemplate` y SQL explicito (sin `SELECT *`; las conexiones las abre y cierra Spring).
- Usuarios, billeteras y actividades: JPA/Hibernate con `@OneToOne` (Usuario-Billetera), `@OneToMany` y `@ManyToOne` (Usuario-Actividad).
- DTOs: todos los controllers reciben y devuelven DTOs (`*Request` / `*Response`); las clases de `model` y `entity` no se exponen en la API.
- Validacion: los `*Request` usan Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@Positive`, `@Size`...) y los services mantienen las reglas de negocio.
- Errores: jerarquia `ResourceNotFoundException` (404), `InvalidRequestException` (400) y `ConflictException` (409) resueltas en `GlobalExceptionHandler`, que ademas responde 400 a errores de validacion y 500 (con log) a errores inesperados, siempre con el mismo `ErrorResponse`.

## Ejecucion

1. Crear la base `phelti_db` en PostgreSQL y ejecutar `database/schema.sql`.
2. Definir la variable de entorno `DB_PASSWORD`.
3. `./mvnw spring-boot:run`
4. Swagger UI: `http://localhost:8080/swagger-ui/index.html`
5. Pruebas: `./mvnw test`

## Flujo de negocio

1. `POST /api/users` registra usuarios.
2. `POST /api/wallets/users/{userId}` crea la billetera y `POST /api/wallets/users/{userId}/deposits` recarga phelts.
3. `POST /api/publications` publica una oferta (`OFFERS_SUPPORT`) o una necesidad (`NEEDS_SUPPORT`).
4. `POST /api/proposals` propone sobre una publicacion; `PUT /api/proposals/{id}/accept` la acepta (la publicacion pasa a `IN_PROGRESS` y las demas propuestas pendientes se rechazan).
5. `POST /api/activities` agenda la actividad: reserva los phelts del solicitante (quien recibe el servicio) y genera el codigo de check-in.
6. `PUT /api/activities/{id}/complete` con el codigo transfiere los phelts reservados al participante y completa la publicacion.
   `PUT /api/activities/{id}/cancel` libera los phelts, marca la propuesta como `CANCELLED` y vuelve a publicar la publicacion para que reciba nuevas propuestas.
