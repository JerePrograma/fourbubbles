# Seguridad

## Implementado

- contraseñas BCrypt con costo 12;
- access token JWT HS256 firmado con clave HMAC de al menos 256 bits;
- access token corto;
- refresh token aleatorio opaco;
- almacenamiento exclusivo del SHA-256 del refresh token;
- rotación al renovar y revocación al cerrar sesión;
- cookie `HttpOnly`, `SameSite=Strict` y `Secure` en producción;
- sesiones stateless;
- autorización por roles y métodos;
- respuestas JSON uniformes para autenticación requerida y acceso denegado;
- validación de entrada y parámetros;
- consultas JPA parametrizadas;
- mensajes de error sin stacktrace al cliente;
- secretos por variables de entorno;
- valores rechazados de contraseña y token ocultos en errores;
- logs sin tokens ni contraseñas;
- `X-Request-ID` validado, generado y expuesto;
- correlación de solicitudes mediante MDC y logs JSON de producción;
- limitación básica de intentos fallidos de login por usuario y origen.

## Protección del login

Valores predeterminados:

- máximo 5 fallos;
- ventana de 15 minutos;
- bloqueo de 15 minutos.

Variables:

- `LOGIN_MAX_ATTEMPTS`;
- `LOGIN_ATTEMPT_WINDOW`;
- `LOGIN_BLOCK_DURATION`.

Las duraciones usan sintaxis ISO-8601, por ejemplo `PT15M`.

### Límite arquitectónico

El contador reside en memoria de proceso:

- se pierde al reiniciar;
- no se comparte entre réplicas;
- no sustituye un rate limiter perimetral;
- no es suficiente para producción horizontal.

Antes de escalar a más de una instancia debe moverse a Redis o almacenamiento compartido con expiración atómica. También debe existir limitación en proxy o gateway.

## Dirección de origen y proxies

La protección usa la dirección que observa `HttpServletRequest.getRemoteAddr()`.

En producción:

- debe configurarse el proxy inverso como confiable;
- no debe confiarse ciegamente en un `X-Forwarded-For` enviado por Internet;
- el proxy debe sobrescribir cabeceras reenviadas;
- deben probarse las reglas con la topología real.

## Correlación

El identificador entrante se acepta únicamente cuando cumple `[A-Za-z0-9._-]{8,128}`. Esto evita introducir saltos de línea o caracteres arbitrarios en logs. Los demás valores se sustituyen por UUID.

## CSRF

CSRF está deshabilitado porque las operaciones de negocio requieren bearer token. La cookie de refresh no autoriza operaciones de negocio y está restringida por path y SameSite. Antes de permitir orígenes de terceros debe revisarse este supuesto.

## Roles actuales

- `ADMIN`: administración y operación completas del corte actual;
- `OPERATOR`: clientes, pedidos, estados operativos y pagos;
- `DRIVER`: consulta de pedidos y transiciones habilitadas;
- `REPORT_VIEWER`: consulta sin escritura.

La UI oculta acciones según rol, pero la autoridad real permanece en `@PreAuthorize` del backend.

## Pendiente

- limitación distribuida de login y refresh;
- protección específica del endpoint de refresh;
- revocación de todas las sesiones de un usuario;
- recuperación y cambio de contraseña;
- MFA para administración;
- bloqueo y desbloqueo administrativo de cuentas;
- CSP, HSTS y cabeceras adicionales en despliegue;
- escaneo automático de dependencias, imágenes y secretos;
- SBOM y política de vulnerabilidades;
- respaldo cifrado y prueba periódica de restauración;
- política de retención, anonimización y eliminación;
- auditoría consultable de eventos de seguridad;
- idempotencia antes de pagos externos o webhooks.
