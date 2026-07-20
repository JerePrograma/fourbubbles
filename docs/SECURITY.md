# Seguridad

## Implementado

- contraseñas BCrypt con costo 12;
- access token JWT firmado con clave HMAC de al menos 256 bits;
- access token corto;
- refresh token aleatorio opaco;
- solo se almacena SHA-256 del refresh token;
- rotación al renovar y revocación al cerrar sesión;
- cookie `HttpOnly`, `SameSite=Strict`, `Secure` en producción;
- sesiones stateless;
- autorización por roles y métodos;
- validación de entrada;
- consultas JPA parametrizadas;
- mensajes de error sin stacktrace al cliente;
- secretos por variables;
- logs sin tokens ni contraseñas.

## Decisiones y límites

CSRF está deshabilitado porque las operaciones de negocio requieren bearer token. La cookie de refresh no autoriza llamadas de negocio y está restringida por path y SameSite. Antes de permitir orígenes de terceros debe revisarse este supuesto.

## Pendiente

- rate limiting de login y refresh;
- revocación de todas las sesiones de un usuario;
- recuperación y cambio de contraseña;
- MFA para administración;
- correlación y monitoreo de eventos;
- cabeceras CSP/HSTS en despliegue;
- escaneo de dependencias y secretos;
- respaldo cifrado y prueba de restauración;
- política de retención y anonimización.
