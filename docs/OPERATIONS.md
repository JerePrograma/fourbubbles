# Operación y despliegue

Última actualización: 2026-07-21.

## Perfiles

- `dev`: administrador inicial y logs de desarrollo.
- `test`: Testcontainers, Flyway y validación JPA.
- `prod`: cookies seguras, logs JSON y SQL deshabilitado.

Compose usa `dev`; no es una topología productiva.

## Topología local

| Servicio | Puerto interno | Puerto host predeterminado | Variable |
|---|---:|---:|---|
| frontend | 80 | 8080 | `FRONTEND_HOST_PORT` |
| backend | 8080 | 8081 | `BACKEND_HOST_PORT` |
| postgres | 5432 | 5432 | `POSTGRES_HOST_PORT` |

Las publicaciones se limitan a `127.0.0.1`. La red interna permanece estable:

- frontend llama a `backend:8080`;
- backend llama a `postgres:5432`.

`COMPOSE_PROJECT_NAME=fourbubbles` evita colisiones de nombres, red y volumen con otros proyectos.

## Inicio recomendado

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Parámetros:

| Parámetro | Efecto |
|---|---|
| `-Rebuild` | reconstruye imágenes antes de iniciar |
| `-Reset` | elimina el volumen PostgreSQL y recrea el stack |
| `-SkipOpen` | no abre el navegador al finalizar |

## Idempotencia de `.env`

`Start-Local.ps1` crea `.env` desde `.env.example` cuando no existe. Si existe:

- conserva todos los valores existentes;
- no reemplaza secretos;
- agrega variables faltantes;
- rechaza placeholders o Base64 JWT inválido;
- valida que los puertos sean distintos y estén entre 1 y 65535.

Modificar `APP_DEV_ADMIN_PASSWORD` después de crear PostgreSQL no cambia la contraseña persistida del usuario. Debe restaurarse el valor original o recrearse el volumen con `-Reset`.

## Conflictos de puertos

La prevalidación ocurre antes de construir imágenes. Para cada puerto muestra, según corresponda:

- contenedor, imagen, ID y publicación;
- PID, proceso y ruta ejecutable.

No se detienen proyectos ni procesos ajenos. Las correcciones admitidas son cambiar el puerto en `.env` o detener manualmente el recurso identificado.

## Health y dependencias

- PostgreSQL usa `pg_isready`.
- Backend usa `/api/actuator/health/readiness`.
- Frontend comprueba que Nginx sirva el punto de montaje React.
- Backend depende de PostgreSQL `service_healthy`.
- Frontend depende del backend `service_healthy`.
- Los tres servicios usan `restart: on-failure:3`, evitando reinicios infinitos por configuración inválida.

Nginx usa el DNS embebido de Docker y resolución diferida de `backend`, por lo que puede iniciar aunque el registro DNS del backend todavía no exista. Un fallo transitorio de resolución ya no mata permanentemente al frontend.

## Verificación

```powershell
.\scripts\Verify-Local.ps1
```

Comprueba:

1. salida de Compose vacía, objeto único o colección sin depender de `.Count` sobre un escalar;
2. presencia exacta de `postgres`, `backend` y `frontend`;
3. estado `running` y health `healthy`;
4. puertos efectivos mediante `docker compose port`;
5. readiness backend;
6. al menos ocho migraciones Flyway exitosas;
7. SPA y proxy Nginx;
8. rechazo anónimo 401/403;
9. login administrativo;
10. catálogo protegido no vacío.

Ante cualquier error imprime estado y logs y termina con código no exitoso.

## Detención, reinicio y limpieza

Detener preservando datos:

```powershell
docker compose down --remove-orphans
```

Reiniciar con la configuración actual:

```powershell
.\scripts\Start-Local.ps1
.\scripts\Verify-Local.ps1
```

Reconstruir:

```powershell
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Destruir datos:

```powershell
docker compose down -v --remove-orphans
```

O mediante el flujo integrado:

```powershell
.\scripts\Start-Local.ps1 -Reset -Rebuild
```

## Recuperación ante inicio parcial

Si el inicio falla después de invocar Compose:

1. se muestran estado y logs;
2. se ejecuta `docker compose down --remove-orphans`;
3. se conserva `postgres_data`;
4. el script relanza la excepción;
5. no se imprimen URLs ni mensajes de éxito.

Reintento recomendado:

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
```

## Actualización

```powershell
git switch main
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Flyway aplica migraciones pendientes automáticamente. No se editan migraciones ya publicadas.

## Diagnóstico manual

```powershell
docker compose ps --all
docker compose logs --tail 300 postgres backend frontend
docker compose port postgres 5432
docker compose port backend 8080
docker compose port frontend 80
```

## Backups y producción

No existe automatización productiva. Antes de usar datos reales se requiere:

- backup programado, cifrado y con retención;
- restauración ensayada y RPO/RTO definidos;
- TLS y secretos administrados;
- object storage privado para evidencias;
- observabilidad y alertas;
- límites CPU/memoria;
- rollback compatible con migraciones aditivas;
- smoke post-deploy.
