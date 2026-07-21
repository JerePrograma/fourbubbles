# Contrato API

Versión documentada: `0.4.0`.

La API se sirve bajo `/api`. Swagger local: `/api/swagger-ui.html`.

## Convenciones

Éxito:

```json
{"success":true,"data":{},"timestamp":"2026-07-20T20:00:00-03:00"}
```

Error:

```json
{"success":false,"code":"BUSINESS_CODE","message":"Descripción segura","status":422,"path":"/api/...","timestamp":"2026-07-20T20:00:00-03:00","violations":[]}
```

Las rutas protegidas requieren `Authorization: Bearer <token>`.

## Núcleo existente

| Área | Rutas principales |
|---|---|
| Auth | `/auth/login`, `/auth/refresh`, `/auth/logout` |
| Clientes | `/clients...` |
| Pedidos | `/orders...` |
| Recepción | `/orders/{id}/reception...` |
| Compatibilidad | `/orders/{id}/compatibility-profile`, `/compatibility...` |
| Pagos | `/payments...` |
| Auditoría | `/audit` |

## Producción

### Máquinas

| Método | Ruta | Rol | Uso |
|---|---|---|---|
| GET | `/production/machines` | todos | listar |
| POST | `/production/machines` | ADMIN | crear |
| PUT | `/production/machines/{id}` | ADMIN | actualizar |

Ejemplo de request:

```json
{
  "code":"WASHER_02",
  "name":"Lavadora secundaria",
  "machineType":"WASHER",
  "capacityGrams":10000,
  "status":"ACTIVE",
  "active":true,
  "notes":null
}
```

Código y tipo son inmutables después de crear. No se actualiza una máquina con ciclo activo.

### Programas

| Método | Ruta | Rol | Uso |
|---|---|---|---|
| GET | `/production/programs?stage=WASH` | todos | listar/filtrar |
| POST | `/production/programs` | ADMIN | crear |
| PUT | `/production/programs/{id}` | ADMIN | actualizar |

Programa WASH:

```json
{
  "code":"WASH_30_NONE",
  "name":"Lavado 30 sin fragancia",
  "stage":"WASH",
  "durationMinutes":45,
  "maxTemperatureC":30,
  "gentle":false,
  "usesSoftener":false,
  "fragrancePolicy":"NONE",
  "active":true,
  "notes":null
}
```

Programa DRY usa `maxTemperatureC=null`, `fragrancePolicy=null` y `usesSoftener=false`.

Código/etapa son inmutables. Tras el primer uso también quedan congelados duración, temperatura, gentle, suavizante, fragancia y tipo de máquina.

### Planificar ciclo

`POST /production/cycles`

Roles: `ADMIN`, `OPERATOR`.

Header obligatorio:

```http
Idempotency-Key: web-cycle-550e8400-e29b-41d4-a716-446655440000
```

```json
{
  "machineId":"94000000-0000-0000-0000-000000000001",
  "programId":"95000000-0000-0000-0000-000000000002",
  "orderIds":[
    "11111111-1111-1111-1111-111111111111",
    "22222222-2222-2222-2222-222222222222"
  ],
  "notes":"Separar mediante bolsas identificadas"
}
```

La misma clave con la misma máquina, programa y conjunto de pedidos devuelve el ciclo existente. Reusarla con otra identidad devuelve `IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD`. Las notas no forman parte de la identidad actual.

Precondiciones:

- 1 o 2 pedidos distintos;
- máquina activa/libre;
- programa activo y del tipo correcto;
- etapa válida del pedido;
- perfil y peso real;
- capacidad suficiente;
- sin asignación activa de la etapa;
- para dos pedidos, evaluación vigente efectivamente compatible;
- ningún pedido exclusivo.

### Consultar ciclos

| Método | Ruta | Rol |
|---|---|---|
| GET | `/production/cycles/{id}` | todos |
| GET | `/production/cycles?status=PLANNED&stage=WASH&page=0&size=20` | todos |

La respuesta contiene máquina, programa, pesos, fechas, pedidos, separación requerida e historial.

### Operar ciclo

`POST /production/cycles/{id}/start`

```json
{"observation":"Carga verificada"}
```

`POST /production/cycles/{id}/complete`

```json
{"actualWeightGrams":5200,"observation":"Ciclo finalizado"}
```

`POST /production/cycles/{id}/cancel`

```json
{"observation":"Máquina reservada para mantenimiento"}
```

Solo se cancela `PLANNED`. Solo se inicia `PLANNED`. Solo se completa `RUNNING`.

### Control de calidad

`PATCH /production/orders/{orderId}/quality-control`

```json
{"decision":"PASS","observation":"Sin manchas ni olor residual"}
```

Decisiones:

- `PASS` → `FOLDING`;
- `REWASH` → `REWASH_REQUIRED`.

## Códigos relevantes

| Código | Estado | Significado |
|---|---:|---|
| `IDEMPOTENCY_KEY_REQUIRED` | 400 | falta header |
| `INVALID_IDEMPOTENCY_KEY` | 400 | longitud inválida |
| `IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD` | 409 | clave usada con otra identidad |
| `PRODUCTION_MACHINE_NOT_FOUND` | 404 | máquina inexistente |
| `PRODUCTION_MACHINE_UNAVAILABLE` | 422 | fuera de servicio/inactiva |
| `PRODUCTION_MACHINE_BUSY` | 409 | posee ciclo activo |
| `PRODUCTION_MACHINE_CAPACITY_EXCEEDED` | 422 | supera capacidad |
| `PRODUCTION_MACHINE_PROGRAM_MISMATCH` | 422 | programa de otro tipo |
| `ORDER_NOT_READY_FOR_PRODUCTION_STAGE` | 422 | estado inválido |
| `ORDER_ALREADY_ASSIGNED_TO_ACTIVE_CYCLE` | 409 | asignación activa |
| `PROGRAM_NOT_ALLOWED_FOR_ORDER` | 422 | contradice perfil |
| `CURRENT_COMPATIBILITY_EVALUATION_REQUIRED` | 422 | falta evaluación vigente |
| `ORDERS_NOT_EFFECTIVELY_COMPATIBLE` | 422 | no compatibles |
| `EXCLUSIVE_ORDER_CANNOT_SHARE_CYCLE` | 422 | exclusividad |
| `ORDER_NOT_IN_QUALITY_CONTROL` | 422 | control de calidad fuera de etapa |

## Fuera del contrato

No existen todavía endpoints completos de rutas, caja/costos, inventario, mantenimiento detallado, reclamos ni almacenamiento binario.
