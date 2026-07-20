# Changelog

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
