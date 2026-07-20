# Contrato API

La API se sirve bajo `/api`. La documentación interactiva local está disponible en `/api/swagger-ui.html`.

## Convención de éxito

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-07-20T12:00:00-03:00"
}
```

## Convención de error

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "La solicitud contiene datos inválidos",
  "status": 400,
  "path": "/api/clients",
  "timestamp": "2026-07-20T12:00:00-03:00",
  "violations": [
    { "field": "firstName", "message": "no debe estar vacío" }
  ]
}
```

El mismo formato se utiliza para errores generados dentro de Spring Security:

- `AUTHENTICATION_REQUIRED`, HTTP 401;
- `AUTHENTICATION_FAILED`, HTTP 401;
- `ACCESS_DENIED`, HTTP 403;
- `LOGIN_RATE_LIMITED`, HTTP 429.

Los errores de validación no devuelven valores rechazados de campos sensibles.

## Correlación

Cada respuesta incluye `X-Request-ID`.

- Si el cliente envía un identificador entre 8 y 128 caracteres formado por letras, números, punto, guion o guion bajo, se conserva.
- Cualquier valor ausente o inseguro se reemplaza por un UUID.
- El identificador se incorpora al MDC y a los logs JSON de producción.

## Autenticación

- access token: encabezado `Authorization: Bearer <token>`;
- refresh token: cookie `HttpOnly` limitada a `/api/auth`;
- la renovación rota y revoca el refresh token anterior;
- la UI no persiste el access token en `localStorage`;
- cerrar sesión revoca la sesión actual y elimina la cookie;
- los intentos repetidos de login se limitan por usuario normalizado y origen observado.

## Endpoints disponibles

| Método | Ruta | Resultado principal |
|---|---|---|
| POST | `/auth/login` | access token y cookie refresh |
| POST | `/auth/refresh` | access token renovado y refresh rotado |
| POST | `/auth/logout` | sesión revocada |
| GET | `/catalog/equivalences` | equivalencias vigentes |
| GET | `/catalog/services` | servicios vigentes y límites |
| POST | `/clients` | cliente, domicilios y preferencias creados |
| GET | `/clients` | página de clientes, filtrable por apellido |
| GET | `/clients/{id}` | cliente, preferencias y domicilios activos |
| PUT | `/clients/{id}` | perfil, estado y preferencias actualizados |
| POST | `/orders` | pedido creado y cotizado |
| GET | `/orders` | página de pedidos filtrable por número, cliente y estado |
| GET | `/orders/{id}` | detalle, prendas y transiciones permitidas |
| POST | `/orders/{id}/confirm-price` | precio histórico confirmado |
| PATCH | `/orders/{id}/status` | transición validada y auditada |
| POST | `/payments` | pago registrado y saldo actualizado |

## Preferencias de cliente

Contrato tipado:

```json
{
  "fragrance": "sin perfume",
  "softenerAllowed": false,
  "dryerAllowed": true,
  "hypoallergenic": true,
  "separateColors": true,
  "specialInstructions": "No usar suavizante"
}
```

`preferencesJson` continúa aceptándose transitoriamente para compatibilidad, pero debe contener un objeto JSON válido. Los nuevos consumidores deben usar `preferences`.

La actualización de cliente no reemplaza domicilios existentes. Esa decisión evita borrar trazabilidad hasta implementar versionado explícito de domicilios.

## Búsqueda de pedidos

```http
GET /api/orders?orderNumber=RL-0001&status=QUOTED&page=0&size=20
```

Parámetros:

- `orderNumber`: coincidencia parcial, sin distinguir mayúsculas;
- `clientId`: UUID exacto;
- `status`: valor válido de `OrderStatus`;
- `page`: mínimo lógico 0;
- `size`: limitado internamente entre 1 y 100.

El detalle devuelve `allowedTransitions`, evitando que el consumidor invente pasos no habilitados.

## Reglas de contrato relevantes

- No se exponen entidades JPA directamente.
- Los importes se transmiten como números decimales y se procesan con `BigDecimal`.
- Los pesos se transmiten en gramos enteros.
- Fechas con hora utilizan ISO 8601 con offset.
- Los códigos de moneda son cadenas de tres caracteres, inicialmente `ARS`.
- Las operaciones sensibles requieren autenticación y permisos.
- Las transiciones inválidas devuelven error de dominio.
- El precio confirmado y su desglose se conservan en el pedido.
- Los pagos no pueden superar el saldo ni registrarse antes de confirmar el precio.

## Idempotencia

No está implementada todavía. Antes de integrar pagos externos, webhooks o reintentos automáticos se debe agregar una clave de idempotencia y una política de repetición.

## Paginación

Clientes y pedidos utilizan `Page` de Spring. Antes de publicar la API para consumidores externos se debe reemplazar o formalizar este formato para evitar dependencia de detalles del framework.

## Versionado

La API todavía no lleva `/v1`. Antes de exponerla a consumidores externos se debe incorporar versionado explícito o una política formal de compatibilidad.

## Pendientes del contrato

- edición y versionado de domicilios;
- ajuste manual de cotizaciones;
- historial completo de pagos;
- recepción y peso real;
- carga de evidencias;
- administración comercial;
- compatibilidad y ciclos;
- rutas y agenda;
- caja, costos y reportes.
