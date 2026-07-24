# Contrato API

Versión funcional documentada: `0.3.0`.

Fuente de verdad: controladores y DTO de `backend/src/main/java/ar/com/ropalista`. Referencia inspeccionada: `6f6d3cd8256408bc574e5b3d4568bf1b2866b0d8`.

## Base, OpenAPI y envoltorios

Context path: `/api`.

Swagger UI local: `/api/swagger-ui.html`.

OpenAPI JSON: `/api/v3/api-docs`.

Éxito:

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-07-24T12:00:00-03:00"
}
```

Error:

```json
{
  "success": false,
  "code": "BUSINESS_CODE",
  "message": "Descripción segura",
  "status": 422,
  "path": "/api/...",
  "timestamp": "2026-07-24T12:00:00-03:00",
  "violations": []
}
```

Las rutas protegidas usan `Authorization: Bearer <ACCESS_TOKEN>`. El refresh viaja en la cookie `ropa_lista_refresh`, `HttpOnly`, `SameSite=Strict`, path `/api/auth`.

Los controladores devuelven `200 OK` mediante `ApiResponse.ok`, incluso en altas. Los errores de validación, autenticación, autorización y negocio usan el contrato uniforme.

## Autenticación

Controlador: `auth/api/AuthController`.

| Método y ruta | Método Java | Acceso | Request | Response | Efectos e idempotencia |
|---|---|---|---|---|---|
| `POST /auth/login` | `login` | público | `LoginRequest`: `username`, `password`, ambos no vacíos | `AuthResponse`: access token, TTL, usuario y roles; cookie refresh | crea/rota sesión; no idempotente |
| `POST /auth/refresh` | `refresh` | cookie refresh | sin body | nuevo access token y cookie rotada | rota y revoca refresh anterior; no idempotente |
| `POST /auth/logout` | `logout` | cookie opcional | sin body | `data=null`; cookie expirada | revoca la sesión si existe; repetición segura |

Errores relevantes: credenciales inválidas/bloqueo, refresh ausente o revocado, JSON inválido.

## Catálogo

Controlador: `catalog/api/CatalogController`.

No posee `@PreAuthorize` por método; la configuración global exige autenticación, confirmado por runtime smoke.

| Método y ruta | Método Java | Request | Response | Efectos |
|---|---|---|---|---|
| `GET /catalog/equivalences` | `equivalences` | ninguno | lista de códigos, nombres, categoría, piezas/grupo, unidades, peso y restricciones | lectura |
| `GET /catalog/services` | `services` | ninguno | última oferta vigente por código | lectura |

Nota de arquitectura: este controlador accede directamente a repositorios. Es una deuda conocida; consultar [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md).

## Clientes y domicilios

Controlador: `customer/api/ClientController`.

### DTO principales

`CreateClientRequest`:

- `firstName`, `lastName`, `phone`, `whatsapp`: obligatorios;
- `email`: formato email;
- `acquisitionSource`: máximo 100;
- `preferencesJson` o `preferences`;
- `notes`: máximo 2000;
- `addresses`: lista no vacía de `AddressRequest`.

`AddressRequest` exige `zoneCode`, calle, número y localidad. `primaryAddress` determina la principal.

| Método y ruta | Método Java | Permisos | Request | Response | Efectos |
|---|---|---|---|---|---|
| `POST /clients` | `create` | `ADMIN`, `OPERATOR` | `CreateClientRequest` | `ClientResponse` | crea cliente y domicilios; audita |
| `PUT /clients/{id}` | `update` | `ADMIN`, `OPERATOR` | `UpdateClientRequest` | `ClientResponse` | actualiza perfil/preferencias; audita |
| `POST /clients/{id}/addresses` | `addAddress` | `ADMIN`, `OPERATOR` | `AddressRequest` | `ClientResponse` | agrega versión de domicilio |
| `POST /clients/{id}/addresses/{addressId}/make-primary` | `makePrimary` | `ADMIN`, `OPERATOR` | sin body | `ClientResponse` | cambia principal conservando historial |
| `DELETE /clients/{id}/addresses/{addressId}` | `deactivateAddress` | `ADMIN`, `OPERATOR` | sin body | `ClientResponse` | baja lógica; conserva historial |
| `GET /clients/{id}` | `get` | `ADMIN`, `OPERATOR`, `REPORT_VIEWER` | path UUID | `ClientResponse` | lectura |
| `GET /clients?lastName=&page=0&size=20` | `search` | `ADMIN`, `OPERATOR`, `REPORT_VIEWER` | filtros query | página de `ClientResponse` | lectura |

Validaciones de dominio confirmadas: WhatsApp activo único, domicilio principal único y conservación de al menos un domicilio activo.

## Pedidos

Controlador: `order/api/OrderController`.

`CreateOrderRequest` exige `clientId`, `addressId`, `serviceCode` e ítems. Cada ítem exige `equivalenceCode` y `physicalPieces > 0`.

| Método y ruta | Método Java | Permisos | Request | Response | Efectos e idempotencia |
|---|---|---|---|---|---|
| `POST /orders` | `create` | `ADMIN`, `OPERATOR` | `CreateOrderRequest` | `OrderResponse` | calcula equivalencias/precio y crea pedido; no idempotente |
| `GET /orders` | `search` | cuatro roles | `orderNumber`, `clientId`, `status`, `page`, `size` | página de `OrderSummaryResponse` | lectura |
| `GET /orders/{id}` | `get` | cuatro roles | UUID | `OrderResponse` | lectura |
| `PATCH /orders/{id}/planning` | `updatePlanning` | `ADMIN`, `OPERATOR` | fechas retiro/promesa y notas | `OrderResponse` | actualiza planificación |
| `POST /orders/{id}/manual-quote` | `manualQuote` | `ADMIN` | monto positivo y motivo obligatorio | `OrderResponse` | cotización manual trazable; no idempotente |
| `POST /orders/{id}/confirm-price` | `confirmPrice` | `ADMIN`, `OPERATOR` | sin body | `OrderResponse` | bloquea/revalida promoción y confirma precio |
| `PATCH /orders/{id}/status` | `changeStatus` | `ADMIN`, `OPERATOR`, `DRIVER` | `newStatus`, observación, ubicación, referencia | `OrderResponse` | aplica política de transición y audita |

`OrderResponse.allowedTransitions` es calculado por backend. El frontend no debe inventar transiciones.

## Recepción

Controlador: `reception/api/ReceptionController`.

### Registrar

`POST /orders/{orderId}/reception`

Método Java: `receive`.

Permisos actuales: `ADMIN`, `OPERATOR`. `DRIVER` recibe `403` al crear y puede consultar una recepción existente; esto está cubierto en `ReceptionFlowIT.driverCanReadReceptionButCannotCreateIt`.

Header obligatorio:

```http
Idempotency-Key: reception-<identificador-unico>
```

Formato: 16–120 caracteres de `[A-Za-z0-9._:-]`.

Request `CreateReceptionRequest`:

```json
{
  "receivedAt": "2026-07-24T12:00:00-03:00",
  "actualWeightGrams": 2600,
  "conditionNotes": "Sin observaciones relevantes",
  "bagCode": "BAG-001",
  "items": [
    {
      "equivalenceCode": "TSHIRT",
      "actualPhysicalPieces": 2,
      "damageDetected": false,
      "stainDetected": false,
      "observations": null
    }
  ],
  "evidences": [
    {
      "objectKey": "receptions/<UUID>/front.jpg",
      "fileName": "front.jpg",
      "contentType": "image/jpeg",
      "sizeBytes": 1024,
      "sha256": "<64_HEX>",
      "caption": "Vista frontal"
    }
  ]
}
```

Precondiciones y efectos:

- pedido existente y bloqueado;
- estado `PICKED_UP`;
- una recepción por pedido;
- todos los códigos declarados deben estar presentes;
- códigos adicionales requieren equivalencia vigente;
- al menos una pieza real total;
- `actualWeightGrams > 0`;
- fecha no más de cinco minutos en el futuro;
- genera etiqueta, snapshot real, diferencias, auditoría y transiciones;
- misma clave/mismo pedido devuelve el mismo agregado;
- otra clave para un pedido recibido devuelve `409`;
- clave usada por otro pedido devuelve `409`;
- guarda metadata de evidencia, no binarios.

Errores verificados: `IDEMPOTENCY_KEY_REQUIRED`, `INVALID_IDEMPOTENCY_KEY`, `IDEMPOTENCY_KEY_CONFLICT`, `ORDER_ALREADY_RECEIVED`, `ORDER_NOT_READY_FOR_RECEPTION`, `INVALID_RECEPTION_TIME`, `DUPLICATE_RECEPTION_ITEM`, `MISSING_DECLARED_RECEPTION_ITEMS`, `EQUIVALENCE_NOT_FOUND`, `EMPTY_RECEPTION`.

### Consultar

`GET /orders/{orderId}/reception`

Método Java: `get`.

Permisos: cuatro roles.

Respuesta: `ReceptionResponse` o `data=null` cuando el pedido existe sin recepción.

### Decidir

`POST /orders/{orderId}/reception/decision`

Método Java: `decide`.

Permisos: `ADMIN`, `OPERATOR`.

Request:

```json
{"decision":"APPROVED","notes":"Cliente acepta la diferencia"}
```

Solo `APPROVED` o `REJECTED`. Requiere estado `WAITING_PRICE_APPROVAL` y recepción `PENDING`. Aprobar lleva a `CLASSIFIED`; rechazar a `CANCELLED`.

## Compatibilidad

Controlador: `compatibility/api/CompatibilityController`.

### Perfil

`PUT /orders/{orderId}/compatibility-profile`

Método: `saveProfile`.

Permisos: `ADMIN`, `OPERATOR`.

Precondiciones: pedido `CLASSIFIED` y recepción existente.

Request `TreatmentProfileRequest`:

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

Temperatura permitida: 20–95. El backend puede endurecer secadora, suavizante, hipoalergénico, fragancia y exclusividad.

`GET /orders/{orderId}/compatibility-profile`

Método: `getProfile`.

Permisos: cuatro roles. Devuelve perfil o `data=null`.

### Evaluar

`POST /compatibility/evaluate`

Método: `evaluate`.

Permisos: `ADMIN`, `OPERATOR`.

Request:

```json
{
  "orderAId": "11111111-1111-1111-1111-111111111111",
  "orderBId": "22222222-2222-2222-2222-222222222222"
}
```

Efectos:

- rechaza el mismo pedido;
- normaliza UUID;
- bloquea ambos pedidos en orden estable;
- exige `CLASSIFIED` y perfiles;
- reutiliza snapshot por versiones y `COMPAT-1`, o crea uno;
- persiste razones y recomendación;
- audita.

No es idempotencia por clave, pero la identidad única hace converger repeticiones con las mismas versiones.

`GET /compatibility/evaluations/{evaluationId}`

Permisos: cuatro roles. Lectura del snapshot.

### Excepción

`POST /compatibility/evaluations/{evaluationId}/exception`

Permiso: solo `ADMIN`.

Request:

```json
{"reason":"Separación mediante bolsas y supervisión reforzada"}
```

Requiere evaluación originalmente incompatible y sin excepción. Bloquea evaluación, conserva resultado original y cambia solo `effectivelyCompatible`.

Errores verificados: `ORDER_NOT_FOUND`, `ORDER_NOT_READY_FOR_COMPATIBILITY`, `RECEPTION_NOT_FOUND`, `TREATMENT_PROFILE_NOT_FOUND`, `SAME_ORDER_COMPATIBILITY`, `COMPATIBILITY_EVALUATION_NOT_FOUND`, `COMPATIBILITY_EXCEPTION_NOT_REQUIRED`, `COMPATIBILITY_EXCEPTION_ALREADY_EXISTS`.

## Pagos

Controlador: `payment/api/PaymentController`.

| Método y ruta | Método Java | Permisos | Request | Response | Efectos |
|---|---|---|---|---|---|
| `POST /payments` | `register` | `ADMIN`, `OPERATOR` | `orderId`, `methodCode`, monto positivo, fecha, referencia y notas | `PaymentResponse` | bloquea pedido, impide sobrepago, actualiza saldo/estado y audita |
| `GET /payments?orderId={UUID}` | `history` | `ADMIN`, `OPERATOR`, `REPORT_VIEWER` | query obligatorio `orderId` | lista de `PaymentHistoryResponse` | lectura |

No existe idempotencia para proveedores/webhooks externos.

## Auditoría

Controlador: `audit/api/AuditController`.

`GET /audit?entityType=&entityId=&action=&page=0&size=20`

Permiso: solo `ADMIN`.

Respuesta: página de `AuditEventResponse` con entidad, acción, valores, motivo, fecha y actor.

## Códigos HTTP y errores

- `400`: request, parámetro, enum o precondición sintáctica inválida;
- `401`: sin autenticación o sesión inválida;
- `403`: rol insuficiente;
- `404`: recurso inexistente;
- `409`: unicidad, idempotencia o concurrencia conflictiva;
- `422`: regla de negocio o transición no permitida;
- `500`: error inesperado; el cliente no recibe stack trace.

## Fuera del contrato actual

No existen endpoints en `main` para máquinas, programas, ciclos, rutas, caja, costos, inventario, carga binaria de evidencias, backups ni administración completa de usuarios.
