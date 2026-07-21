# Contrato API

Versión documentada: `0.3.0`.

La API se sirve bajo `/api`. Swagger local: `/api/swagger-ui.html`.

## Convenciones

Éxito:

```json
{"success":true,"data":{},"timestamp":"2026-07-20T20:00:00-03:00"}
```

Error:

```json
{
  "success": false,
  "code": "BUSINESS_CODE",
  "message": "Descripción segura",
  "status": 422,
  "path": "/api/...",
  "timestamp": "2026-07-20T20:00:00-03:00",
  "violations": []
}
```

Las rutas protegidas requieren `Authorization: Bearer <access-token>`. El refresh token viaja en cookie `HttpOnly`.

## Autenticación

| Método | Ruta | Uso |
|---|---|---|
| POST | `/auth/login` | autenticar y emitir sesión |
| POST | `/auth/refresh` | rotar refresh y emitir access token |
| POST | `/auth/logout` | revocar sesión actual |

## Clientes y domicilios

| Método | Ruta | Uso |
|---|---|---|
| POST | `/clients` | crear cliente con domicilio principal |
| GET | `/clients` | búsqueda paginada |
| GET | `/clients/{id}` | detalle |
| PUT | `/clients/{id}` | actualizar perfil/preferencias |
| POST | `/clients/{id}/addresses` | agregar domicilio |
| PUT | `/clients/{id}/addresses/{addressId}/primary` | cambiar principal |
| DELETE | `/clients/{id}/addresses/{addressId}` | baja lógica |

## Pedidos

| Método | Ruta | Uso |
|---|---|---|
| POST | `/orders` | crear pedido declarado |
| GET | `/orders` | buscar por número, cliente o estado |
| GET | `/orders/{id}` | detalle |
| PATCH | `/orders/{id}/planning` | retiro, promesa y notas tempranas |
| POST | `/orders/{id}/manual-quote` | cotización manual `ADMIN` |
| POST | `/orders/{id}/confirm-price` | confirmar precio y promoción |
| PATCH | `/orders/{id}/status` | transición permitida |

## Recepción

### Registrar

`POST /orders/{id}/reception`

Header obligatorio:

```http
Idempotency-Key: web-reception-550e8400-e29b-41d4-a716-446655440000
```

Ejemplo:

```json
{
  "receivedAt": "2026-07-20T18:00:00-03:00",
  "actualWeightGrams": 2350,
  "bagCode": "BAG-001",
  "notes": "Recepción sin novedades",
  "items": [
    {
      "equivalenceCode": "TSHIRT",
      "physicalPieces": 4,
      "damaged": false,
      "stained": false,
      "observations": null
    }
  ],
  "evidences": []
}
```

La misma clave devuelve el mismo agregado. Una clave diferente no puede crear una segunda recepción para el pedido.

### Consultar y decidir

| Método | Ruta | Uso |
|---|---|---|
| GET | `/orders/{id}/reception` | consultar snapshot real |
| POST | `/orders/{id}/reception/decision` | `APPROVED` o `REJECTED` |

## Compatibilidad

### Guardar perfil

`PUT /orders/{orderId}/compatibility-profile`

Permisos: `ADMIN` u `OPERATOR`.

Precondiciones:

- pedido `CLASSIFIED`;
- recepción existente.

```json
{
  "colorGroup": "LIGHT",
  "materialGroup": "COTTON",
  "maxTemperatureC": 40,
  "dryerAllowed": true,
  "fragrancePolicy": "STANDARD",
  "softenerAllowed": true,
  "hypoallergenic": false,
  "babyClothes": false,
  "petContact": false,
  "heavySoil": false,
  "exclusiveCycle": false,
  "notes": null
}
```

El backend devuelve el perfil efectivo. Puede endurecer el request según cliente/pedido.

`GET /orders/{orderId}/compatibility-profile` permite consulta a los cuatro roles y devuelve `null` cuando aún no existe perfil.

### Evaluar dos pedidos

`POST /compatibility/evaluate`

```json
{
  "orderAId": "11111111-1111-1111-1111-111111111111",
  "orderBId": "22222222-2222-2222-2222-222222222222"
}
```

Permisos: `ADMIN` u `OPERATOR`.

Respuesta principal:

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "orderAId": "11111111-1111-1111-1111-111111111111",
  "orderBId": "22222222-2222-2222-2222-222222222222",
  "profileAVersion": 0,
  "profileBVersion": 0,
  "ruleVersion": "COMPAT-1",
  "compatible": false,
  "overridden": false,
  "effectivelyCompatible": false,
  "reasons": [
    {
      "code": "COLOR_GROUP_MISMATCH",
      "severity": "HARD",
      "message": "Los grupos de color no coinciden"
    }
  ],
  "recommendation": {
    "maxTemperatureC": 30,
    "dryerAllowed": false,
    "softenerAllowed": false,
    "fragrancePolicy": "NONE",
    "programMode": "NORMAL",
    "cycleMode": "BLOCKED"
  },
  "exception": null
}
```

El orden de entrada no altera la identidad de la evaluación: el par se normaliza por UUID.

### Consultar evaluación

`GET /compatibility/evaluations/{evaluationId}`

Permisos de lectura: todos los roles.

### Autorizar excepción

`POST /compatibility/evaluations/{evaluationId}/exception`

Solo `ADMIN`.

```json
{"reason":"Separación mediante bolsas y supervisión reforzada"}
```

No se permite si la evaluación original ya era compatible o si ya existe una excepción.

## Pagos y auditoría

| Método | Ruta | Uso |
|---|---|---|
| POST | `/payments` | registrar pago |
| GET | `/payments/order/{orderId}` | historial por pedido |
| GET | `/audit` | consulta administrativa paginada |

## Códigos de error relevantes de compatibilidad

| Código | Estado | Significado |
|---|---:|---|
| `ORDER_NOT_FOUND` | 404 | pedido inexistente |
| `ORDER_NOT_READY_FOR_COMPATIBILITY` | 422 | pedido fuera de `CLASSIFIED` |
| `RECEPTION_NOT_FOUND` | 422 | no existe recepción |
| `TREATMENT_PROFILE_NOT_FOUND` | 422 | falta perfil de uno de los pedidos |
| `SAME_ORDER_COMPATIBILITY` | 400 | se seleccionó el mismo pedido |
| `COMPATIBILITY_EVALUATION_NOT_FOUND` | 404 | evaluación inexistente |
| `COMPATIBILITY_EXCEPTION_NOT_REQUIRED` | 422 | resultado original compatible |
| `COMPATIBILITY_EXCEPTION_ALREADY_EXISTS` | 409 | excepción duplicada |

## Fuera del contrato actual

No existen endpoints productivos para ciclos, máquinas, rutas, caja, costos, inventario ni carga binaria de evidencias.
