# Operación y despliegue

Versión: `0.3.0`.

## Perfiles

- `dev`: administrador inicial y logs de desarrollo.
- `test`: Testcontainers, Flyway y validación JPA.
- `prod`: cookies seguras, logs JSON y SQL deshabilitado.

Compose usa `dev`; no es una topología productiva.

## Servicios locales

| Servicio | Puerto | Responsabilidad |
|---|---:|---|
| frontend | 8080 | Nginx + SPA React |
| backend | 8081 | API Spring bajo `/api` |
| postgres | interno | PostgreSQL 16 |

## Inicio recomendado

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

`Start-Local.ps1` crea `.env` cuando no existe, genera secretos locales y espera readiness.

`Verify-Local.ps1` valida:

1. contenedores;
2. health del backend;
3. ocho migraciones Flyway o más;
4. SPA;
5. login;
6. API protegida.

## Actualización

```powershell
git switch main
git pull --ff-only origin main
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Flyway aplica V8 automáticamente. No editar migraciones ya publicadas.

## Logs

```powershell
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
docker compose logs -f backend
```

En producción los logs del backend son JSON y deben enviarse a una plataforma central.

## Detención y reinicio

Detener conservando datos:

```powershell
docker compose down
```

Reiniciar:

```powershell
docker compose up -d
```

Eliminar entorno y datos:

```powershell
docker compose down -v --remove-orphans
```

## Verificación funcional mínima

Después de una actualización:

1. iniciar sesión;
2. consultar catálogo;
3. crear o consultar un cliente;
4. consultar un pedido existente;
5. confirmar que recepción carga;
6. en un pedido `CLASSIFIED`, guardar perfil de compatibilidad;
7. evaluar contra otro pedido clasificado con perfil;
8. verificar auditoría como `ADMIN`.

## Compatibilidad operativa

- El perfil solo se modifica en `CLASSIFIED`.
- Los dos pedidos se bloquean por UUID durante una evaluación.
- Una evaluación existente se reutiliza si las versiones no cambiaron.
- Una excepción requiere `ADMIN` y motivo.
- Compatibilidad no asigna máquinas ni cambia estados.

## Health y diagnóstico

```powershell
Invoke-RestMethod 'http://localhost:8081/api/actuator/health'
docker compose ps
.\scripts\Verify-Local.ps1
```

Ante error de migración:

- revisar logs de backend;
- consultar `flyway_schema_history`;
- no borrar una migración aplicada;
- corregir mediante una migración nueva.

## Backups

No existe automatización productiva. Antes de usar datos reales se requiere:

- backup programado;
- cifrado;
- retención;
- restauración ensayada;
- RPO/RTO definidos;
- procedimiento de rollback de aplicación compatible con migraciones aditivas.

## Evidencias

La base solo almacena metadata. El futuro object storage debe ofrecer:

- buckets privados;
- cifrado;
- URLs temporales;
- validación MIME/tamaño/hash;
- política de retención y borrado;
- trazabilidad de acceso.

## Checklist previo a producción

- [ ] dominio y TLS;
- [ ] perfil `prod` verificado;
- [ ] secretos administrados;
- [ ] CORS/cookies revisados;
- [ ] backups y restore probados;
- [ ] object storage privado;
- [ ] observabilidad y alertas;
- [ ] límites CPU/memoria;
- [ ] rate limit distribuido;
- [ ] política de privacidad;
- [ ] rollback ensayado;
- [ ] smoke post-deploy.
