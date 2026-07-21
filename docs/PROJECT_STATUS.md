# Estado integral del proyecto

Última actualización: 2026-07-20.

Versión documentada: `0.3.0`.

## Resumen ejecutivo

Four Bubbles / Ropa Lista dispone de tres verticales utilizables:

1. administración de clientes, domicilios, catálogo, pedidos, precios, promociones, pagos y auditoría;
2. recepción física idempotente con composición/peso reales, inspección, diferencias y decisión;
3. compatibilidad explicable entre pedidos clasificados mediante perfiles versionados y reglas auditables.

Compatibilidad ya no es un booleano libre. Cada evaluación conserva el par ordenado, las versiones de ambos perfiles, la versión del motor, razones y recomendación. Una excepción administrativa no reescribe el resultado original.

El sistema todavía no crea ciclos, asigna máquinas, ejecuta lavado/secado, arma rutas ni calcula caja/costos. Los estados posteriores a `CLASSIFIED` siguen siendo administrativos hasta que existan esos agregados físicos.

## Estado por fase

| Fase | Estado | Entregado | Pendiente principal |
|---|---|---|---|
| Diagnóstico | Finalizado | arquitectura, riesgos, supuestos y roadmap | actualización continua |
| Plataforma | Finalizado para desarrollo | seguridad, PostgreSQL, Flyway, React, Docker, CI y smoke | endurecimiento productivo |
| Administración | Finalizado base | clientes, domicilios, catálogo, pedidos, precios, promociones, pagos y auditoría | timeline y CRUD comercial completo |
| Recepción 0.2.0 | Finalizado base | idempotencia, peso/conteo real, inspección, diferencias, aprobación y evidencia metadata | binarios y correcciones versionadas |
| Compatibilidad 0.3.0 | Finalizado base | perfiles, motor explicable, recomendaciones, historial y excepción | matriz configurable y comparación múltiple |
| Producción | Pendiente | compatibilidad como insumo | ciclos, máquinas, capacidad y calidad |
| Logística | Pendiente | retiro/promesa | rutas, paradas, kilómetros y comunicación |
| Finanzas | Parcial mínimo | cobros e historial | caja, costos, margen y conciliación |
| Crecimiento | Pendiente | catálogo/promociones base | abonos, inventario, mantenimiento y reclamos |

## Finalizado

### Plataforma

- [x] Java 21, Spring Boot 3, Maven.
- [x] React 18, TypeScript, Vite y Vitest.
- [x] PostgreSQL 16.
- [x] Flyway V1-V8 y `ddl-auto=validate`.
- [x] monolito modular, API uniforme, Bean Validation y OpenAPI.
- [x] Actuator, Dockerfiles, Compose y Nginx.
- [x] CI backend/frontend/contenedores.
- [x] runtime smoke con SPA, login y API protegida.
- [x] verificación PowerShell autenticada.

### Seguridad y consistencia

