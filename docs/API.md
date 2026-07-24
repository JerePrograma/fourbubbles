# Contrato API

Versión funcional: `0.4.0`. Fuente de verdad: controladores y DTO bajo `backend/src/main/java/ar/com/ropalista`.

## Convenciones

- context path: `/api`;
- Swagger: `/api/swagger-ui.html`;
- OpenAPI: `/api/v3/api-docs`;
- autenticación: `Authorization: Bearer <ACCESS_TOKEN>`;
- refresh: cookie `ropa_lista_refresh`, `HttpOnly`, `SameSite=Strict`;
- éxito: `ApiResponse.ok(data)`;
- error: código de negocio, mensaje seguro, status, path, timestamp y violaciones.

## Rutas existentes

| Módulo | Rutas principales | Escritura |
|---|---|---|
| autenticación | `/auth/login`, `/auth/refresh`, `/auth/logout` | contrato de sesión |
| catálogo | `/catalog/services`, `/catalog/equivalences` | lectura autenticada |
| clientes | `/clients`, `/clients/{id}`, domicilios | `ADMIN`/`OPERATOR` |
| pedidos | `/orders`, planificación, cotización, precio, estado | según operación |
| recepción | `/orders/{id}/reception`, `/decision` | `ADMIN`/`OPERATOR` |
| compatibilidad | perfiles, `/compatibility/evaluate`, excepción | `ADMIN`/`OPERATOR`; excepción solo `ADMIN` |
| pagos | `/payments` | `ADMIN`/`OPERATOR` |
| auditoría | `/audit` | solo `ADMIN` |

## Producción

Base: `/production`.

### Máquinas

| Método | Ruta | Acceso | Contrato |
|---|---|---|---|
| `POST` | `/machines` | `ADMIN` | crea `MachineRequest` |
| `PUT` | `/machines/{id}` | `ADMIN` | actualiza nombre, capacidad, estado, activo y notas; código/tipo inmutables |
| `GET` | `/machines` | cuatro roles | lista máquinas |

`MachineRequest`: `code`, `name`, `machineType`, `capacityGrams`, `status`, `active`, `notes`.

### Programas

| Método | Ruta | Acceso | Contrato |
|---|---|---|---|
| `POST` | `/programs` | `ADMIN` | crea programa |
| `PUT` | `/programs/{id}` | `ADMIN` | actualiza programa; código/etapa inmutables |
| `GET` | `/programs?stage=WASH|DRY` | cuatro roles | lista o filtra activos por etapa |

`ProgramRequest`: `code`, `name`, `stage`, `durationMinutes`, `maxTemperatureC`, `gentle`, `usesSoftener`, `fragrancePolicy`, `active`, `notes`.

Lavado exige temperatura y fragancia. Secado no admite temperatura de lavado, fragancia ni suavizante.

### Ciclos

| Método | Ruta | Acceso | Efecto |
|---|---|---|---|
| `POST` | `/cycles` | `ADMIN`/`OPERATOR` | planifica ciclo idempotente |
| `GET` | `/cycles/{id}` | cuatro roles | detalle e historial |
| `GET` | `/cycles?status=&stage=&page=&size=` | cuatro roles | búsqueda paginada |
| `POST` | `/cycles/{id}/start` | `ADMIN`/`OPERATOR` | `PLANNED → RUNNING` |
| `POST` | `/cycles/{id}/complete` | `ADMIN`/`OPERATOR` | `RUNNING → COMPLETED` |
| `POST` | `/cycles/{id}/cancel` | `ADMIN`/`OPERATOR` | `PLANNED → CANCELLED` |

Header obligatorio al crear:

```http
Idempotency-Key: cycle-<identificador>
```

Longitud: 8–120 caracteres.

`CreateCycleRequest`:

```json
{
  "machineId": "UUID",
  "programId": "UUID",
  "orderIds": ["UUID"],
  "notes": "opcional"
}
```

Admite uno o dos pedidos distintos. La repetición con misma clave/máquina/programa/conjunto devuelve el mismo ciclo. Una carga diferente con la misma clave devuelve conflicto.

`CompleteCycleRequest` exige `actualWeightGrams > 0`.

### Control de calidad

`PATCH /production/orders/{orderId}/quality-control`

Acceso: `ADMIN`/`OPERATOR`.

```json
{"decision":"PASS","observation":"Resultado conforme"}
```

- `PASS` → `FOLDING`;
- `REWASH` → `REWASH_REQUIRED`.

## Precondiciones productivas

- máquina activa, disponible y del tipo requerido;
- programa activo y permitido por cada perfil;
- peso real de recepción;
- capacidad suficiente;
- pedido listo para etapa;
- sin asignación activa de la misma etapa;
- dos pedidos: perfiles no exclusivos y evaluación vigente `effectivelyCompatible=true`.

## Errores relevantes

`PRODUCTION_MACHINE_NOT_FOUND`, `PRODUCTION_PROGRAM_NOT_FOUND`, `PRODUCTION_CYCLE_NOT_FOUND`, `PRODUCTION_MACHINE_BUSY`, `PRODUCTION_MACHINE_UNAVAILABLE`, `PRODUCTION_MACHINE_CAPACITY_EXCEEDED`, `PROGRAM_NOT_ALLOWED_FOR_ORDER`, `ORDER_ALREADY_ASSIGNED_TO_ACTIVE_CYCLE`, `CURRENT_COMPATIBILITY_EVALUATION_REQUIRED`, `ORDERS_NOT_EFFECTIVELY_COMPATIBLE`, `EXCLUSIVE_ORDER_CANNOT_SHARE_CYCLE`, `IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD`, `ORDER_NOT_IN_QUALITY_CONTROL`.
