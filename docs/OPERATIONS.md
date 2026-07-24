# Operación y despliegue

Versión funcional: `0.4.0`.

## Inicio

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

`Start-Local.ps1` crea/completa `.env`, valida secretos y puertos, inicia Compose y espera health. No detiene recursos ajenos ni elimina PostgreSQL salvo `-Reset`.

## Topología

| Servicio | Interno | Host predeterminado |
|---|---:|---:|
| frontend | 80 | 8080 |
| backend | 8080 | 8081 |
| postgres | 5432 | 5432 |

Los puertos host se configuran con `FRONTEND_HOST_PORT`, `BACKEND_HOST_PORT` y `POSTGRES_HOST_PORT`. Se publican en `127.0.0.1`.

## Verificación

`Verify-Local.ps1` exige:

1. `postgres`, `backend`, `frontend` únicos, running y healthy;
2. readiness `UP`;
3. al menos diez migraciones exitosas;
4. SPA y proxy Nginx;
5. rechazo anónimo;
6. login;
7. catálogo autenticado.

## Gates

```bash
cd backend && mvn clean verify
```

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

```powershell
.\scripts\tests\Local.Common.Tests.ps1
```

```bash
docker compose config --quiet
docker compose build
```

## Actualización

```powershell
git switch main
git status
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Flyway aplica `V9` y `V10` automáticamente. No editar `V1`–`V10`.

## Detención y datos

Preservar datos:

```powershell
docker compose down --remove-orphans
```

Destruir base local:

```powershell
.\scripts\Start-Local.ps1 -Reset -Rebuild
```

`-Reset` elimina clientes, pedidos, recepciones, perfiles, evaluaciones, ciclos, pagos y auditoría locales.

## Diagnóstico

```powershell
docker compose ps --all
docker compose logs --tail 300 postgres backend frontend
```

## Producción

`NO VERIFICADO`: no existen dominio/TLS, gestor de secretos, backup/restore, límites de recursos, observabilidad, object storage ni rollback ensayado. Compose usa perfil `dev`; no desplegarlo con datos reales.
