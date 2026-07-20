# Operación y despliegue

## Perfiles

- `dev`: crea el administrador desde variables de entorno y habilita mayor detalle de logs.
- `test`: usado por Testcontainers, Flyway y validación JPA contra PostgreSQL real.
- `prod`: exige configuración externa, cookies seguras y no muestra SQL.

El `docker-compose.yml` actual utiliza `dev`. No debe publicarse tal cual como entorno productivo.

## Puesta en marcha local

La guía reproducible para Windows y PowerShell está en [WINDOWS_SETUP.md](WINDOWS_SETUP.md).

Resumen:

```bash
cp .env.example .env
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

Servicios locales:

| Servicio | Puerto host | Uso |
|---|---:|---|
| PostgreSQL | 5432 | base local |
| Backend | 8081 | API directa y Swagger |
| Frontend/Nginx | 8080 | aplicación y proxy `/api` |

## Salud

- Actuator expone liveness y readiness.
- Compose espera PostgreSQL antes de iniciar el backend.
- El frontend espera readiness del backend.

Verificación:

```bash
curl --fail http://localhost:8081/api/actuator/health
```

## Logs

```bash
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
```

Los logs no deben contener contraseñas, JWT, refresh tokens ni cuerpos completos con información sensible.

## Base de datos

Flyway es la única vía admitida para cambios de esquema. `ddl-auto=validate` detecta divergencias sin modificar la base.

Consultar migraciones:

```bash
docker compose exec postgres \
  psql -U ropalista -d ropalista \
  -c 'select installed_rank, version, description, success from flyway_schema_history order by installed_rank;'
```

Reglas:

- no editar una migración ya desplegada en un ambiente persistente;
- crear una nueva versión `Vn__descripcion.sql`;
- validar en PostgreSQL real;
- mantener JPA y migraciones coherentes;
- no habilitar `ddl-auto=update`.

## Backups

Procedimiento base:

```bash
pg_dump --format=custom --file=ropalista.dump ropalista
pg_restore --clean --if-exists --dbname=ropalista_restore_test ropalista.dump
```

Con Compose:

```bash
docker compose exec -T postgres \
  pg_dump -U ropalista -d ropalista --format=custom > ropalista.dump
```

No se considera un backup válido hasta comprobar una restauración en otra base.

Pendiente:

- automatización programada;
- cifrado y retención;
- almacenamiento externo;
- alerta ante fallos;
- prueba periódica de restauración.

## Actualización local

```bash
git switch main
git pull --ff-only origin main
docker compose up --build -d
docker compose ps
```

Flyway aplica automáticamente las migraciones pendientes cuando inicia el backend.

## Detención

Conservar datos:

```bash
docker compose down
```

Eliminar todo el entorno de desarrollo:

```bash
docker compose down -v --remove-orphans
```

El segundo comando borra la base local.

## Validación previa a release

```bash
cd backend
mvn clean verify

cd ../frontend
npm ci
npm run lint
npm run build

cd ..
docker compose config --quiet
docker compose build
```

El pipeline de GitHub Actions ejecuta la misma división: backend, frontend y contenedores.

## Requisitos mínimos de producción

Antes de un despliegue real se requiere:

- usar perfil `prod`;
- secretos administrados fuera de `.env` versionado;
- TLS terminado en proxy o plataforma;
- dominio y política CORS explícitos;
- PostgreSQL persistente con backups restaurables;
- almacenamiento externo para evidencias cuando se implemente;
- límites de CPU, memoria y disco;
- rotación y agregación de logs;
- correlación de solicitudes;
- monitoreo de salud y capacidad;
- migración previa controlada;
- procedimiento de rollback;
- cuenta administrativa creada fuera del inicializador `dev`;
- protección contra fuerza bruta y rate limiting.

El Compose actual es adecuado para desarrollo, demostración y validación, no para operación comercial sin esos controles.
