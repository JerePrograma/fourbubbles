# Diagnóstico y resolución de problemas

Fuente de verdad: `scripts/Start-Local.ps1`, `scripts/Verify-Local.ps1`, helpers de `scripts/`, `docker-compose.yml`, healthchecks y workflows.

Las soluciones destructivas no son predeterminadas. Preservar el volumen PostgreSQL salvo que se acepte explícitamente perder los datos locales.

## Docker no está disponible

### Síntoma

`Start-Local.ps1` o `Verify-Local.ps1` informan que Docker o Docker Compose no están disponibles.

### Causa confirmada o probable

- Docker Desktop no está iniciado;
- el motor usa contenedores Windows en lugar de Linux;
- `docker` no está en `PATH`;
- el subcomando `docker compose` no está instalado.

### Cómo diagnosticarlo

```powershell
docker info
docker compose version
```

### Solución segura

Iniciar Docker Desktop, seleccionar contenedores Linux y volver a ejecutar el script. No continuar con comandos manuales que omitan las comprobaciones.

### Cómo verificar la solución

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
```

### Archivos relacionados

- `scripts/Start-Local.ps1`
- `scripts/Verify-Local.ps1`

## Un puerto publicado está ocupado

### Síntoma

El preflight rechaza `POSTGRES_HOST_PORT`, `BACKEND_HOST_PORT` o `FRONTEND_HOST_PORT` y muestra un contenedor o proceso ocupante.

### Causa confirmada o probable

Otro proyecto usa el puerto host. Los puertos internos de Compose no son el problema.

### Cómo diagnosticarlo

Revisar el diagnóstico emitido por `Start-Local.ps1`. No detener el proceso automáticamente si no pertenece a Four Bubbles.

### Solución segura

Editar solo los puertos host en `.env`, manteniéndolos distintos:

```dotenv
POSTGRES_HOST_PORT=15432
BACKEND_HOST_PORT=18081
FRONTEND_HOST_PORT=18080
```

### Cómo verificar la solución

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

### Archivos relacionados

- `.env.example`
- `scripts/Local.Common.ps1`
- `docker-compose.yml`

## `.env` falta o conserva placeholders

### Síntoma

Compose informa una variable obligatoria ausente o los scripts rechazan `change-me`, `change-me-now`, un JWT inválido o puertos duplicados.

### Causa confirmada

`POSTGRES_PASSWORD`, `JWT_SECRET_BASE64` o `APP_DEV_ADMIN_PASSWORD` no poseen valores locales válidos.

### Cómo diagnosticarlo

No imprimir secretos. Ejecutar:

```powershell
.\scripts\Start-Local.ps1 -SkipOpen
```

El script valida presencia, placeholders, Base64, longitud mínima y puertos.

### Solución segura

Permitir que `Start-Local.ps1` cree `.env` cuando no existe. Si existe, el script agrega claves faltantes sin reemplazar secretos.

### Cómo verificar la solución

```powershell
.\scripts\tests\Local.Common.Tests.ps1
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
```

### Archivos relacionados

- `.env.example`
- `scripts/Local.Common.ps1`
- `scripts/Start-Local.ps1`

## El login falla después de cambiar la contraseña de `.env`

### Síntoma

`Verify-Local.ps1` no puede autenticar al administrador aunque el valor actual de `APP_DEV_ADMIN_PASSWORD` parezca correcto.

### Causa confirmada

Cambiar `.env` no cambia el hash del usuario ya persistido en PostgreSQL.

### Cómo diagnosticarlo

Revisar si la base fue creada con otra contraseña. No registrar ni publicar la contraseña anterior.

### Solución segura

Restaurar en `.env` la contraseña usada al crear la base. Solo en un entorno local descartable, y aceptando perder todos los datos, recrear mediante:

```powershell
.\scripts\Start-Local.ps1 -Reset -Rebuild -SkipOpen
```

### Cómo verificar la solución

```powershell
.\scripts\Verify-Local.ps1
```

### Archivos relacionados

- `scripts/Start-Local.ps1`
- `scripts/Verify-Local.ps1`
- `backend/src/main/java/ar/com/ropalista/auth/application/DevAdminInitializer.java`

## El backend no alcanza readiness

### Síntoma

El contenedor `backend` no queda saludable o `/api/actuator/health/readiness` no devuelve `UP`.

### Causa confirmada o probable

- PostgreSQL no está saludable;
- credenciales o host de base incorrectos;
- migración Flyway fallida;
- validación JPA incompatible con el esquema;
- secreto JWT ausente o inválido.

### Cómo diagnosticarlo

```powershell
docker compose ps --all
docker compose logs --tail 250 postgres backend
```

`Start-Local.ps1` ya muestra este diagnóstico al fallar.

### Solución segura

Corregir `.env` o la causa de migración. No desactivar Flyway ni cambiar `ddl-auto=validate` para forzar el arranque.

### Cómo verificar la solución

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

### Archivos relacionados

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/`
- `docker-compose.yml`

