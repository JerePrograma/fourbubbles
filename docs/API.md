# Contrato API

Versión documentada: `0.1.2`.

La API se sirve bajo `/api`. Swagger local: `/api/swagger-ui.html`.

## Convenciones

Éxito:

```json
{
  "success": true,
  "data": {}
}
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

Cada respuesta incluye `X-Request-ID`. Un valor entrante válido se conserva; de lo contrario se genera un UUID.

## Autenticación

| Método | Ruta | Autorización | Descripción |
|---|---|---|---|
| POST | `/auth/login` | pública | access token y cookie refresh |
| POST | `/auth/refresh` | cookie refresh | rotación de refresh |
| POST | `/auth/logout` | sesión | revocación y limpieza de cookie |

El access token se envía como:

```http
Authorization: Bearer <token>
```

## Roles

Jerarquía:

```text
ADMIN > OPERATOR > DRIVER > REPORT_VIEWER
```

La jerarquía habilita permisos heredados. Las anotaciones específicas continúan restringiendo escrituras administrativas.

## Catálogo

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| GET | `/catalog/services` | autenticado | servicios vigentes |
| GET | `/catalog/equivalences` | autenticado | equivalencias vigentes |

## Clientes y domicilios

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| POST | `/clients` | ADMIN, OPERATOR | crear cliente con domicilios |
| GET | `/clients` | lectura | búsqueda paginada |
| GET | `/clients/{id}` | lectura | perfil, domicilios activos e historial |
| PUT | `/clients/{id}` | ADMIN, OPERATOR | actualizar perfil/preferencias |
| POST | `/clients/{id}/addresses` | ADMIN, OPERATOR | agregar domicilio |
| POST | `/clients/{id}/addresses/{addressId}/make-primary` | ADMIN, OPERATOR | establecer principal |
| DELETE | `/clients/{id}/addresses/{addressId}` | ADMIN, OPERATOR | baja lógica |

### Crear domicilio

```json
{
  "zoneCode": "MARCOS_PAZ",
  "street": "Sarmiento",
  "number": "123",
  "extra": null,
  "locality": "Marcos Paz",
  "neighborhood": "Centro",
  "references": "Portón negro",
  "primaryAddress": false
}
```

Reglas:

- debe quedar al menos un domicilio activo;
- existe un único principal activo;
- el principal no puede desactivarse;
- una baja conserva historial y referencias de pedidos.

## Pedidos

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| POST | `/orders` | ADMIN, OPERATOR | crear y cotizar |
| GET | `/orders` | lectura operativa | buscar por número, cliente y estado |
| GET | `/orders/{id}` | lectura operativa | detalle y transiciones permitidas |
| PATCH | `/orders/{id}/planning` | ADMIN, OPERATOR | retiro, promesa y notas tempranas |
| POST | `/orders/{id}/manual-quote` | ADMIN | registrar cotización manual |
| POST | `/orders/{id}/confirm-price` | ADMIN, OPERATOR | confirmar precio |
| PATCH | `/orders/{id}/status` | ADMIN, OPERATOR, DRIVER | transición válida |

### Crear pedido

```json
{
  "clientId": "uuid",
  "addressId": "uuid",
  "serviceCode": "ROPA_LISTA_12",
  "promotionCode": null,
  "declaredWeightGrams": 2200,
  "exclusiveCycle": false,
  "pickupScheduledAt": null,
  "promisedAt": null,
  "notes": "Observación",
  "items": [
    {
      "equivalenceCode": "TSHIRT",
      "physicalPieces": 4,
      "observations": null
    }
  ]
}
```

### Planificación temprana

```json
{
  "pickupScheduledAt": "2026-07-21T10:00:00-03:00",
  "promisedAt": "2026-07-23T18:00:00-03:00",
  "notes": "Llamar antes"
}
```

Solo se admite en `INQUIRY` o `QUOTED` y antes de confirmar precio.

### Cotización manual

```json
{
  "amount": 12000.00,
  "reason": "Prenda especial que requiere tratamiento individual"
}
```

La respuesta conserva:

- `automaticQuotedPrice`;
- `quotedPrice` vigente;
- `manualQuoteReason`;
- `manualQuoteAt`;
- `manualQuoteBy`;
- `priceBreakdown` actualizado.

### Cambio de estado

```json
{
  "newStatus": "RESERVED",
  "observation": "Confirmado por cliente",
  "location": null,
  "notificationReference": null
}
```

El backend rechaza transiciones fuera de la política y devuelve `allowedTransitions` en el detalle.

## Pagos

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| POST | `/payments` | ADMIN, OPERATOR | registrar pago |
| GET | `/payments?orderId={uuid}` | lectura | historial del pedido |

```json
{
  "orderId": "uuid",
  "methodCode": "TRANSFER",
  "amount": 3000.00,
  "paidAt": null,
  "reference": "TRX-123",
  "notes": null
}
```

Reglas:

- precio confirmado obligatorio;
- importe positivo;
- total acumulado no superior al confirmado;
- pedido bloqueado durante el cálculo de saldo;
- historial ordenado por fecha.

## Auditoría

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| GET | `/audit` | ADMIN | búsqueda paginada |

Parámetros opcionales:

- `entityType`;
- `entityId`;
- `action`;
- `page`;
- `size`.

## Códigos relevantes

- `VALIDATION_ERROR`;
- `INVALID_PARAMETER`;
- `AUTHENTICATION_REQUIRED`;
- `ACCESS_DENIED`;
- `CLIENT_NOT_FOUND`;
- `ADDRESS_NOT_FOUND`;
- `DUPLICATE_WHATSAPP`;
- `PRIMARY_ADDRESS_REQUIRED`;
- `PRIMARY_ADDRESS_CANNOT_BE_DEACTIVATED`;
- `ORDER_NOT_FOUND`;
- `ORDER_NOT_EDITABLE`;
- `MANUAL_QUOTE_REQUIRED`;
- `INVALID_STATUS_TRANSITION`;
- `PRICE_NOT_CONFIRMED`;
- `PAYMENT_EXCEEDS_BALANCE`;
- `PROMOTION_ALREADY_USED_AT_ADDRESS`;
- `PROMOTION_QUOTA_EXHAUSTED`;
- `DATA_CONFLICT`;
- `INTERNAL_ERROR`.

Los errores internos no exponen stacktrace ni secretos al cliente.
