# Operación y despliegue

Versión: `0.1.2`.

## Perfiles

- `dev`: crea administrador desde variables y habilita mayor detalle de logs.
- `test`: Testcontainers, Flyway y validación JPA.
- `prod`: cookies seguras, logs JSON correlacionados y SQL deshabilitado.

El `docker-compose.yml` actual usa `dev`. Es para desarrollo, demostración y evaluación, no para producción comercial.

## Servicios locales

| Servicio | Puerto | Uso |
|---|---:|---|
| PostgreSQL | 5432 | base local |
| Backend | 8081 | API, Swagger y Actuator |
| Frontend/Nginx | 8080 | SPA y proxy `/api` |

## Inicio recomendado

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

`Start-Local.ps1`:

- verifica Docker;
- crea `.env` si falta;
- genera contraseñas y JWT secret;
- valida Compose;
- inicia el stack;
- espera health;
- muestra la contraseña inicial solo cuando la genera.

`Verify-Local.ps1`:

- verifica servicios;
- health;
- seis migraciones o más;
- HTML de la SPA;
- login con `.env`;
- consulta protegida del catálogo.

## Inicio manual

```bash
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

## Salud

```bash
curl --fail http://localhost:8081/api/actuator/health
curl --fail http://localhost:8081/api/actuator/health/readiness
```

## Logs

```bash
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
docker compose logs -f backend
```

No deben aparecer contraseñas, access tokens, refresh tokens ni cuerpos completos sensibles.

## Migraciones

Consultar:

```bash
docker compose exec -T postgres \
  psql -U ropalista -d ropalista \
  -c 'select installed_rank, version, description, success from flyway_schema_history order by installed_rank;'
```

Reglas:

- no editar V1–V6;
- cada cambio usa una migración nueva;
- validar sobre PostgreSQL real;
- mantener JPA y SQL alineados;
- no usar `ddl-auto=update`.

## Backups

Crear backup:

```bash
docker compose exec -T postgres \
  pg_dump -U ropalista -d ropalista --format=custom > ropalista.dump
```

Prueba de restauración orientativa:

```bash
createdb ropalista_restore_test
pg_restore --clean --if-exists --dbname=ropalista_restore_test ropalista.dump
```

Un archivo no es un backup confiable hasta probar su restauración.

Pendiente productivo:

- programación;
- cifrado;
- retención;
- almacenamiento externo;
- alertas;
- restauración periódica automatizada.

## Actualización local

```powershell
git switch main
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Flyway aplica automáticamente V6 sobre una base existente en V5.

## Detención

Conservar datos:

```bash
docker compose down
```

Borrar entorno y datos:

```bash
docker compose down -v --remove-orphans
```

El segundo comando elimina la base local completa.

## Validación de release

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

Luego debe pasar el smoke runtime:

- stack completo;
- readiness;
- SPA;
- login;
- JWT;
- API protegida.

## Procedimiento de diagnóstico

1. `docker compose ps`.
2. `docker compose logs --tail 300 backend`.
3. comprobar `.env` sin compartir su contenido.
4. ejecutar `docker compose config --quiet`.
5. ejecutar `Verify-Local.ps1`.
6. revisar `flyway_schema_history`.
7. si la contraseña del administrador cambió después de crear el volumen, restaurar la original o recrear conscientemente la base.

## Requisitos mínimos de producción

- perfil `prod`;
- TLS;
- dominio y CORS explícitos;
- secretos administrados;
- PostgreSQL persistente;
- backups restaurables;
- observabilidad central;
- límites CPU/memoria/disco;
- rate limiting compartido;
- almacenamiento externo para evidencias;
- migración controlada;
- rollback;
- cuenta administrativa creada fuera del inicializador `dev`;
- política de retención y datos personales;
- escaneo de dependencias e imágenes.

## Rollback

Las migraciones Flyway actuales son forward-only. Un rollback productivo debe combinar:

1. restauración o migración correctiva explícita;
2. imagen de aplicación compatible con el esquema resultante;
3. backup validado previo;
4. ventana de mantenimiento;
5. verificación funcional posterior.

No se debe intentar rollback editando una migración ya aplicada.
