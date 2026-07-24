# Seguridad

Versión funcional: `0.3.0`.

Fuente de verdad: `SecurityConfig`, controladores, servicios de autenticación, configuración y pruebas de integración.

## Sesiones

- contraseñas con BCrypt, costo documentado 12;
- access JWT HS256 de corta duración;
- clave HMAC de al menos 256 bits;
- refresh token opaco, aleatorio, hasheado, rotativo y revocable;
- cookie `HttpOnly`, `SameSite=Strict`, path `/api/auth`;
- `Secure=true` en perfil `prod`;
- access token exclusivamente en memoria del frontend;
- una sola renovación HTTP en vuelo;
- throttling local de login.

No guardar access/refresh en `localStorage`, `sessionStorage` ni logs.

## Roles y jerarquía

```text
ADMIN > OPERATOR > DRIVER > REPORT_VIEWER
```

La jerarquía amplía permisos, pero no elimina precondiciones de dominio.

### Matriz verificada en controladores

| Operación | Permiso mínimo efectivo |
|---|---|
| login/refresh/logout | contrato de sesión |
| catálogo | usuario autenticado |
| crear/actualizar cliente y domicilios | `OPERATOR` |
| consultar clientes | `REPORT_VIEWER` |
| crear pedido y confirmar precio | `OPERATOR` |
| cotización manual | `ADMIN` |
| cambiar estado de pedido | `DRIVER` |
| registrar recepción | `OPERATOR` |
| consultar recepción | `REPORT_VIEWER` |
| decidir recepción | `OPERATOR` |
| guardar perfil/evaluar compatibilidad | `OPERATOR` |
| consultar perfiles/evaluaciones | `REPORT_VIEWER` |
| autorizar excepción | `ADMIN` |
| registrar pago | `OPERATOR` |
| consultar pagos | `REPORT_VIEWER` |
| consultar auditoría | `ADMIN` |

`ReceptionFlowIT.driverCanReadReceptionButCannotCreateIt` confirma que `DRIVER` no puede registrar recepción en el código actual.

## Autoridad del backend

- Bean Validation valida DTO.
- Spring Security valida autenticación y rol.
- Servicios validan precondiciones y transiciones.
- PostgreSQL aplica constraints.
- La UI no es control de seguridad.

Un `ADMIN` tampoco puede crear un perfil fuera de `CLASSIFIED`, crear una segunda recepción, sobrepagar ni exceptuar una evaluación compatible.

## Preferencias y compatibilidad

`CompatibilityService.effectiveProfile` preserva la opción más restrictiva:

- prohibición de secadora;
- prohibición de suavizante;
- exigencia hipoalergénica;
- fragancia `NONE` para hipoalergénico;
- exclusividad del pedido/cliente.

Una excepción:

- requiere `ADMIN`;
- requiere motivo;
- registra actor y fecha;
- es única;
- no modifica resultado original;
- cambia solo compatibilidad efectiva.

## Concurrencia e idempotencia

- promociones: bloqueo al confirmar;
- pagos: bloqueo de pedido;
- recepción: bloqueo de pedido + clave idempotente + constraints;
- perfil: bloqueo de pedido;
- evaluación: bloqueo de dos pedidos en orden UUID;
- excepción: bloqueo de evaluación.

El orden canónico evita interbloqueos A/B contra B/A.

## Errores y correlación

- `X-Request-ID` se valida o genera.
- Se propaga por MDC.
- Producción usa logs JSON.
- Errores inesperados se registran con stack en servidor.
- El cliente recibe mensaje seguro sin stack trace.
- Credenciales/tokens rechazados no deben reflejarse.

## CORS y cookies

`CORS_ALLOWED_ORIGINS` debe enumerar orígenes confiables. En producción se deben revisar dominio, TLS, cookies, reverse proxy y cabeceras reenviadas como un conjunto.

## Secretos

Obligatorios fuera de test:

```text
POSTGRES_PASSWORD
DB_PASSWORD
JWT_SECRET_BASE64
APP_DEV_ADMIN_PASSWORD en dev
```

El repositorio contiene solo placeholders o secretos de prueba no productivos. No copiar valores reales.

## Auditoría

Eventos sensibles incluyen entidad, identificador, acción, actor, valores relevantes, motivo y fecha. La auditoría técnica no equivale a firma digital, consentimiento ni no repudio legal.

## Riesgos abiertos

1. throttling local, no distribuido;
2. sin MFA;
3. sin gestión completa de usuarios/sesiones/roles;
4. sin WAF ni topología productiva;
5. sin gestor/rotación de secretos;
6. sin backup/restore probado;
7. sin política de privacidad y retención de evidencias;
8. sin idempotencia de webhooks de pago;
9. reglas `COMPAT-1` en código;
10. sin pruebas DAST ni carga.

Antes de producción, todos los puntos de severidad alta de [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md) deben resolverse.
