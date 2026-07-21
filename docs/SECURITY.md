# Seguridad

Versión: `0.3.0`.

## Credenciales y sesiones

- BCrypt costo 12.
- JWT HS256 de corta duración.
- clave HMAC mínima de 256 bits.
- refresh token opaco, aleatorio, hasheado, rotativo y revocable.
- cookie `HttpOnly`, `SameSite=Strict`, `Secure` en producción.
- access token solo en memoria del frontend.
- bloqueo local ante intentos repetidos de login.

## Roles

Jerarquía:

```text
ADMIN > OPERATOR > DRIVER > REPORT_VIEWER
```

| Operación sensible | Rol mínimo |
|---|---|
| cotización manual | ADMIN |
| consulta de auditoría | ADMIN |
| excepción de compatibilidad | ADMIN |
| decidir diferencias de recepción | OPERATOR |
| guardar perfil/evaluar compatibilidad | OPERATOR |
| registrar recepción | DRIVER |
| consultar perfiles/evaluaciones | REPORT_VIEWER |

La jerarquía no sustituye las precondiciones de dominio. Un `ADMIN` tampoco puede crear un perfil fuera de `CLASSIFIED` ni exceptuar una evaluación originalmente compatible.

## Datos y validación

- Bean Validation en DTO.
- errores uniformes sin stack trace al cliente.
- valores rechazados de contraseña/token no se reflejan.
- JSON de preferencias normalizado al persistir.
- restricciones del cliente y pedido se aplican en backend.
- el frontend no es autoridad para permisos ni compatibilidad.

### Restricciones efectivas de compatibilidad

El formulario puede solicitar un tratamiento, pero el backend conserva las restricciones más estrictas:

- `dryerAllowed=false` del cliente prevalece;
- `softenerAllowed=false` del cliente prevalece;
- `hypoallergenic=true` prevalece y fuerza fragancia `NONE`;
- `exclusiveCycle=true` del pedido o cliente prevalece.

Esto evita que una edición accidental convierta una carga restringida en una carga estándar.

## Concurrencia e idempotencia

- promociones bloqueadas al confirmar;
- pedido bloqueado al registrar pagos;
- pedido bloqueado al registrar recepción;
- recepción única por pedido y clave idempotente;
- perfil creado/actualizado bajo bloqueo del pedido;
- evaluación bloquea ambos pedidos en orden UUID;
- excepción bloquea la evaluación.

El orden UUID evita interbloqueos entre dos evaluaciones que consultan el mismo par en orden inverso.

## Compatibilidad y excepciones

El resultado original se conserva en `compatible`. Una excepción:

- requiere `ADMIN`;
- requiere motivo concreto;
- registra actor y fecha;
- no puede duplicarse;
- no cambia las razones ni la recomendación originales;
- solo cambia `effectivelyCompatible`.

La excepción es una decisión operativa excepcional, no una modificación encubierta de reglas.

## Auditoría

Se auditan, entre otros:

- cliente y domicilio;
- pedido, precio y estado;
- pago;
- recepción y decisión;
- perfil de tratamiento;
- evaluación de compatibilidad;
- excepción administrativa.

Los eventos incluyen tipo de entidad, identificador, acción, actor, cambios relevantes, motivo y fecha.

## Correlación y logs

- `X-Request-ID` se acepta o genera.
- el identificador se propaga mediante MDC.
- producción usa logs JSON.
- errores inesperados se registran en servidor con stack trace.
- el cliente recibe un mensaje seguro.

## Riesgos abiertos

1. Rate limiting local, no distribuido.
2. Sin MFA.
3. Sin administración completa de usuarios/roles.
4. Sin WAF ni reverse proxy productivo definido.
5. Sin política formal de retención/borrado de evidencias.
6. Evidencias solo metadata; la seguridad del objeto externo depende del proveedor futuro.
7. Sin secretos administrados ni rotación automatizada.
8. Sin backups/restore automatizados.
9. Sin idempotencia de webhooks de pago externos.
10. Las reglas `COMPAT-1` están en código; cambios requieren release y nueva versión de reglas.

## Requisitos antes de producción

- TLS extremo a extremo.
- secretos administrados.
- cookies y CORS revisados para el dominio final.
- backups cifrados y restauración ensayada.
- object storage privado con URLs temporales.
- rate limit distribuido.
- monitoreo, alertas y retención de auditoría.
- política de privacidad y tratamiento de imágenes/datos personales.
