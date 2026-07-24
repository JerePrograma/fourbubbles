# Puesta en marcha en Windows con PowerShell

Versión funcional: `0.4.0`.

## Requisitos

- Git para Windows;
- Docker Desktop con contenedores Linux;
- PowerShell 7.

```powershell
git --version
docker --version
docker compose version
docker info
$PSVersionTable.PSVersion
```

## Clonar o actualizar

```powershell
git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
git pull --ff-only origin main
```

No usar `reset --hard` con cambios locales.

## Iniciar

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
```

El script crea/completa `.env`, genera secretos si corresponde, valida puertos, inicia servicios y espera health.

## Verificar

```powershell
.\scripts\Verify-Local.ps1
```

Debe informar:

- tres servicios running/healthy;
- readiness `UP`;
- diez migraciones o más;
- SPA/proxy;
- rechazo anónimo;
- login y catálogo.

## Puertos alternativos

```dotenv
POSTGRES_HOST_PORT=15432
BACKEND_HOST_PORT=18081
FRONTEND_HOST_PORT=18080
```

No cambiar los puertos internos.

## Reiniciar tras actualizar

```powershell
git status
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

## Detener

```powershell
docker compose down --remove-orphans
```

## Reset destructivo

```powershell
.\scripts\Start-Local.ps1 -Reset -Rebuild
```

Elimina datos locales, incluidos ciclos y configuración creada localmente.

## Logs

```powershell
docker compose ps --all
docker compose logs --tail 300 postgres backend frontend
```

## URLs predeterminadas

| Componente | URL |
|---|---|
| aplicación | `http://localhost:8080` |
| API | `http://localhost:8081/api` |
| Swagger | `http://localhost:8081/api/swagger-ui.html` |
| readiness | `http://localhost:8081/api/actuator/health/readiness` |

Este procedimiento es para desarrollo/evaluación, no producción.
