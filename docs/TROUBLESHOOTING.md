# Diagnóstico y resolución de problemas

## Docker o Compose no disponible

```powershell
docker info
docker compose version
```

Iniciar Docker Desktop con contenedores Linux. No omitir los scripts oficiales.

## Puerto ocupado

Cambiar solo puertos host en `.env`:

```dotenv
POSTGRES_HOST_PORT=15432
BACKEND_HOST_PORT=18081
FRONTEND_HOST_PORT=18080
```

No cambiar `postgres:5432` ni `backend:8080` internos.

## `.env` inválido

```powershell
.\scripts\Start-Local.ps1 -SkipOpen
```

El script valida placeholders, JWT Base64, secretos y puertos. No imprimir secretos.

## Login falla tras cambiar contraseña

Cambiar `.env` no cambia el hash persistido. Restaurar la contraseña original o, aceptando pérdida local:

```powershell
.\scripts\Start-Local.ps1 -Reset -Rebuild
```

## Backend no alcanza readiness

```powershell
docker compose ps --all
docker compose logs --tail 300 postgres backend
```

No desactivar Flyway ni `ddl-auto=validate`.

## Menos de diez migraciones

La versión 0.4.0 requiere `V1`–`V10`.

```powershell
docker compose exec -T postgres psql -U <usuario> -d <base> -c "select version, description, success from flyway_schema_history order by installed_rank;"
```

Reconstruir imagen y resolver el error. No editar migraciones publicadas.

## Ciclo devuelve `PRODUCTION_MACHINE_BUSY`

La máquina ya tiene ciclo `PLANNED` o `RUNNING`. Completar/cancelar el ciclo planificado o elegir otra máquina. Un ciclo iniciado no se cancela.

## Ciclo devuelve capacidad excedida

Comparar suma de pesos reales con `capacityGrams`. No reducir peso desde la UI ni fraccionar implícitamente: el modelo actual asigna pedidos completos.

## Falta evaluación vigente

`CURRENT_COMPATIBILITY_EVALUATION_REQUIRED` exige evaluar nuevamente después de modificar un perfil. Las evaluaciones históricas no se reutilizan con versiones nuevas.

## Pedido no se puede compartir

Revisar exclusividad, `effectivelyCompatible`, programa y perfiles. Una excepción no permite compartir exclusividad ni exceder capacidad.

## Clave idempotente en conflicto

Repetir exactamente máquina, programa y conjunto de pedidos. Las notas no forman parte de la identidad; otro payload con la misma clave devuelve conflicto.

## Programa usado no se puede modificar

`V10` protege parámetros técnicos. Crear una nueva versión/código de programa para cambiar semántica; no desactivar el trigger.

## `separationRequired=true`

Es una advertencia operativa, no prueba de separación. No asumir que el sistema rastrea bolsas o compartimentos.

## Diagnóstico final

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```
