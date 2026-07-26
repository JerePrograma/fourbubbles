# Contrato API

Versión funcional: `0.4.2`. Fuente de verdad: controladores y DTO bajo `backend/src/main/java/ar/com/ropalista`.

## Convenciones

- context path: `/api`;
- Swagger: `/api/swagger-ui.html`;
- autenticación: `Authorization: Bearer <ACCESS_TOKEN>`;
- éxito: `ApiResponse.ok(data)`;
- error: código de negocio, mensaje seguro, status, path, timestamp y violaciones.

## Rutas principales

| Módulo | Rutas | Escritura |
|---|---|---|
| autenticación | `/auth/login`, `/refresh`, `/logout` | sesión |
| clientes/pedidos | `/clients`, `/orders` | según endpoint |
| recepción | `/orders/{id}/reception`, `/decision` | `ADMIN`/`OPERATOR` |
| compatibilidad | perfiles, `/compatibility/evaluate`, excepción | excepción solo `ADMIN` |
| producción | `/production/machines`, `/programs`, `/cycles` | configuración `ADMIN`; operación `ADMIN`/`OPERATOR` |
| pagos/auditoría | `/payments`, `/audit` | auditoría solo `ADMIN` |

## Ciclos

`POST /production/cycles` requiere `Idempotency-Key` de 8–120 caracteres y uno o dos pedidos. Una carga compartida exige evaluación vigente y no exclusividad. Si se habilita mediante excepción, las asignaciones responden con `separationRequired=true`.

Estados:

```text
PLANNED → RUNNING → COMPLETED
PLANNED → CANCELLED
```

## Separación física

Base: `/production/cycles/{cycleId}/separations`.

| Método | Ruta | Acceso | Efecto |
|---|---|---|---|
| `GET` | `/production/cycles/{cycleId}/separations` | cuatro roles | lista asignaciones que requieren separación |
| `PUT` | `/production/cycles/{cycleId}/separations/{orderId}` | `ADMIN`/`OPERATOR` | confirma el contenedor físico |

Request:

```json
{"containerCode":"BAG-001"}
```

Reglas:

- solo ciclos `PLANNED`;
- el pedido debe pertenecer al ciclo y requerir separación;
- código normalizado a mayúsculas;
- 3–80 caracteres `[A-Za-z0-9._:-]`;
- contenedor único dentro del ciclo;
- repetir el mismo código sobre la misma asignación es idempotente;
- un código diferente después de confirmar devuelve conflicto;
- el ciclo no inicia mientras exista una separación requerida sin confirmar.

Respuesta:

```json
{
  "cycleId":"UUID",
  "cycleNumber":"PC-000001",
  "orderId":"UUID",
  "orderNumber":"RL-000001",
  "separationRequired":true,
  "containerCode":"BAG-001",
  "confirmedAt":"2026-07-26T12:00:00-03:00",
  "confirmedBy":"operator"
}
```

## Métricas productivas

`GET /production/metrics?from=<ISO_OFFSET_DATE_TIME>&to=<ISO_OFFSET_DATE_TIME>`

Acceso: los cuatro roles. Ambos parámetros son opcionales; por defecto se consultan 30 días y el máximo permitido es 366 días. El intervalo es `[from,to)` por `production_cycles.created_at`.

La respuesta incluye estados, lavados/secados completados, cargas compartidas, separación requerida/pendiente, pedidos asignados, pesos, duración media y porcentajes de finalización/preparación. Solo ciclos completados aportan peso real y duración.

Errores: `INVALID_METRICS_RANGE`, `METRICS_RANGE_TOO_LARGE`. No calcula costos ni capacidad histórica.

## Control de calidad

`PATCH /production/orders/{orderId}/quality-control`:

- `PASS → FOLDING`;
- `REWASH → REWASH_REQUIRED`.

## Errores productivos relevantes

`PRODUCTION_MACHINE_BUSY`, `PRODUCTION_MACHINE_CAPACITY_EXCEEDED`, `PROGRAM_NOT_ALLOWED_FOR_ORDER`, `CURRENT_COMPATIBILITY_EVALUATION_REQUIRED`, `EXCLUSIVE_ORDER_CANNOT_SHARE_CYCLE`, `PRODUCTION_CYCLE_NOT_STARTABLE`, `PRODUCTION_CYCLE_ALREADY_STARTED`, `PRODUCTION_CYCLE_ORDER_NOT_FOUND`, `SEPARATION_NOT_REQUIRED`, `SEPARATION_CONTAINER_ALREADY_USED`, `SEPARATION_ALREADY_CONFIRMED`.
