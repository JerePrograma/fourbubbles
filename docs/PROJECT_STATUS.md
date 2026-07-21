# Estado integral del proyecto

Última actualización: 2026-07-20.

Versión documentada: `0.4.0`.

## Resumen ejecutivo

Four Bubbles / Ropa Lista dispone de cuatro verticales utilizables:

1. administración de clientes, domicilios, catálogo, pedidos, precios, promociones, pagos y auditoría;
2. recepción física idempotente con composición/peso reales, inspección, diferencias y decisión;
3. compatibilidad explicable entre pedidos clasificados;
4. producción física base con máquinas, programas, ciclos, capacidad, lavado, secado y control de calidad.

La producción ya no se representa únicamente mediante cambios manuales de estado. Un ciclo conserva máquina, programa, pesos, pedidos, separación, ejecución e historial. Compartir un ciclo exige compatibilidad vigente, capacidad y ausencia de exclusividad.

Aún no existen logística real, caja/costos, inventario de insumos, mantenimiento completo ni almacenamiento binario de evidencias.

## Estado por fase

| Fase | Estado | Entregado | Pendiente principal |
|---|---|---|---|
| Plataforma | Finalizado para desarrollo | seguridad, PostgreSQL, Flyway, React, Docker, CI y smoke | hardening productivo |
| Administración | Finalizado base | clientes, domicilios, catálogo, pedidos, precios, promociones, pagos y auditoría | timeline y CRUD comercial completo |
| Recepción 0.2.0 | Finalizado base | idempotencia, peso/conteo real, inspección y decisión | binarios y correcciones versionadas |
| Compatibilidad 0.3.0 | Finalizado base | perfiles, motor, historial y excepción | matriz configurable y búsqueda avanzada |
| Producción 0.4.0 | Finalizado base | máquinas, programas, ciclos, capacidad, ejecución y calidad | insumos, mantenimiento y optimización |
| Logística | Pendiente | fechas de retiro/promesa | rutas, paradas, kilómetros y comunicación |
| Finanzas | Parcial mínimo | cobros e historial | caja, costos, margen y conciliación |
| Crecimiento | Pendiente | catálogo/promociones base | abonos, inventario, reclamos y SLA |

## Finalizado

### Plataforma

- [x] Java 21, Spring Boot 3 y Maven.
- [x] React 18, TypeScript, Vite y Vitest.
- [x] PostgreSQL 16.
- [x] Flyway V1-V10 y `ddl-auto=validate`.
- [x] monolito modular, API uniforme, Bean Validation y OpenAPI.
- [x] Actuator, Dockerfiles, Compose y Nginx.
- [x] CI backend/frontend/contenedores.
- [x] runtime smoke con SPA, login y API protegida.
- [x] verificación PowerShell autenticada.

### Seguridad y concurrencia

