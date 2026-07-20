# Changelog

## 0.1.0 - Fase 1, primer corte

### Agregado

- Estructura inicial de monolito modular.
- Configuración Java 21, Spring Boot, Maven, PostgreSQL, Flyway y OpenAPI.
- Perfiles `dev`, `test` y `prod`.
- Autenticación JWT y refresh token opaco rotativo.
- Roles y autorización por método.
- Auditoría genérica persistida.
- Módulos de zonas, clientes, domicilios, catálogo, precios, promociones, pedidos y pagos.
- Migraciones V1 a V5 con restricciones, índices y datos iniciales.
- Cálculo de grupos y unidades equivalentes.
- Validación de límites de servicio y capacidad segura.
- Precio histórico y explicación persistida del cálculo.
- Estados de pedido y política de transiciones.
- Pagos parciales y saldo.
- Frontend mobile first inicial.
- Dockerfiles, Docker Compose, Nginx y pipeline CI.
- Pruebas unitarias e integración Testcontainers.
- Documentación técnica y funcional inicial.

### Pendiente

Consultar `docs/PROJECT_STATUS.md` y `docs/ROADMAP.md`.
