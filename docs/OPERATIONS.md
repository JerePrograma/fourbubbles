# Operación y despliegue

## Perfiles

- `dev`: crea administrador desde variables y habilita mayor detalle de logs.
- `test`: usado por Testcontainers, Flyway y validación JPA.
- `prod`: cookies seguras, logs JSON correlacionados y sin SQL.

El `docker-compose.yml` actual utiliza `dev`. No debe publicarse tal cual como producción.

## Puesta en marcha local

Guía Windows: [WINDOWS_SETUP.md](WINDOWS_SETUP.md).

```bash
cp .env.example .env
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

| Servicio | Puerto host | Uso |
|---|---:|---|
| PostgreSQL | 5432 | base local |
| Backend | 8081 | API, Swagger y Actuator |
| Frontend/Nginx | 8080 | aplicación y proxy `/api` |

## Variables operativas nuevas en 0.1.1

| Variable | Inicial | Uso |
|---|---|---|
| `LOGIN_MAX_ATTEMPTS` | `5` | fallos antes del bloqueo |
| `LOGIN_ATTEMPT_WINDOW` | `PT15M` | ventana de conteo |
| `LOGIN_BLOCK_DURATION` | `PT15M` | duración del bloqueo |

El contador es local al proceso. En despliegues con más de una réplica debe reemplazarse por almacenamiento compartido.

## Salud

```bash
curl --fail http://localhost:8081/api/actuator/health
```

Compose espera PostgreSQL antes del backend y readiness del backend antes del frontend.

## Correlación

Toda respuesta contiene `X-Request-ID`.

Ejemplo:

```bash
curl -i -H 'X-Request-ID: soporte-20260720-001' \
  http://localhost:8081/api/actuator/health
```

El identificador se utiliza para localizar la solicitud en logs. Valores con caracteres inseguros se reemplazan por UUID.

## Logs

```bash
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
```

Seguimiento:

```bash
docker compose logs -f backend
```

Los logs no deben contener contraseñas, JWT, refresh tokens ni cuerpos sensibles. El perfil `prod` incluye `requestId` en cada evento JSON.

## Base de datos

Flyway es la única vía admitida. `ddl-auto=validate` detecta divergencias sin modificar la base.

```bash
docker compose exec postgres \
  psql -U ropalista -d ropalista \
  -c 'select installed_rank, version, description, success from flyway_schema_history order by installed_rank;'
```

Reglas:

- no editar migraciones ya desplegadas;
- crear una nueva `Vn__descripcion.sql`;
- validar sobre PostgreSQL real;
- mantener JPA y migraciones coherentes;
- no habilitar `ddl-auto=update`.

## Backups

```bash
docker compose exec -T postgres \
  pg_dump -U ropalista -d ropalista --format=custom > ropalista.dump
```

Prueba de restauración:

```bash
createdb ropalista_restore_test
pg_restore --clean --if-exists --dbname=ropalista_restore_test ropalista.dump
```

No se considera válido un backup sin restauración probada.

Pendiente:

- automatización;
- cifrado;
- retención;
- almacenamiento externo;
- alertas;
- restauraciones periódicas.

## Actualización local

Windows recomendado:

```powershell
git switch main
git pull --ff-only origin main
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Manual:

```bash
git switch main
git pull --ff-only origin main
docker compose up --build -d
docker compose ps
```

Flyway aplica migraciones pendientes al iniciar el backend.

## Detención

Conservar datos:

```bash
docker compose down
```

Eliminar entorno y datos:

```bash
docker compose down -v --remove-orphans
```

El segundo comando es destructivo.

## Validación previa a release

```bash
cd backend
mvn clean verify

cd ../frontend
npm ci
npm run lint
npm test
npm run build

cd ..
docker compose config --quiet
docker compose build
```

## Requisitos mínimos de producción

Antes de operar comercialmente:

- perfil `prod`;
- secretos en gestor externo;
- TLS;
- dominio y CORS explícitos;
- proxy confiable que sobrescriba cabeceras reenviadas;
- rate limiting perimetral;
- limitador de login compartido, preferentemente Redis;
- PostgreSQL persistente;
- backups restaurables;
- almacenamiento externo de evidencias;
- límites de CPU, memoria y disco;
- rotación y agregación de logs;
- monitoreo de salud, errores y capacidad;
- migración controlada;
- rollback probado;
- cuenta administrativa fuera del inicializador `dev`;
- CSP, HSTS y cabeceras de seguridad;
- escaneo de dependencias, imágenes y secretos;
- política de incidentes y retención.

## Limitaciones operativas de 0.1.1

- bloqueo de login local a una instancia;
- sin ajuste manual de cotización;
- sin recepción real ni evidencias;
- sin rutas ni agenda operativa completa;
- sin historial visible de pagos;
- sin caja ni costos;
- sin smoke test HTTP automatizado del Compose ya iniciado.

El Compose actual sirve para desarrollo, demostración y validación, no para producción.