## El frontend responde pero `/api` falla

### Síntoma

La SPA carga, pero las solicitudes a `/api` devuelven error de gateway o no alcanzan el backend.

### Causa confirmada o probable

El frontend puede iniciar antes que el backend; Nginx debe resolver `backend` de forma diferida. Un backend no saludable seguirá produciendo fallos hasta recuperarse.

### Cómo diagnosticarlo

```powershell
docker compose ps --all
docker compose logs --tail 250 frontend backend
```

### Solución segura

Corregir primero el backend. No reemplazar `backend:8080` por un puerto host dentro de Nginx o Compose.

### Cómo verificar la solución

```powershell
.\scripts\Verify-Local.ps1
```

### Archivos relacionados

- `infra/nginx/default.conf`
- `.github/workflows/runtime-smoke.yml`

## Flyway informa menos de ocho migraciones

### Síntoma

`Verify-Local.ps1` rechaza el stack porque `flyway_schema_history` contiene menos de ocho migraciones exitosas.

### Causa confirmada o probable

La imagen backend está desactualizada, una migración falló o se está usando otro volumen/proyecto Compose.

### Cómo diagnosticarlo

```powershell
docker compose exec -T postgres psql -U $env:POSTGRES_USER -d $env:POSTGRES_DB -c "select installed_rank, version, description, success from flyway_schema_history order by installed_rank;"
```

Cuando las variables no estén exportadas en la sesión, usar los valores de `.env` sin publicarlos.

### Solución segura

Reconstruir la imagen y revisar el error de Flyway. No editar migraciones `V1`–`V8` ya publicadas.

### Cómo verificar la solución

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

### Archivos relacionados

- `scripts/Verify-Local.ps1`
- `backend/src/main/resources/db/migration/`

## La API responde 401 o 403 sin token

### Síntoma

`GET /api/catalog/services` responde `401` o `403` de forma anónima.

### Causa confirmada

Es el comportamiento esperado: el catálogo requiere usuario autenticado por la configuración global.

### Cómo diagnosticarlo

Autenticar con `POST /api/auth/login` y repetir con `Authorization: Bearer <ACCESS_TOKEN>`.

### Solución segura

No abrir el endpoint para silenciar el smoke. Corregir la sesión o el cliente que omite el token.

### Cómo verificar la solución

```powershell
.\scripts\Verify-Local.ps1
```

### Archivos relacionados

- `backend/src/main/java/ar/com/ropalista/auth/infrastructure/SecurityConfig.java`
- `frontend/src/api/httpClient.ts`

## Una recepción repetida devuelve conflicto

### Síntoma

Una segunda solicitud de recepción responde `ORDER_ALREADY_RECEIVED` o `IDEMPOTENCY_KEY_CONFLICT`.

### Causa confirmada

- se usó otra clave para un pedido ya recibido; o
- la misma clave se reutilizó para otro pedido.

### Cómo diagnosticarlo

Comparar pedido y `Idempotency-Key` con la primera operación. La repetición legítima usa exactamente la misma clave y el mismo pedido.

### Solución segura

No generar otra recepción ni borrar la existente. Si hubo un error operativo real, registrar la necesidad de una corrección versionada; esa función aún está pendiente.

### Cómo verificar la solución

Repetir la solicitud original con la misma clave debe devolver el mismo identificador.

### Archivos relacionados

- `ReceptionService.receive`
- `ReceptionFlowIT`
- `docs/API.md`

## La compatibilidad produce un resultado inesperado

### Síntoma

Dos pedidos quedan incompatibles o una opción del formulario se vuelve más restrictiva.

### Causa confirmada o probable

`COMPAT-1` aplica restricciones de color, material, hipoalergénico, bebé/mascotas, suciedad, fragancia y exclusividad. Las preferencias del cliente o pedido no pueden relajarse desde la UI.

### Cómo diagnosticarlo

Revisar `reasons`, `recommendation`, versiones de perfil y `ruleVersion` de la evaluación. Distinguir `compatible` de `effectivelyCompatible`.

### Solución segura

Corregir el perfil si el dato es erróneo. No modificar la evaluación histórica. Una excepción solo puede autorizarla `ADMIN` con motivo y no altera el resultado original.

### Cómo verificar la solución

Crear una evaluación nueva después de corregir el perfil; la anterior debe permanecer consultable.

### Archivos relacionados

- `CompatibilityEngine.evaluate`
- `CompatibilityService.evaluate`
- `docs/ASSUMPTIONS.md`
