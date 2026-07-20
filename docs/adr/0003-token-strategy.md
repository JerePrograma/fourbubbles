# ADR 0003: JWT corto y refresh opaco

## Estado

Aceptado para la etapa inicial.

## Decisión

Access token JWT en memoria del frontend. Refresh token aleatorio en cookie `HttpOnly`; hash persistido, rotación y revocación.

## Motivo

Evita persistir JWT de larga duración en almacenamiento accesible a JavaScript y permite revocar sesiones.

## Consecuencia

Debe mantenerse protección estricta de CORS, SameSite y cookie segura. Quedan pendientes rate limiting y gestión de todas las sesiones.
