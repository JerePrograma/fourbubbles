# Operación y despliegue

Versión: `0.2.0`.

## Perfiles

- `dev`: administrador inicial y logs de desarrollo.
- `test`: Testcontainers/Flyway/JPA.
- `prod`: cookies seguras, logs JSON y SQL deshabilitado.

Compose usa `dev`; no es una topología productiva.

## Servicios

| Servicio | Puerto | Uso |
|---|---:|---|
| PostgreSQL | 5432 | base local |
| Backend | 8081 | API/Swagger/Actuator |
| Frontend | 8080 | SPA/proxy |

## Inicio recomendado

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

La verificación exige siete migraciones Flyway o más, SPA, login y API protegida.

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

Usar `X-Request-ID` para correlacionar incidentes. No compartir tokens, contraseñas ni cuerpos sensibles.

## Migraciones

```bash
docker compose exec -T postgres \
  psql -U ropalista -d ropalista \
  -c 'select installed_rank, version, description, success from flyway_schema_history order by installed_rank;'
```

Reglas:

- no editar V1–V7;
- cambios nuevos usan V8 o posterior;
- validar con PostgreSQL real;
- mantener JPA/SQL alineados;
- no usar `ddl-auto=update`.

## Recepción

### Idempotency-Key

Los clientes deben generar una clave estable por intento lógico. No generar una clave nueva al reintentar por timeout, porque otra clave sobre un pedido recibido responde conflicto.

Ejemplo:

```text
web-reception-550e8400-e29b-41d4-a716-446655440000
```

### Evidencias

0.2.0 solo registra metadata. Antes de usar evidencia en una operación real debe existir un procedimiento externo que:

1. cargue el archivo a almacenamiento privado;
2. calcule SHA-256;
3. obtenga tamaño/MIME;
4. entregue `objectKey` al formulario;
5. controle retención y permisos.

No se debe ingresar una clave ficticia y asumir que la foto quedó almacenada.

### Etiquetas

`RCV-xxxxxx` es único en la base. La impresión física todavía depende del procedimiento externo; el sistema genera el identificador, no maneja una impresora.

## Backups

```bash
docker compose exec -T postgres \
  pg_dump -U ropalista -d ropalista --format=custom > ropalista.dump
```

Una copia no se considera válida sin restauración de prueba.

Las evidencias externas deben tener backup/retención independientes de PostgreSQL.

## Actualización local

```powershell
git switch main
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Flyway aplica V7 automáticamente sobre V6.

## Detención

Conservar datos:

```bash
docker compose down
```

Eliminar datos:

```bash
docker compose down -v --remove-orphans
```

## Gate de release

```bash
cd backend && mvn clean verify
cd ../frontend && npm ci && npm run lint && npm test && npm run build
cd .. && docker compose config --quiet && docker compose build
```

Luego debe pasar runtime smoke.

## Diagnóstico

1. `docker compose ps`.
2. logs de backend/postgres.
3. `Verify-Local.ps1`.
4. `flyway_schema_history`.
5. revisar `X-Request-ID`.
6. comprobar estado del pedido antes de recibir: `PICKED_UP`.
7. ante retry, reutilizar la misma `Idempotency-Key`.

## Producción mínima

- perfil `prod`;
- TLS;
- secretos gestionados;
- PostgreSQL persistente;
- backups restaurables;
- object storage privado;
- carga firmada y escaneo de evidencias;
- observabilidad y alertas;
- límites de recursos;
- rate limiting compartido;
- rollback;
- política de datos personales.

## Rollback

V7 es forward-only. No editarla ni borrarla. Un rollback requiere backup, migración correctiva y aplicación compatible.
