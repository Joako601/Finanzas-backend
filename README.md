# Finanzas · `backend`

API REST en **Java 21 + Spring Boot 4 + PostgreSQL** para el flavor `nube` de Finanzas.

## Requisitos
- JDK 21
- Docker (opcional, para levantar PostgreSQL 16 local con `docker compose up -d`)

## Arranque rápido en local
1. Levantar PostgreSQL:
   ```bash
   docker compose up -d
   ```
2. Copiar la plantilla de configuración local:
   ```bash
   cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
   ```
   y colocar `finanzas.firebase.project-id` (y opcionalmente `finanzas.external.banxico.token`).
3. Ejecutar con perfil `dev`:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```
4. Abrir Swagger UI en `http://localhost:8090/swagger-ui.html` o verificar salud en `GET http://localhost:8090/api/v1/health`.

## Pruebas (sin PostgreSQL ni internet)
Las pruebas usan H2 en `MODE=PostgreSQL` ejecutando todas las migraciones Flyway reales:
```bash
./gradlew test build
```
