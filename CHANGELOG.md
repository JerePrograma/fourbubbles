# Changelog

## 0.1.2 - Cierre administrativo de Fase 1

Fecha: 2026-07-20.

### Agregado

- Migración Flyway `V6__administrative_closure.sql`.
- Vigencia de domicilios mediante `valid_from` y `valid_to`.
- Historial de domicilios activos e inactivos.
- Alta de domicilios alternativos desde API e interfaz.
- Cambio controlado de domicilio principal.
- Baja lógica de domicilios conservando referencias históricas.
- Cotización manual exclusiva de `ADMIN` con importe, motivo, actor y fecha.
- Conservación separada del precio automático original.
- Edición de retiro, promesa y notas durante `INQUIRY` o `QUOTED`.
- Historial de pagos por pedido.
- Búsqueda administrativa de auditoría por entidad, identificador y acción.
- Interfaz de auditoría.
- Jerarquía de roles `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- Bloqueo pesimista de promociones durante la confirmación.
- Revalidación de vigencia, servicio, primera compra, domicilio y cupos al confirmar.
- Bloqueo pesimista del pedido durante el registro de pagos.
- Prueba concurrente que impide consumir dos veces una promoción restringida.
- Prueba concurrente que impide superar el saldo con pagos simultáneos.
- Workflow permanente de smoke runtime con Compose, login y API protegida.
- Verificación local PowerShell de frontend, autenticación y API protegida.

### Modificado

- El detalle de pedido permite registrar cotización manual y planificación temprana.
- El detalle muestra historial de pagos.
- La edición de cliente administra domicilios actuales e históricos.
- El menú expone auditoría solo cuando corresponde al rol.
- La promoción deja de considerarse consumida durante la cotización y se confirma bajo bloqueo transaccional.
- Los pagos se serializan por pedido para calcular el saldo sobre una fuente consistente.
- La documentación transversal pasa a describir 0.1.2.

### Corregido

- Persistencia explícita del domicilio antes de auditar su UUID generado.
- `flush` intermedio al reemplazar el domicilio principal para respetar el índice único parcial.
- Orden de domicilios: principal primero y luego vigencia descendente.
- Bloqueo de planificación fuera de `INQUIRY` y `QUOTED`.
- Fixtures de promociones unitarias con estado `ACTIVE` explícito.
- Prueba de autorización con payload válido para comprobar realmente el 403.
- Auditoría temporal coherente con `OffsetDateTime`.

### Validación

- 16 pruebas unitarias backend.
- Integraciones sobre PostgreSQL 16 y Flyway V1–V6.
- Contrato API, autorización, flujo operativo y flujo administrativo.
- Concurrencia de promociones y pagos.
- TypeScript estricto, Vitest y build Vite.
- Validación y construcción de imágenes Docker.
- Arranque completo, readiness, SPA, login y API autenticada.

### Limitaciones conscientes

- Recepción física, peso real, evidencias y aprobación de diferencias no están implementados.
- No hay todavía caja, reembolsos ni comprobantes externos.
- Falta idempotencia para proveedores de pago y webhooks.
- El limitador de login es local a cada instancia.
- El perfil `dev` y Compose no representan una topología productiva.

## 0.1.1 - Hardening y flujo operativo completo de Fase 1 base

Fecha: 2026-07-20.

### Agregado

- Respuestas JSON uniformes para autenticación requerida y acceso denegado.
- `X-Request-ID`, MDC y correlación de logs.
- Protección básica contra intentos repetidos de login.
- Preferencias tipadas y actualización de clientes.
- Catálogo de servicios vigente.
- Búsqueda, resumen y detalle de pedidos.
- Interfaz de alta, cotización, estados y pagos.
- Pruebas MockMvc, PostgreSQL y Vitest.

### Corregido

- Proveedor temporal de auditoría con `OffsetDateTime` UTC.
- Renderizado seguro de valores JSON desconocidos en TypeScript.

## 0.1.0 - Plataforma base y primer corte de Fase 1

Fecha: 2026-07-20.

### Agregado

- Monolito modular Java 21 y Spring Boot 3.
- PostgreSQL 16, JPA/Hibernate y Flyway V1–V5.
- Seguridad JWT y refresh token opaco.
- Auditoría, clientes, catálogo, precios, promociones, pedidos y pagos.
- React 18, TypeScript y Vite.
- Docker Compose, Nginx y GitHub Actions.
- Documentación técnica y funcional inicial.
