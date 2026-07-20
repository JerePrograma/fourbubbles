# Contrato API

Versión documentada: `0.2.0`.

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
  "path": "/api/resource",
  "timestamp": "2026-07-20T20:00:00-03:00",
  "violations": []
}
```

Cada respuesta incorpora `X-Request-ID`.

## Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/login` | access token y cookie refresh |
| POST | `/auth/refresh` | rotación del refresh |
| POST | `/auth/logout` | revocación y limpieza |

```http
Authorization: Bearer <token>
```

Jerarquía:

```text
ADMIN > OPERATOR > DRIVER > REPORT_VIEWER
```

## Catálogo

- `GET /catalog/services`
- `GET /catalog/equivalences`

## Clientes/domicilios

- `POST /clients`
- `GET /clients`
- `GET /clients/{id}`
- `PUT /clients/{id}`
- `POST /clients/{id}/addresses`
- `POST /clients/{id}/addresses/{addressId}/make-primary`
- `DELETE /clients/{id}/addresses/{addressId}`

Escrituras: `ADMIN`, `OPERATOR`. Lectura: roles heredados.

## Pedidos

- `POST /orders`
- `GET /orders`
- `GET /orders/{id}`
- `PATCH /orders/{id}/planning`
- `POST /orders/{id}/manual-quote`
- `POST /orders/{id}/confirm-price`
- `PATCH /orders/{id}/status`

## Recepción

### Registrar

```http
POST /orders/{orderId}/reception
Authorization: Bearer <token>
Idempotency-Key: web-reception-5c2cc70d-7bb1-40d6-a338-667721b72d74
Content-Type: application/json
```

Roles: `ADMIN`, `OPERATOR`.

```json
{
  "receivedAt": "2026-07-20T17:30:00-03:00",
  "actualWeightGrams": 2680,
  "conditionNotes": "Mancha leve en una remera",
  "bagCode": "BAG-0012",
  "items": [
    {
      "equivalenceCode": "TSHIRT",
      "actualPhysicalPieces": 2,
      "damageDetected": false,
      "stainDetected": true,
      "observations": "Mancha frontal"
    }
  ],
  "evidences": [
    {
      "objectKey": "receptions/RL-000012/front.jpg",
      "fileName": "front.jpg",
      "contentType": "image/jpeg",
      "sizeBytes": 245331,
      "sha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
      "caption": "Vista frontal"
    }
  ]
}
```

Reglas:

- clave obligatoria y segura;
- pedido en `PICKED_UP`;
- una recepción por pedido;
- todos los códigos declarados presentes;
- total real positivo;
- peso real positivo;
- fecha no futura;
- la misma clave devuelve el mismo agregado.

Respuesta relevante:

```json
{
  "id": "uuid",
  "orderId": "uuid",
  "receivedAt": "2026-07-20T17:30:00-03:00",
  "declaredPhysicalPieces": 2,
  "actualPhysicalPieces": 2,
  "declaredWeightGrams": 2500,
  "actualWeightGrams": 2680,
  "pieceDifference": 0,
  "weightDifferenceGrams": 180,
  "damageDetected": false,
  "stainDetected": true,
  "requiresCustomerApproval": false,
  "approvalStatus": "NOT_REQUIRED",
  "labelCode": "RCV-000001",
  "bagCode": "BAG-0012",
  "orderStatus": "CLASSIFIED",
  "items": [],
  "evidences": []
}
```

### Consultar

```http
GET /orders/{orderId}/reception
```

Roles de lectura. Si el pedido existe pero no tiene recepción, `data` es `null`.

### Decidir diferencias

```http
POST /orders/{orderId}/reception/decision
```

Roles: `ADMIN`, `OPERATOR`.

```json
{
  "decision": "APPROVED",
  "notes": "Cliente acepta diferencia documentada"
}
```

Valores admitidos:

- `APPROVED` → pedido `CLASSIFIED`;
- `REJECTED` → pedido `CANCELLED`.

Solo se acepta si recepción y pedido están pendientes de decisión.

## Pagos

- `POST /payments`
- `GET /payments?orderId={uuid}`

El pedido se bloquea durante el cálculo de saldo.

## Auditoría

```http
GET /audit?entityType=ORDER_RECEPTION&entityId=<uuid>&action=CREATE
```

Solo `ADMIN`.

## Códigos nuevos de recepción

- `IDEMPOTENCY_KEY_REQUIRED`;
- `INVALID_IDEMPOTENCY_KEY`;
- `IDEMPOTENCY_KEY_CONFLICT`;
- `ORDER_ALREADY_RECEIVED`;
- `ORDER_NOT_READY_FOR_RECEPTION`;
- `INVALID_RECEPTION_TIME`;
- `DUPLICATE_RECEPTION_ITEM`;
- `MISSING_DECLARED_RECEPTION_ITEMS`;
- `EMPTY_RECEPTION`;
- `RECEPTION_NOT_FOUND`;
- `RECEPTION_DECISION_NOT_ALLOWED`;
- `INVALID_RECEPTION_DECISION`.

Constraints únicos o carreras no previstas pueden responder `DATA_CONFLICT` sin exponer detalles de base.

## Errores generales

- `VALIDATION_ERROR`;
- `INVALID_PARAMETER`;
- `AUTHENTICATION_REQUIRED`;
- `ACCESS_DENIED`;
- `ORDER_NOT_FOUND`;
- `INVALID_STATUS_TRANSITION`;
- `INTERNAL_ERROR`.

Los errores internos se registran con correlación y no exponen stacktrace al cliente.
