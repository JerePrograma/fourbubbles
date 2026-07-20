# Seguridad

Versión: `0.2.0`.

## Credenciales y sesiones

- BCrypt costo 12.
- JWT HS256 de corta duración.
- HMAC mínima de 256 bits.
- refresh opaco, aleatorio, hasheado, rotativo y revocable.
- cookie `HttpOnly`, `SameSite=Strict`, `Secure` en producción.
- access token solo en memoria del frontend.

## Roles

```text
ADMIN > OPERATOR > DRIVER > REPORT_VIEWER
```

| Operación | ADMIN | OPERATOR | DRIVER | REPORT_VIEWER |
|---|---:|---:|---:|---:|
| consultar pedidos/recepción | sí | sí | sí | sí |
| registrar recepción | sí | sí | no | no |
| decidir diferencias | sí | sí | no | no |
| cotización manual | sí | no | no | no |
| cambiar estado | sí | sí | sí | no |
| registrar pago | sí | sí | no | no |
| consultar auditoría | sí | no | no | no |

La UI no es una barrera de seguridad. Los endpoints están protegidos con `@PreAuthorize`.

## Contratos y observabilidad

- 401/403 JSON uniformes.
- Bean Validation y parámetros inválidos seguros.
- stacktraces no expuestos.
- `X-Request-ID` validado/generado.
- MDC y logs correlacionados.
- secretos fuera de Git.
- administrador automático solo en `dev`.

## Protección de login

- intentos por usuario/origen;
- ventana configurable;
- bloqueo temporal;
- limpieza al autenticar.

Es local a la instancia; producción distribuida requiere almacenamiento compartido o control perimetral.

## Integridad transaccional

### Promoción

Bloqueo pesimista y revalidación al confirmar.

### Pago

Bloqueo pesimista del pedido antes de calcular saldo y persistir.

### Domicilio principal

Flush intermedio para respetar el índice único parcial.

### Recepción

La recepción usa defensa en profundidad:

1. valida formato de `Idempotency-Key`;
2. consulta uso previo de la clave;
3. bloquea el pedido con `PESSIMISTIC_WRITE`;
4. vuelve a comprobar pedido y clave;
5. valida estado y payload;
6. persiste recepción, snapshot real, estados y auditoría en una transacción;
7. constraints únicos respaldan la garantía.

Comportamiento:

- misma clave/mismo pedido: respuesta existente;
- misma clave/otro pedido: 409;
- otra clave/pedido recibido: 409;
- carrera con misma clave: un agregado, dos respuestas equivalentes.

### Decisión

Solo se acepta cuando:

- pedido en `WAITING_PRICE_APPROVAL`;
- recepción en `PENDING`;
- decisión `APPROVED` o `REJECTED`;
- actor autenticado disponible.

La decisión, transición y auditoría son atómicas.

## Evidencias

La aplicación no acepta binarios en 0.2.0. Solo registra metadata validada:

- clave de objeto;
- nombre y MIME;
- tamaño positivo;
- SHA-256 hexadecimal de 64 caracteres;
- descripción.

Riesgos todavía abiertos:

- no verifica que el objeto exista;
- no valida contenido real contra MIME/hash;
- no controla acceso al object storage;
- no hay URL firmada ni antivirus.

Un despliegue productivo deberá agregar un flujo de carga seguro y asociar metadata únicamente después de confirmar el objeto.

## Datos personales

Recepción puede contener observaciones y fotografías sensibles. Debe definirse:

- finalidad;
- consentimiento/base legal;
- acceso por rol;
- retención;
- borrado/anonimización cuando corresponda;
- auditoría de acceso;
- cifrado en tránsito y reposo.

## Riesgos pendientes

1. limitador de login local;
2. proxy/IP de confianza no configurado automáticamente;
3. sin MFA;
4. sin administración UI de usuarios;
5. sin idempotencia de pagos externos/webhooks;
6. sin object storage integrado;
7. sin antivirus ni validación binaria;
8. sin gestión central de secretos;
9. sin SAST/escaneo de imágenes obligatorio;
10. sin política formal de retención.

## Antes de producción

- perfil `prod`;
- TLS;
- secretos gestionados;
- CORS restringido;
- PostgreSQL persistente y backups restaurables;
- rate limiting compartido;
- object storage privado;
- URLs firmadas;
- escaneo de archivos;
- observabilidad/alertas;
- rollback;
- política de datos personales/evidencias.
