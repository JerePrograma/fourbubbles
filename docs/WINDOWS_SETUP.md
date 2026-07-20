# Puesta en marcha en Windows con PowerShell

Versión: `0.1.2`.

Este es el camino recomendado para desarrollo y evaluación funcional.

## 1. Requisitos

Instalar:

- Git para Windows;
- Docker Desktop;
- PowerShell 7.

Docker Desktop debe usar contenedores Linux.

Verificar:

```powershell
git --version
docker --version
docker compose version
docker info
$PSVersionTable.PSVersion
```

## 2. Clonar por primera vez

```powershell
Set-Location "$HOME\Documents"
git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
git pull --ff-only origin main
```

## 3. Actualizar un clon existente

```powershell
Set-Location 'RUTA\A\fourbubbles'
git switch main
git status
git pull --ff-only origin main
```

Si `git status` muestra cambios locales, no los destruyas. Guardalos en un commit, stash o copia antes de actualizar.

## 4. Habilitar scripts en la terminal actual

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

La configuración desaparece al cerrar esa terminal.

## 5. Iniciar el sistema

```powershell
.\scripts\Start-Local.ps1 -Rebuild
```

Primera ejecución:

- crea `.env` desde `.env.example`;
- genera contraseña PostgreSQL;
- genera JWT secret;
- genera contraseña administrativa;
- construye imágenes;
- inicia servicios;
- espera health.

El script mostrará:

```text
Usuario: admin
Contraseña: <generada>
```

Guardá esa contraseña. Solo se muestra cuando `.env` se crea.

## 6. Verificar el stack

```powershell
.\scripts\Verify-Local.ps1
```

Salida esperada:

```text
Verificación local exitosa.
Salud: UP
Migraciones exitosas: 6
Servicios disponibles: <cantidad>
Frontend, autenticación y API protegida: OK
```

La verificación usa las credenciales de `.env` pero no las imprime.

## 7. Abrir la aplicación

```powershell
Start-Process 'http://localhost:8080'
Start-Process 'http://localhost:8081/api/swagger-ui.html'
```

| Componente | URL |
|---|---|
| Aplicación | `http://localhost:8080` |
| API | `http://localhost:8081/api` |
| Swagger | `http://localhost:8081/api/swagger-ui.html` |
| Health | `http://localhost:8081/api/actuator/health` |

## 8. Consultar contenedores

```powershell
docker compose ps
```

Deben existir:

- `postgres`;
- `backend`;
- `frontend`.

## 9. Logs

```powershell
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
```

Seguimiento en vivo:

```powershell
docker compose logs -f backend
```

Salir con `Ctrl+C` no detiene los servicios.

## 10. Detener sin borrar datos

```powershell
docker compose down
```

Reiniciar:

```powershell
docker compose up -d
```

## 11. Actualizar versión

```powershell
git switch main
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

## 12. Reinicio destructivo

```powershell
docker compose down -v --remove-orphans
.\scripts\Start-Local.ps1 -Rebuild
```

Esto elimina todos los datos locales.

Para regenerar también `.env`:

```powershell
docker compose down -v --remove-orphans
Remove-Item -LiteralPath '.env' -Force
.\scripts\Start-Local.ps1 -Rebuild
```

## 13. Problema: cambió la contraseña pero no puedo entrar

`APP_DEV_ADMIN_PASSWORD` solo se utiliza al crear el administrador. Cambiar `.env` después no actualiza el hash existente.

Opciones:

1. restaurar la contraseña usada originalmente;
2. conservar los datos y modificar el usuario mediante un procedimiento administrativo futuro;
3. en desarrollo descartable, recrear el volumen.

## 14. Problema: el backend no queda saludable

```powershell
docker compose ps
docker compose logs --tail 300 backend
docker compose logs --tail 300 postgres
docker compose config --quiet
```

Verificar:

- Docker Desktop iniciado;
- puertos 5432, 8080 y 8081 libres;
- `POSTGRES_PASSWORD` igual a `DB_PASSWORD`;
- JWT secret válido;
- memoria suficiente;
- migraciones sin fallos.

## 15. Validación técnica opcional fuera de Docker

Backend:

```powershell
Set-Location '.\backend'
mvn clean verify
```

Frontend:

```powershell
Set-Location '..\frontend'
npm ci
npm run lint
npm test
npm run build
```

Requiere Java 21, Maven 3.9 y Node 22 instalados localmente.

## 16. Seguridad

No compartir:

- `.env`;
- contraseñas;
- JWT;
- cookies;
- dumps con datos reales;
- logs que contengan información personal.

`.env` está ignorado por Git y debe permanecer así.
