# Puesta en marcha en Windows con PowerShell

Versión: `0.2.0`.

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

## 2. Clonar

```powershell
Set-Location "$HOME\Documents"
git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
git pull --ff-only origin main
```

## 3. Actualizar un clon

```powershell
Set-Location 'RUTA\A\fourbubbles'
git switch main
git status
git pull --ff-only origin main
```

No destruyas cambios locales sin respaldarlos.

## 4. Habilitar scripts

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

## 5. Iniciar

```powershell
.\scripts\Start-Local.ps1 -Rebuild
```

Primera ejecución:

- crea `.env`;
- genera contraseña PostgreSQL;
- genera JWT secret;
- genera contraseña administrativa;
- construye imágenes;
- inicia servicios;
- espera health;
- muestra la contraseña inicial una vez.

## 6. Verificar

```powershell
.\scripts\Verify-Local.ps1
```

Resultado esperado:

```text
Verificación local exitosa.
Salud: UP
Migraciones exitosas: 7
Servicios disponibles: <cantidad>
Frontend, autenticación y API protegida: OK
```

## 7. Abrir

```powershell
Start-Process 'http://localhost:8080'
Start-Process 'http://localhost:8081/api/swagger-ui.html'
```

| Componente | URL |
|---|---|
| App | `http://localhost:8080` |
| API | `http://localhost:8081/api` |
| Swagger | `http://localhost:8081/api/swagger-ui.html` |
| Health | `http://localhost:8081/api/actuator/health` |

## 8. Contenedores y logs

```powershell
docker compose ps
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
```

En vivo:

```powershell
docker compose logs -f backend
```

## 9. Detener/reiniciar

```powershell
docker compose down
docker compose up -d
```

## 10. Actualizar versión

```powershell
git switch main
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

V7 se aplica automáticamente.

## 11. Reinicio destructivo

```powershell
docker compose down -v --remove-orphans
.\scripts\Start-Local.ps1 -Rebuild
```

Para regenerar `.env`:

```powershell
docker compose down -v --remove-orphans
Remove-Item -LiteralPath '.env' -Force
.\scripts\Start-Local.ps1 -Rebuild
```

Borra todos los datos locales.

## 12. Contraseña administrativa

Cambiar `APP_DEV_ADMIN_PASSWORD` después de crear la base no actualiza el usuario persistido.

Opciones:

- restaurar contraseña original;
- recrear el volumen en desarrollo descartable;
- esperar/implementar administración de usuarios.

## 13. Probar recepción

1. Iniciar sesión.
2. Crear cliente/pedido.
3. Confirmar precio.
4. Cambiar estados hasta `PICKED_UP`.
5. En **Pedidos**, abrir **Recibir**.
6. Informar peso/conteo real.
7. Registrar daños/manchas.
8. Opcionalmente informar metadata de un archivo externo.
9. Registrar.
10. Aprobar/rechazar si queda pendiente.

El navegador genera una `Idempotency-Key` estable para el formulario. No recargues deliberadamente durante una operación sin comprobar el resultado; si hubo timeout, la misma instancia de formulario reutiliza la clave.

## 14. Evidencia externa

Los campos de evidencia no suben archivos. Requieren previamente:

- `objectKey` real;
- nombre;
- MIME;
- tamaño;
- SHA-256.

Para una demo sin almacenamiento externo, dejarlos vacíos. No inventar datos y presentarlos como fotografía almacenada.

## 15. Backend no saludable

```powershell
docker compose ps
docker compose logs --tail 300 backend
docker compose logs --tail 300 postgres
docker compose config --quiet
```

Verificar puertos 5432/8080/8081, contraseñas DB coherentes, JWT válido, memoria y Flyway.

## 16. Validación fuera de Docker

```powershell
Set-Location '.\backend'
mvn clean verify

Set-Location '..\frontend'
npm ci
npm run lint
npm test
npm run build
```

Requiere Java 21, Maven y Node 22 locales.

## 17. Seguridad

No compartir:

- `.env`;
- contraseñas/tokens/cookies;
- dumps reales;
- hashes o object keys privados de evidencias;
- logs con información personal.
