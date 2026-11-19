# PHELTI - Plataforma de Intercambio de Tiempo

API REST en Spring Boot que permite intercambiar tiempo (phelts) entre usuarios.

## Arquitectura

- `controller` -> `service` (interfaces + implementaciones) -> `repository`
- Publicaciones y propuestas: JDBC con `JdbcTemplate` y SQL explicito (sin `SELECT *`; las conexiones las abre y cierra Spring).
- Usuarios, billeteras, actividades, habilidades, intereses, disponibilidad, transacciones, confirmaciones, calificaciones y categorias: JPA/Hibernate con `@OneToOne` (Usuario-Billetera), `@OneToMany` / `@ManyToOne` (Usuario-Actividad, Usuario-Disponibilidad, Billetera-Transaccion, Actividad-Confirmacion, Actividad-Calificacion) y `@ManyToMany` (Usuario-Habilidad, Usuario-Interes).
- CORS: configurable en `phelti.cors.allowed-origins` (`application.properties`); por defecto permite los puertos 3000, 4200, 5173 y 5500 de `localhost`, para que un front en otro repositorio consuma la API.
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
4. `POST /api/proposals` propone sobre una publicacion (un usuario no puede tener dos propuestas pendientes o aceptadas sobre la misma publicacion); `PUT /api/proposals/{id}/accept` la acepta (la publicacion pasa a `IN_PROGRESS` y las demas propuestas pendientes se rechazan).
5. `POST /api/activities` agenda la actividad (ambos usuarios deben tener billetera): reserva los phelts del solicitante (quien recibe el servicio) y genera el codigo de check-in.
6. `POST /api/activities/{id}/confirmations` (RF-11): cada uno de los dos usuarios confirma con el codigo de check-in. Cuando ambos confirmaron, la actividad pasa a `COMPLETED`, los phelts reservados se transfieren al participante y la publicacion se completa. `GET /api/activities/{id}/confirmations` lista las confirmaciones.
   `PUT /api/activities/{id}/cancel` libera los phelts, marca la propuesta como `CANCELLED` y vuelve a publicar la publicacion para que reciba nuevas propuestas.
7. `POST /api/ratings` (RF-15): cada usuario califica de 1 a 5 al otro en una actividad `COMPLETED` (una calificacion por usuario y actividad); el promedio del calificado se actualiza.

## Requisitos funcionales y endpoints

| RF | Requisito | Endpoints |
|---|---|---|
| RF-01 | Registro de usuarios | `POST /api/users` |
| RF-02 | Gestion de perfil | `GET /api/users/{id}`, `PUT /api/users/{id}` (nombre, descripcion, ubicacion, modalidad preferida) |
| RF-03 | Habilidades | `POST/GET /api/skills`, `GET /api/users/{id}/skills`, `PUT/DELETE /api/users/{id}/skills/{skillId}` |
| RF-04 | Intereses | `POST/GET /api/interests`, `GET /api/users/{id}/interests`, `PUT/DELETE /api/users/{id}/interests/{interestId}` |
| RF-05 | Disponibilidad | `POST/GET /api/users/{id}/availability`, `DELETE /api/availability/{id}` (no permite horarios que se crucen el mismo dia) |
| RF-06 | Publicacion de actividades | `POST/GET/PUT/DELETE /api/publications` (titulo, descripcion, modalidad, `durationMinutes` y `scheduledAt` opcionales), `GET /api/categories`, `POST /api/categories` |
| RF-07 | Busqueda y filtrado | `GET /api/publications/search` (tipo, modalidad, categoria, palabra clave, maximo de phelts, estado), `GET /api/users/search` (habilidad, interes, modalidad, dia) |
| RF-08 | Emparejamiento | `GET /api/matching/users/{userId}/publications` |
| RF-09 | Solicitud de actividad | `POST /api/proposals` (con `proposedAmount` opcional si la publicacion es negociable) |
| RF-10 | Aceptacion o rechazo | `PUT /api/proposals/{id}/accept`, `PUT /api/proposals/{id}/reject` |
| RF-11 | Confirmacion de realizacion | `POST/GET /api/activities/{id}/confirmations` |
| RF-12 | Gestion de Tiempo | `POST /api/wallets/users/{id}`, `GET /api/wallets/users/{id}`, `POST /api/wallets/users/{id}/deposits` |
| RF-13 | Transferencia de Tiempo | se registra al completar la actividad; cada movimiento queda en `transaccion` con su actividad (`id_actividad`) y, en las transferencias, usuario origen y destino |
| RF-14 | Historial y metricas | `GET /api/wallets/users/{id}/transactions`, `GET /api/activities/users/{id}`, `GET /api/users/{id}/metrics` |
| RF-15 | Calificaciones | `POST /api/ratings`, `GET /api/ratings/users/{id}`, `GET /api/ratings/activities/{id}` |
| RF-16 | Pruebas de flujos criticos | `./mvnw test`: pruebas unitarias de todos los metodos de los services |

### Emparejamiento (RF-08)

Para un usuario se evaluan las publicaciones `PUBLISHED` de otros usuarios y se ordenan por puntaje:
- Publicaciones `NEEDS_SUPPORT`: +3 por cada habilidad del usuario que aparece en el titulo, la descripcion o la categoria.
- Publicaciones `OFFERS_SUPPORT`: +2 por cada interes del usuario que aparece en el titulo, la descripcion o la categoria.
- Si el usuario tiene modalidad preferida, solo se consideran publicaciones de esa modalidad (+1).
- +2 si el usuario y el dueño de la publicacion tienen disponibilidad que se cruza el mismo dia.

Solo aparecen publicaciones con al menos una coincidencia de habilidad o interes; cada resultado trae la lista de razones.

### Historial de Tiempo (RF-13 y RF-14)

Cada movimiento de la billetera queda registrado con su tipo: `DEPOSIT`, `RESERVE`, `RELEASE`, `TRANSFER_OUT` y `TRANSFER_IN`.

## Pruebas

`./mvnw test` ejecuta las pruebas unitarias (JUnit 5 y Mockito) de todos los metodos de los services: casos exitosos, validaciones y errores de negocio.