- [x] JWT, refresh opaco rotativo, BCrypt y cookie segura.
- [x] jerarquía `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- [x] autorización por método y contratos 401/403.
- [x] `X-Request-ID`, MDC y logs correlacionados.
- [x] bloqueos pesimistas en promociones, pagos, recepción, compatibilidad y producción.
- [x] advisory locks para claves idempotentes de ciclo.
- [x] constraints únicos como última defensa.

### Administración

- [x] clientes, preferencias, WhatsApp único y estado.
- [x] múltiples domicilios, principal único, vigencia e historial.
- [x] servicios, equivalencias, precios y promociones versionados.
- [x] composición declarada, peso, límites y cotización.
- [x] promociones revalidadas al confirmar.
- [x] pagos parciales/totales sin sobrecobro concurrente.
- [x] auditoría consultable.

### Recepción

- [x] recepción única e idempotente.
- [x] peso/conteo reales separados de la declaración.
- [x] diferencias totales y por equivalencia.
- [x] daño, mancha, observaciones, etiqueta y bolsa.
- [x] metadata de evidencia con SHA-256.
- [x] aprobación/rechazo y transición a `CLASSIFIED`/`CANCELLED`.

### Compatibilidad

- [x] perfil efectivo por pedido/recepción.
- [x] restricciones del cliente/pedido no relajables.
- [x] motor `COMPAT-1` con razones y recomendación.
- [x] evaluación por par canónico y versiones.
- [x] concurrencia A/B-B/A sin duplicar snapshot.
- [x] excepción separada, exclusiva de `ADMIN` y auditada.

### Producción

- [x] máquinas lavadora/secadora con capacidad y estado.
- [x] programas WASH/DRY.
- [x] seeds operativos iniciales.
- [x] parámetros técnicos congelados tras primer uso.
- [x] ciclo con número humano e idempotencia.
- [x] uno o dos pedidos por ciclo.
- [x] una máquina por ciclo activo.
- [x] un pedido por etapa activa.
- [x] peso real y capacidad transaccional.
- [x] programa compatible con todos los perfiles.
- [x] evaluación vigente efectiva para compartir.
- [x] exclusividad inviolable.
- [x] separación requerida cuando existe override.
- [x] planificación, inicio, finalización y cancelación previa.
- [x] lavado y secado registrados físicamente.
- [x] omisión explícita de secadora cuando el perfil no la admite.
- [x] control de calidad `PASS`/`REWASH`.
- [x] historial de ciclo/pedido y auditoría.
- [x] UI operativa y configuración básica.
- [x] 28 unitarias y 25 integraciones backend.

## Parcial

- Evidencias: metadata; falta object storage y binarios.
- Compatibilidad: por pares; falta búsqueda/optimización de lotes.
- Producción: flujo base; falta consumo de insumos, mantenimiento detallado y métricas OEE.
- Administración de máquinas/programas: API completa base; UI orientada a alta, no edición avanzada.
- Agenda: hay fechas; no rutas ni capacidad diaria logística.
- Tablero: estructura sin KPIs completos.
- Finanzas: cobros sin caja, costos ni conciliación.
- Historial del cliente: datos dispersos sin timeline agregado.

## Pendiente inmediato — Logística 0.5.0

- franjas horarias;
- rutas y paradas;
- conductor y orden de visita;
- retiro/entrega reales;
- kilómetros, combustible y tiempo;
- incidencias;
- agenda diaria y capacidad;
- plantillas/enlaces de WhatsApp;
- pruebas concurrentes de asignación de ruta.

## Pendiente posterior

### Finanzas 0.6.0

- caja y arqueo;
- ingresos/egresos y reembolsos;
- conciliación;
- costos de insumos, energía, agua, transporte y mano de obra;
- costo por ciclo/pedido;
- margen y rentabilidad.

### Crecimiento 0.7.0

- abonos, comercios y SLA;
- inventario y lotes;
- mantenimiento completo;
- reclamos, evidencias y compensaciones;
- tableros y alertas.

## Criterios MVP

| # | Criterio | Estado 0.4.0 |
|---:|---|---|
| 1-11 | cliente, pedido, recepción, precio y promoción | Cumple |
| 12 | programar retiro | Parcial, sin ruta |
| 13 | recibir pedido | Cumple |
| 14 | evaluar compatibilidad | Cumple |
| 15 | asignar hasta dos pedidos a ciclo | Cumple |
| 16 | impedir sobrepeso | Cumple |
| 17 | registrar lavado y secado | Cumple |
| 18-19 | estados trazables y pago | Cumple |
| 20 | programar entrega | Parcial, sin ruta |
| 21 | entregar y cerrar | Cumple administrativamente |
| 22 | costo y margen | Pendiente |
| 23 | agenda diaria | Pendiente real |
| 24 | tablero básico | Parcial |
| 25 | historial del cliente | Parcial |

Resultado: **19 cumplen, 4 son parciales y 2 están pendientes**.

## Riesgos abiertos

1. Evidencias solo metadata.
2. No hay corrección versionada de recepción confirmada.
3. Reglas de compatibilidad codificadas, no administrables.
4. La separación requerida se registra, pero no existe trazabilidad física de bolsas dentro de una máquina.
5. No se registra secado natural como etapa independiente.
6. Sin inventario/consumo de insumos.
7. Mantenimiento solo como estado de máquina.
8. Sin idempotencia para webhooks de pago.
9. Rate limiting de login local.
10. Backups, restore y rollback no automatizados.
11. Compose usa perfil `dev`.

## Próximo orden recomendado

1. Logística y agenda.
2. Caja, costos y rentabilidad.
3. Inventario y mantenimiento.
4. Reclamos, abonos y tableros.
