# Puesta en marcha en Windows con PowerShell

Última actualización: 2026-07-21.

## 1. Requisitos

- Git para Windows.
- Docker Desktop usando contenedores Linux.
- PowerShell 7.

```powershell
git --version
docker --version
docker compose version
docker info
$PSVersionTable.PSVersion
```

## 2. Clonar o actualizar

Clonar:

```powershell
Set-Location "$HOME\Documents"
git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
```

Actualizar un clon existente:

```powershell
Set-Location 'RUTA\A\fourbubbles'
git switch main
git status
git pull --ff-only origin main
```

No uses `reset --hard` si existen cambios locales que necesitás conservar.

## 3. Habilitar scripts

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

La configuración se pierde al cerrar la terminal.

## 4. Configurar puertos

La primera ejecución crea `.env`. También podés copiar `.env.example` y editar solo los puertos antes de iniciar:

```dotenv
POSTGRES_HOST_PORT=5432
BACKEND_HOST_PORT=8081
FRONTEND_HOST_PORT=8080
```

Los tres valores deben ser enteros entre 1 y 65535 y deben ser distintos.

Cuando `5432`, `8080` o `8081` ya estén usados, elegí otros puertos, por ejemplo:

```dotenv
POSTGRES_HOST_PORT=15432
BACKEND_HOST_PORT=18081
FRONTEND_HOST_PORT=18080
```

No cambies `DB_HOST=postgres`, `DB_PORT=5432` ni la resolución `backend:8080`: son direcciones internas de la red Compose.

## 5. Construir e iniciar

```powershell
.\scripts\Start-Local.ps1 -Rebuild
```

En la primera ejecución el script:

1. crea `.env` desde `.env.example`;
2. genera contraseña PostgreSQL, secreto JWT y contraseña administrativa;
3. valida `.env` y Compose;
4. detecta conflictos de puertos antes de construir;
5. muestra contenedor, imagen, ID, PID y proceso cuando existe un conflicto;
6. inicia el stack;
7. espera health real de PostgreSQL, backend y frontend;
8. abre la aplicación.

Para no abrir el navegador:

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
```

Ejecuciones posteriores conservan secretos y valores existentes. Solo se agregan variables faltantes.

## 6. Detección de conflictos

El script distingue:

- contenedores del mismo proyecto, permitiendo reinicios idempotentes;
- contenedores ajenos, mostrando nombre, imagen, ID y publicación;
- procesos Windows, mostrando PID, nombre y ruta cuando está disponible.

No detiene automáticamente recursos ajenos. La salida indica dos opciones seguras:

1. cambiar `POSTGRES_HOST_PORT`, `BACKEND_HOST_PORT` o `FRONTEND_HOST_PORT` en `.env`;
2. detener manualmente el proceso o proyecto identificado.

Comprobación manual adicional:

```powershell
Get-NetTCPConnection -State Listen -LocalPort 5432,8080,8081 -ErrorAction SilentlyContinue |
    Select-Object LocalAddress,LocalPort,OwningProcess

docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
```

## 7. Verificar

```powershell
.\scripts\Verify-Local.ps1
```

La verificación exige:

- un contenedor para cada servicio esperado;
- `running` y `healthy` en PostgreSQL, backend y frontend;
- readiness `UP`;
- ocho migraciones Flyway exitosas o más;
- SPA servida por Nginx;
- proxy `/api` operativo;
- rechazo 401/403 sin token;
- login administrativo;
- catálogo protegido con al menos un servicio.

Los puertos se obtienen con `docker compose port`; no se asumen valores fijos.

## 8. URLs

`Start-Local.ps1` imprime las URLs efectivas. Con los valores predeterminados:

| Componente | Dirección |
|---|---|
| Aplicación | `http://localhost:8080` |
| API | `http://localhost:8081/api` |
| Swagger | `http://localhost:8081/api/swagger-ui.html` |
| Readiness | `http://localhost:8081/api/actuator/health/readiness` |
| PostgreSQL | `localhost:5432` |

## 9. Reiniciar

Reconciliar la configuración existente:

```powershell
.\scripts\Start-Local.ps1
.\scripts\Verify-Local.ps1
```

Reconstruir después de actualizar código:

```powershell
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

## 10. Detener y limpiar

Detener preservando datos:

```powershell
docker compose down --remove-orphans
```

Eliminar también todos los datos locales:

```powershell
docker compose down -v --remove-orphans
```

Recrear todo desde el script:

```powershell
.\scripts\Start-Local.ps1 -Reset -Rebuild
```

`-Reset` es destructivo: elimina usuarios, clientes, pedidos, recepciones, perfiles, evaluaciones, pagos y auditoría locales.

Para regenerar además todas las credenciales:

```powershell
docker compose down -v --remove-orphans
Remove-Item -LiteralPath '.env' -Force
.\scripts\Start-Local.ps1 -Rebuild
```

## 11. Recuperación ante inicio parcial

Cuando `docker compose up` o una validación posterior falla, `Start-Local.ps1`:

1. imprime `docker compose ps --all`;
2. imprime logs recientes de PostgreSQL, backend y frontend;
3. ejecuta `docker compose down --remove-orphans`;
4. conserva el volumen PostgreSQL;
5. termina con error y nunca imprime éxito.

Para reintentar:

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
```

Si el volumen quedó incompatible con credenciales cambiadas, la salida correcta es recrearlo conscientemente:

```powershell
.\scripts\Start-Local.ps1 -Reset -Rebuild
```

## 12. Logs

```powershell
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
docker compose logs -f backend
```

Salir con `Ctrl+C` no detiene los servicios.

## 13. Convivencia con otros proyectos Docker

- `COMPOSE_PROJECT_NAME=fourbubbles` separa nombres, red y volumen.
- Los puertos se publican solo en `127.0.0.1`.
- Cambiar puertos host no altera la red interna.
- `down` afecta únicamente este proyecto Compose.
- El script nunca ejecuta `docker stop`, `docker rm` ni `Stop-Process` contra recursos ajenos.

## 14. Alcance

Este procedimiento es para desarrollo y evaluación funcional. No constituye un despliegue productivo.
