# Puesta en marcha en Windows con PowerShell

Versión: `0.3.0`.

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

Docker Desktop debe estar iniciado antes de continuar.

## 2. Clonar

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

No uses `reset --hard` si existen cambios locales que necesitás conservar.

## 4. Habilitar scripts en la terminal actual

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

La configuración se pierde al cerrar la terminal.

## 5. Construir e iniciar

```powershell
.\scripts\Start-Local.ps1 -Rebuild
```

En la primera ejecución el script:

- crea `.env`;
- genera contraseña PostgreSQL;
- genera secreto JWT;
- genera contraseña de administrador;
- construye imágenes;
- inicia PostgreSQL, backend y frontend;
- espera readiness.

Guardá las credenciales mostradas. `.env` no debe subirse al repositorio.

## 6. Verificar

```powershell
.\scripts\Verify-Local.ps1
```

Para 0.3.0 se espera:

```text
Verificación local exitosa.
Salud: UP
Migraciones exitosas: 8
Frontend, autenticación y API protegida: OK
```

El script admite más de ocho migraciones para versiones futuras.

## 7. Abrir

```powershell
Start-Process 'http://localhost:8080'
Start-Process 'http://localhost:8081/api/swagger-ui.html'
```

| Componente | Dirección |
|---|---|
| Aplicación | `http://localhost:8080` |
| API | `http://localhost:8081/api` |
| Swagger | `http://localhost:8081/api/swagger-ui.html` |
| Health | `http://localhost:8081/api/actuator/health` |

## 8. Comprobar contenedores

```powershell
docker compose ps
```

Deben aparecer `postgres`, `backend` y `frontend` saludables o iniciados.

## 9. Logs

```powershell
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
```

Seguimiento:

```powershell
docker compose logs -f backend
```

Salir con `Ctrl+C` no detiene los servicios.

## 10. Detener y reiniciar

Detener conservando datos:

```powershell
docker compose down
```

Reiniciar:

```powershell
docker compose up -d
.\scripts\Verify-Local.ps1
```

## 11. Actualizar aplicación

```powershell
git switch main
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Flyway aplica las migraciones pendientes automáticamente.

## 12. Eliminar todo el entorno local

```powershell
docker compose down -v --remove-orphans
```

Esto elimina toda la base local: usuarios, clientes, pedidos, recepciones, perfiles, evaluaciones, pagos y auditoría.

Para regenerar también credenciales:

```powershell
docker compose down -v --remove-orphans
Remove-Item -LiteralPath '.env' -Force
.\scripts\Start-Local.ps1 -Rebuild
```

## 13. Validación manual de compatibilidad

Se necesitan dos pedidos `CLASSIFIED` con recepción.

1. Abrir **Pedidos**.
2. Entrar en **Compatibilidad** del primer pedido.
3. Guardar su perfil.
4. Repetir para el segundo pedido.
5. Volver al primero.
6. Seleccionar el segundo como candidato.
7. Ejecutar **Evaluar compatibilidad**.
8. Revisar razones y recomendación.
9. Como `ADMIN`, probar una excepción solo si el resultado original es incompatible.

## 14. Problemas frecuentes

### Docker no disponible

```powershell
docker info
```

Iniciar Docker Desktop y confirmar modo Linux.

### Puerto ocupado

```powershell
Get-NetTCPConnection -LocalPort 8080,8081 -ErrorAction SilentlyContinue
```

Detener el proceso conflictivo o ajustar la configuración local.

### Contraseña administrativa no funciona

Si cambiaste `.env` después de crear el volumen, el usuario existente conserva la contraseña anterior. Restaurá la anterior o recreá el volumen.

### Menos de ocho migraciones

```powershell
docker compose logs --tail 500 backend
```

No edites una migración aplicada. Corregí mediante una migración nueva.

### Pantalla de compatibilidad rechazada

Confirmá:

- pedido en `CLASSIFIED`;
- recepción existente;
- usuario `ADMIN` u `OPERATOR` para guardar/evaluar;
- ambos pedidos con perfil.

## 15. Alcance local

Este procedimiento es para desarrollo y evaluación funcional. No constituye un despliegue productivo.
