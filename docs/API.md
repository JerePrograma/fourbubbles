# Contrato API

La API se sirve bajo `/api`. La documentación interactiva local está disponible en `/api/swagger-ui.html`.

El recorrido funcional con ejemplos completos está en [USER_GUIDE.md](USER_GUIDE.md).

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

Los errores de validación no devuelven valores rechazados de campos sensibles.

## Autenticación

- access token: encabezado `Authorization: Bearer <token>`;
- refresh token: cookie `HttpOnly` limitada a `/api/auth`;
- la renovación rota y revoca el refresh token anterior;
- la UI no persiste el access token en `localStorage`;
- cerrar sesión revoca la sesión actual y elimina la cookie.

Inicio de sesión:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "contraseña-configurada"
}
```

## Endpoints disponibles

| Método | Ruta | Resultado principal |
|---|---|---|
| POST | `/auth/login` | access token y cookie refresh |
| POST | `/auth/refresh` | access token renovado y refresh rotado |
| POST | `/auth/logout` | sesión revocada |
| GET | `/catalog/equivalences` | equivalencias vigentes |
| POST | `/clients` | cliente y domicilios creados |
| GET | `/clients` | página de clientes |
| GET | `/clients/{id}` | cliente y domicilios |
| POST | `/orders` | pedido cotizado |
| GET | `/orders/{id}` | detalle del pedido |
| POST | `/orders/{id}/confirm-price` | precio histórico confirmado |
| PATCH | `/orders/{id}/status` | transición validada y auditada |
| POST | `/payments` | pago y saldo actualizado |

## Reglas de contrato relevantes

- No se exponen entidades JPA directamente.
- Los importes se transmiten como números decimales y se procesan con `BigDecimal`.
- Los pesos se transmiten en gramos enteros.
- Fechas con hora utilizan ISO 8601 con offset, por ejemplo `2026-07-21T10:00:00-03:00`.
- Los códigos de moneda son cadenas de tres caracteres, inicialmente `ARS`.
- Las operaciones sensibles requieren autenticación y permisos.
- Las transiciones de pedido inválidas devuelven error de dominio.
- El precio confirmado y su desglose se conservan en el pedido.

## Idempotencia

No está implementada todavía. Antes de integrar pagos externos, webhooks o reintentos automáticos se debe agregar una clave de idempotencia y una política de repetición.

## Paginación

La búsqueda de clientes utiliza paginación Spring. Los clientes externos no deben depender de campos internos del formato `Page` sin formalizar primero el contrato.

## Versionado

La API todavía no lleva `/v1`. Antes de exponerla a consumidores externos se debe incorporar versionado explícito o una política formal de compatibilidad.

## Pendientes del contrato

- listado y filtros de pedidos;
- historial completo de cliente;
- recepción y peso real;
- carga de evidencias;
- administración comercial;
- compatibilidad y ciclos;
- rutas y agenda;
- caja, costos y reportes.
