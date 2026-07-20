# Seguridad

Versión: `0.1.2`.

## Implementado

### Credenciales y sesiones

- contraseñas BCrypt con costo 12;
- JWT HS256 de corta duración;
- clave HMAC mínima de 256 bits;
- refresh token opaco y aleatorio;
- persistencia exclusiva del hash SHA-256 del refresh token;
- rotación en cada renovación;
- revocación en logout;
- cookie `HttpOnly` y `SameSite=Strict`;
- cookie `Secure` bajo perfil productivo;
- access token conservado solo en memoria del frontend.

### Roles

```text
ADMIN > OPERATOR > DRIVER > REPORT_VIEWER
```

La jerarquía evita duplicar permisos de lectura. Las escrituras siguen protegidas explícitamente:

- `ADMIN`: cotización manual, auditoría y toda operación heredada;
- `OPERATOR`: clientes, domicilios, pedidos, planificación, confirmación y pagos;
- `DRIVER`: lectura operativa y transiciones permitidas;
- `REPORT_VIEWER`: consulta.

La seguridad efectiva está en Spring Security. Ocultar botones en React no concede ni revoca permisos.

### Respuestas y datos

- contrato JSON uniforme para 401 y 403;
- validaciones no exponen stacktrace;
- rechazo seguro de parámetros enum inválidos;
- contraseñas y tokens no aparecen como valores rechazados;
- errores inesperados se registran en servidor y responden un mensaje genérico;
- CORS parametrizado;
- secretos únicamente por variables de entorno;
- administrador automático solo bajo perfil `dev`.

### Correlación

- cada solicitud tiene `X-Request-ID`;
- el valor entrante se reutiliza solo si cumple el formato permitido;
- el identificador se incorpora al MDC;
- producción emite logs JSON correlacionados;
- CORS expone el encabezado al frontend.

### Protección de login

- conteo de intentos por usuario normalizado y origen observado;
- ventana de intentos configurable;
- bloqueo temporal configurable;
- limpieza al autenticar correctamente.

Variables:

- `LOGIN_MAX_ATTEMPTS`;
- `LOGIN_ATTEMPT_WINDOW`;
- `LOGIN_BLOCK_DURATION`.

## Integridad transaccional

### Promociones

La confirmación toma un bloqueo `PESSIMISTIC_WRITE` sobre la promoción antes de revalidar:

- estado;
- vigencia;
- servicio;
- primera compra;
- domicilio;
- cupo total;
- cupo diario;
- cupo mensual.

Esto evita consumos concurrentes contradictorios.

### Pagos

El registro toma un bloqueo `PESSIMISTIC_WRITE` sobre el pedido antes de:

1. consultar pagos anteriores;
2. calcular el saldo;
3. validar el nuevo importe;
4. persistir el pago;
5. actualizar `payment_status`.

Dos pagos simultáneos no pueden superar el precio confirmado.

### Domicilio principal

El reemplazo despromueve y hace flush antes de promover el nuevo domicilio. Esto respeta el índice único parcial sin depender del orden interno de SQL de Hibernate.

## Matriz resumida

| Operación | ADMIN | OPERATOR | DRIVER | REPORT_VIEWER |
|---|---:|---:|---:|---:|
| consultar clientes/pedidos | sí | sí | sí | sí |
| crear/editar cliente | sí | sí | no | no |
| administrar domicilios | sí | sí | no | no |
| crear pedido | sí | sí | no | no |
| editar planificación temprana | sí | sí | no | no |
| cotización manual | sí | no | no | no |
| confirmar precio | sí | sí | no | no |
| cambiar estado | sí | sí | sí | no |
| registrar pago | sí | sí | no | no |
| consultar historial de pagos | sí | sí | sí | sí |
| consultar auditoría | sí | no | no | no |

## Riesgos pendientes

1. El limitador de login está en memoria y se pierde al reiniciar.
2. Varias instancias requieren Redis u otro almacenamiento compartido.
3. La IP observada solo es confiable con proxies explícitamente configurados.
4. No hay MFA.
5. No existe administración de usuarios desde UI.
6. No hay idempotencia para webhooks o proveedores de pago externos.
7. Falta almacenamiento externo seguro para futuras evidencias.
8. Falta gestión centralizada de secretos.
9. Falta SAST, análisis de dependencias y escaneo de imágenes como gates obligatorios.
10. Falta política formal de retención de auditoría y datos personales.
11. Falta TLS y cabeceras perimetrales de un despliegue real.

## Requisitos antes de producción

- perfil `prod`;
- TLS;
- secretos administrados fuera del host de aplicación;
- CORS limitado al dominio real;
- base persistente con backups restaurables;
- rate limiting compartido/perimetral;
- proxy de confianza configurado;
- observabilidad y alertas;
- rotación de logs;
- cuenta administrativa creada por un proceso productivo, no por `DevAdminInitializer`;
- pruebas de restauración;
- procedimiento de rollback;
- política de datos personales y auditoría.
