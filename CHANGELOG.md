# Changelog

## 0.1.1 - Hardening y flujo operativo completo de Fase 1 base

Fecha: 2026-07-20.

### Agregado

- Respuestas JSON uniformes para autenticación requerida y acceso denegado dentro de Spring Security.
- Identificador `X-Request-ID` validado o generado por solicitud y propagado mediante MDC.
- Correlación incluida en logs JSON del perfil `prod`.
- Protección básica contra intentos repetidos de login por combinación de usuario y origen.
- Configuración de la protección mediante `LOGIN_MAX_ATTEMPTS`, `LOGIN_ATTEMPT_WINDOW` y `LOGIN_BLOCK_DURATION`.
- Contrato tipado de preferencias de cliente, conservando compatibilidad temporal con `preferencesJson`.
- Actualización de perfil, estado y preferencias de cliente mediante `PUT /api/clients/{id}`.
- Catálogo de servicios vigentes mediante `GET /api/catalog/services`.
- Búsqueda paginada de pedidos por número, cliente y estado mediante `GET /api/orders`.
- Resumen operativo de pedidos con cliente, servicio, cantidades, importes y fechas.
- Transiciones permitidas incluidas en el detalle del pedido.
- Interfaz de búsqueda y edición de clientes.
- Interfaz guiada de alta y cotización de pedidos basada en clientes, domicilios, servicios y equivalencias reales.
- Vista previa de piezas físicas, grupos, unidades equivalentes, peso estimado y necesidad de revisión.
- Interfaz de listado, filtros y paginación de pedidos.
- Detalle operativo de pedido con prendas, precio, desglose, estado y pago.
- Confirmación de precio, cambio de estado y registro de pagos desde la interfaz.
- Pruebas unitarias del limitador de intentos de login.
- Pruebas MockMvc de 401, 403, correlación, validación y parámetros inválidos.
- Prueba integrada con PostgreSQL real del flujo cliente → actualización → pedido → búsqueda → confirmación → pago parcial → pago total.
- Pruebas Vitest para el cálculo del borrador de pedido y conversión de fechas.
- Ejecución de `npm test` en CI.

### Modificado

- El frontend deja de depender de Swagger para el recorrido operativo normal disponible en 0.1.1.
- El listado de clientes permite búsqueda por apellido y muestra el domicilio principal.
- El alta de cliente captura preferencias operativas estructuradas.
- La política de estados expone únicamente las transiciones válidas desde el estado actual.
- La página de pedidos reemplaza el placeholder inicial por funciones reales.
- La documentación distingue el flujo ya disponible de recepción, producción, logística y finanzas todavía pendientes.

### Limitaciones conscientes

- El bloqueo de login es local a cada instancia y se pierde al reiniciar; producción distribuida requiere Redis o almacenamiento compartido.
- La protección usa la dirección observada por la aplicación. En producción debe configurarse y validarse correctamente el proxy de confianza.
- La actualización de cliente no modifica domicilios para evitar sobrescribir trazabilidad histórica; el versionado de domicilios queda pendiente.
- No existe aún ajuste manual de cotización para pedidos con `requiresQuote=true`.
- No existe historial/listado de pagos en la interfaz; se muestra el resultado del pago registrado y el estado consolidado del pedido.
- Recepción, peso real, evidencias, compatibilidad, ciclos, rutas, caja, costos, inventario y reclamos siguen fuera de este corte.

## 0.1.0 - Plataforma base y primer corte de Fase 1

Fecha: 2026-07-20.

### Agregado

- Estructura inicial de monolito modular.
- Java 21, Spring Boot 3, Maven, PostgreSQL 16, Flyway y OpenAPI.
- Perfiles `dev`, `test` y `prod`.
- Autenticación JWT y refresh token opaco rotativo.
- Roles y autorización por método.
- Auditoría JPA y eventos sensibles persistidos.
- Módulos de zonas, clientes, domicilios, catálogo, precios, promociones, pedidos y pagos.
- Migraciones V1 a V5 con restricciones, índices y datos iniciales.
- Cálculo de grupos y unidades equivalentes sin perder piezas físicas.
- Validación de límites de servicio y capacidad segura.
- Precio histórico y explicación persistida del cálculo.
- Estados de pedido y política explícita de transiciones.
- Pagos parciales, saldo y estado de pago.
- Frontend mobile first inicial.
- Dockerfiles, Docker Compose, Nginx y pipeline CI.
- Pruebas unitarias e integración Testcontainers con PostgreSQL real.
- Lockfile npm y uso de `npm ci`.
- Guía reproducible para Windows/PowerShell.
- Guía funcional con recorrido completo mediante Swagger.
- Documentación técnica, funcional, operativa y backlog.

### Corregido durante la estabilización

- Nombre de columna SQL reservado `references`, reemplazado por `delivery_references`.
- Tipos de moneda normalizados de `CHAR(3)` a `VARCHAR(3)` para coincidir con JPA y evitar padding.
- Mapeo UUID explícito en uso de promociones.
- Configuración TypeScript de Vite.
- Pruebas de cupos promocionales con mocks numéricos explícitos.
- Selección explícita de HS256 para JWT.
- Pipeline con logs de error útiles, cache y cancelación de ejecuciones obsoletas.
- Imagen frontend reproducible mediante `package-lock.json` y `npm ci`.

### No incluido en 0.1.0

- recepción y pesaje real;
- fotografías y daños preexistentes;
- compatibilidad y ciclos;
- logística y rutas;
- caja completa, costos y rentabilidad;
- abonos, inventario, reclamos y tableros avanzados.

Consultar `docs/PROJECT_STATUS.md` y `docs/ROADMAP.md` para el detalle completo.
