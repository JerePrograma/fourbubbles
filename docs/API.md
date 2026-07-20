# Contrato API

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

## Autenticación

- access token: encabezado `Authorization: Bearer ...`;
- refresh token: cookie `HttpOnly` limitada a `/api/auth`;
- la renovación rota y revoca el token anterior;
- la UI no persiste el access token en `localStorage`.

## Idempotencia

No está implementada todavía. Antes de integrar pagos externos o webhooks se agregará clave de idempotencia.

## Versionado

La API todavía no lleva `/v1`. Antes de publicar clientes externos debe incorporarse versionado explícito o una política formal de compatibilidad.