- [x] JWT HS256, refresh opaco hasheado/rotativo y BCrypt.
- [x] cookie segura y access token solo en memoria.
- [x] jerarquía `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- [x] autorización por método y contratos 401/403.
- [x] `X-Request-ID`, MDC y logs correlacionados.
- [x] bloqueo pesimista de promociones, pagos, recepción y compatibilidad.
- [x] idempotencia de recepción.
- [x] excepción de compatibilidad exclusiva de `ADMIN`.

### Administración

- [x] clientes, estado, preferencias y WhatsApp único activo.
- [x] múltiples domicilios, principal único, vigencia, baja e historial.
- [x] servicios, equivalencias, precios y promociones versionados.
- [x] composición declarada, unidades equivalentes y peso declarado.
- [x] precio automático histórico y cotización manual trazable.
- [x] promociones revalidadas al confirmar con control concurrente.
- [x] planificación temprana, búsqueda, detalle, paginación y transiciones.
- [x] pagos parciales/totales, saldo e historial sin sobrecobro concurrente.
- [x] auditoría consultable.

### Recepción

- [x] una recepción por pedido y clave idempotente global.
- [x] recepción solo desde `PICKED_UP` y fecha no futura.
- [x] conteo/peso declarados y reales separados.
- [x] diferencia total y por equivalencia.
- [x] daño, mancha, observaciones, etiqueta y bolsa.
- [x] metadata de evidencia con SHA-256 sin almacenar binarios.
- [x] aprobación por piezas, daño o peso mayor a 250 g/10 %.
- [x] decisión aprobada/rechazada con actor, fecha y notas.
- [x] clasificación automática sin diferencias y cancelación por rechazo.
- [x] UI y pruebas de idempotencia secuencial/concurrente.

### Compatibilidad

- [x] perfil único por pedido y recepción.
- [x] color, material, temperatura, secadora, fragancia y suavizante.
- [x] hipoalergénico, bebé, mascotas, suciedad y exclusividad.
- [x] restricciones del cliente/pedido no relajables por el formulario.
- [x] motor `COMPAT-1` con razones `HARD` y `WARNING`.
- [x] recomendación de tratamiento compartido.
- [x] evaluación por par ordenado y versiones de perfiles/reglas.
- [x] reevaluación histórica al cambiar un perfil.
- [x] bloqueo de ambos pedidos en orden UUID.
- [x] excepción separada, bloqueada transaccionalmente y auditada.
- [x] UI de perfil, candidato, evaluación, razones y excepción.
- [x] 24 pruebas unitarias y 18 integraciones backend en el gate de 0.3.0.

## Parcial

- Evidencias: metadata implementada; falta carga/descarga y object storage.
- Compatibilidad: evaluación por pares implementada; falta matriz administrable y búsqueda avanzada de candidatos.
- Historial del cliente: datos distribuidos; falta timeline agregado.
- Agenda: hay fechas, no rutas ni capacidad diaria.
- Tablero: estructura visual sin KPIs completos.
- Finanzas: cobros sin caja, reembolsos, comprobantes ni conciliación.
- Administración comercial: catálogo en base sin CRUD UI completo.
- Seguridad perimetral: limitador local, sin Redis/WAF.

## Pendiente inmediato — Producción 0.4.0

- máquinas y capacidades;
- programas de lavado/secado;
- ciclo con estado propio;
- asignación de uno o dos pedidos efectivamente compatibles;
- límite de peso y capacidad transaccional;
- bolsas de red y trazabilidad de separación;
- inicio/fin de lavado y secado;
- control de calidad y relavado;
- pruebas concurrentes de asignación.

## Pendiente posterior

### Logística

- franjas, rutas, paradas y orden de visita;
- retiro/entrega, kilómetros, combustible y tiempo;
- incidencias y WhatsApp.

### Finanzas

- caja, arqueo, ingresos/egresos y reembolsos;
- conciliación, costos por pedido/ciclo, mano de obra y amortización;
- margen y rentabilidad.

### Crecimiento

- abonos, comercios y SLA;
- inventario, lotes y equipamiento;
- mantenimiento, reclamos, compensaciones y políticas;
- tableros y alertas.

## Criterios MVP

| # | Criterio | Estado 0.3.0 |
|---:|---|---|
| 1-5 | cliente, preferencias, pedido, piezas y equivalencias | Cumple |
| 6-11 | peso real, límites, precio, promoción y confirmación | Cumple |
| 12 | programar retiro | Parcial, sin ruta |
| 13 | recibir pedido | Cumple |
| 14 | evaluar compatibilidad | Cumple por pares |
| 15 | asignar dos pedidos a ciclo | Pendiente |
| 16 | impedir sobrepeso de ciclo | Pendiente |
| 17 | registrar lavado y secado | Pendiente |
| 18-19 | estados trazables y pago | Cumple |
| 20 | programar entrega | Parcial, sin ruta |
| 21 | entregar y cerrar | Cumple administrativamente |
| 22 | costo y margen | Pendiente |
| 23 | agenda diaria | Pendiente real |
| 24 | tablero básico | Parcial |
| 25 | historial del cliente | Parcial |

Resultado: **16 cumplen, 4 son parciales y 5 están pendientes**.

## Riesgos abiertos

1. Evidencias solo metadata; el objeto debe existir externamente.
2. No hay corrección versionada de una recepción confirmada.
3. La aprobación no es firma digital del cliente.
4. Las reglas de compatibilidad están codificadas en `COMPAT-1`, no administradas desde UI.
5. La excepción permite compatibilidad efectiva, pero no debe usarse como sustituto de una regla correcta.
6. No existe idempotencia para proveedores de pago externos.
7. Rate limiting de login local.
8. Backups, restauración y rollback no automatizados.
9. Compose usa `dev` y falta observabilidad central.
10. Los estados de producción aún no equivalen a ejecución física.

## Próximo orden recomendado

1. Ciclos y máquinas.
2. Logística y agenda.
3. Caja, costos y rentabilidad.
4. Inventario, reclamos y abonos.
