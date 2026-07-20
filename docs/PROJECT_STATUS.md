# Estado integral del proyecto

Última actualización: 2026-07-20.

Versión documentada: `0.1.2`.

## Resumen ejecutivo

Four Bubbles / Ropa Lista dispone de una plataforma reproducible y de un circuito administrativo utilizable desde la interfaz:

- autenticación, autorización, sesiones y auditoría;
- clientes, preferencias y domicilios versionados;
- catálogo comercial versionado;
- creación, cálculo, búsqueda y operación de pedidos;
- precio automático y cotización manual trazable;
- promociones revalidadas bajo bloqueo transaccional;
- pagos parciales/totales e historial;
- protección contra sobrecobro concurrente;
- pruebas unitarias, integración PostgreSQL, frontend, contenedores y runtime.

La versión 0.1.2 **no es todavía el MVP físico completo**. El mayor faltante es recepción: peso y conteo reales, diferencias, fotografías, daños, manchas, aprobación del cliente, etiquetas e idempotencia. Sin esa capa, los estados de producción representan un flujo administrativo, no trazabilidad física completa.

## Estado por fase

| Fase | Estado | Entregado | Pendiente principal |
|---|---|---|---|
| 0. Diagnóstico | Finalizado | arquitectura, dominio, riesgos, supuestos y roadmap | mantener documentación vigente |
| Plataforma base | Finalizado para desarrollo | Java, React, PostgreSQL, Flyway, seguridad, errores, auditoría, Docker y CI | controles productivos distribuidos |
| 1. Núcleo administrativo | Finalizado base | clientes, domicilios, catálogo, pedidos, precios, promociones, pagos, auditoría y UI | recepción física e historial agregado |
| 2. Producción | Pendiente | estados preparatorios | compatibilidad, ciclos, máquinas, lavado, secado, calidad y relavado |
| 3. Logística | Pendiente | retiro/promesa en pedido | rutas, paradas, kilómetros, agenda real y WhatsApp |
| 4. Finanzas | Parcial mínimo | pagos por pedido | caja, costos, gastos, margen y rentabilidad |
| 5. Crecimiento | Pendiente | semillas comerciales | abonos, comercios, inventario, mantenimiento, reclamos y tableros |

## Finalizado en `main` al integrar 0.1.2

### Plataforma y arquitectura

- [x] Java 21, Spring Boot 3 y Maven.
- [x] React 18, TypeScript, Vite, React Router, React Hook Form y Zod.
- [x] Vitest.
- [x] monolito modular por dominio.
- [x] PostgreSQL 16.
- [x] Flyway V1–V6.
- [x] `ddl-auto=validate`.
- [x] DTO de entrada y salida.
- [x] Bean Validation.
- [x] errores y respuestas uniformes.
- [x] OpenAPI/Swagger.
- [x] Actuator y health checks.
- [x] perfiles `dev`, `test` y `prod`.
- [x] Dockerfiles multi-stage, Compose y Nginx.
- [x] `package-lock.json` y `npm ci`.

### Seguridad

- [x] Spring Security stateless.
- [x] JWT HS256 corto.
- [x] refresh token opaco, hasheado, rotativo y revocable.
- [x] cookie `HttpOnly`, `SameSite=Strict` y segura en producción.
- [x] BCrypt costo 12.
- [x] roles `ADMIN`, `OPERATOR`, `DRIVER`, `REPORT_VIEWER`.
- [x] jerarquía `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- [x] autorización por método.
- [x] operaciones manuales sensibles reservadas a `ADMIN`.
- [x] 401 y 403 con contrato JSON.
- [x] `X-Request-ID`, MDC y logs correlacionados.
- [x] limitación local de intentos de login.
- [x] secretos fuera de Git.

### Clientes y domicilios

- [x] alta, consulta, búsqueda y actualización de cliente.
- [x] WhatsApp único entre clientes activos.
- [x] preferencias tipadas.
- [x] uno o más domicilios.
- [x] exactamente un domicilio principal activo.
- [x] alta de domicilio alternativo.
- [x] cambio de principal con flush intermedio seguro.
- [x] baja lógica del domicilio.
- [x] vigencia `valid_from`/`valid_to`.
- [x] historial de domicilios inactivos.
- [x] preservación de pedidos vinculados a domicilios históricos.
- [x] interfaz de gestión.
- [x] auditoría de cambios.

### Catálogo, precios y promociones

- [x] servicios y equivalencias versionados.
- [x] piezas físicas, grupos y unidades equivalentes separados.
- [x] peso estimado opcional.
- [x] restricciones de secadora, presupuesto y ciclo exclusivo.
- [x] precios versionados y seleccionados por vigencia/zona.
- [x] precio histórico en el pedido.
- [x] desglose explicable.
- [x] promociones automáticas soportadas.
- [x] bloqueo de promociones que requieren validación manual no modelada.
- [x] consumo al confirmar, no al cotizar.
- [x] bloqueo pesimista de la promoción.
- [x] revalidación de estado, vigencia, servicio, primera compra, domicilio y cupos.
- [x] prueba concurrente de consumo único.

### Pedidos

- [x] número `RL-000001`.
- [x] cliente, domicilio, servicio, promoción y precio aplicado.
- [x] piezas físicas, grupos y unidades equivalentes.
- [x] peso declarado.
- [x] límites por unidades, peso y capacidad segura.
- [x] precio automático separado del precio manual.
- [x] cotización manual con motivo, actor y fecha.
- [x] edición de retiro, promesa y notas solo en `INQUIRY`/`QUOTED`.
- [x] confirmación histórica del precio.
- [x] 26 estados y política explícita de transiciones.
- [x] historial de estados.
- [x] listado, filtros, paginación y detalle.
- [x] transiciones permitidas calculadas por backend.
- [x] interfaz operativa.

### Pagos

- [x] efectivo, transferencia, Mercado Pago y otro.
- [x] pagos parciales y totales.
- [x] cálculo de total y saldo.
- [x] bloqueo sin precio confirmado.
- [x] bloqueo de importe no positivo o superior al saldo.
- [x] historial por pedido.
- [x] actor, fecha, medio, referencia, notas y estado.
- [x] bloqueo pesimista del pedido durante el cobro.
- [x] prueba concurrente que evita sobrecobro.
- [x] interfaz de registro e historial.

### Auditoría

- [x] auditoría JPA de creación/modificación/actor/versión.
- [x] eventos de cliente, domicilio, pedido, precio, estado y pago.
- [x] valores anterior/nuevo serializados.
- [x] búsqueda paginada por entidad, identificador y acción.
- [x] interfaz exclusiva de `ADMIN`.

### Pruebas y entrega

- [x] 16 pruebas unitarias backend.
- [x] Testcontainers PostgreSQL 16.
- [x] Flyway V1–V6 y validación JPA.
- [x] contratos 401, 403, validación y correlación.
- [x] flujo operativo integrado.
- [x] flujo administrativo integrado.
- [x] autorización por jerarquía.
- [x] concurrencia promocional.
- [x] concurrencia financiera.
- [x] TypeScript estricto, Vitest y build.
- [x] construcción de imágenes.
- [x] smoke runtime: Compose, readiness, SPA, login y API protegida.
- [x] verificación local PowerShell autenticada.

## Parcial

- [ ] Preferencias: capturadas y tipadas; falta usarlas en compatibilidad de producción.
- [ ] Estados: trazables; falta evidencia obligatoria y permisos específicos por transición.
- [ ] Historial del cliente: hay domicilios, pedidos, pagos y auditoría por separado; falta una vista agregada.
- [ ] Agenda: fechas en pedidos, pero no existe agenda logística como fuente de verdad.
- [ ] Tablero: estructura visual, sin KPIs operativos completos.
- [ ] Finanzas: cobros por pedido, sin caja, reembolsos, cuentas corrientes ni comprobantes externos.
- [ ] Seguridad perimetral: limitador local, sin Redis/WAF ni política distribuida.

## Pendiente inmediato — Recepción 0.2.0

- peso real;
- conteo real de piezas;
- diferencias contra lo declarado;
- modificación controlada de composición;
- daños preexistentes;
- manchas y observaciones;
- fotografías/evidencias;
- aceptación o rechazo del cliente ante diferencias;
- recalculo posterior a recepción;
- etiqueta y bolsa;
- clave de idempotencia para evitar recepción duplicada;
- vista de historial agregado del cliente.

## Pendiente por fases

### Fase 2 — Producción

- matriz de compatibilidad;
- reglas por color, alergias, bebé, mascotas, fragancia, suavizante y contaminación;
- propuestas de combinación;
- bloqueo de incompatibles;
- excepción autorizada con motivo;
- ciclos compartidos y exclusivos;
- límite real de carga;
- máquinas y programas;
- bolsas de red;
- lavado, secado, calidad, relavado, doblado y embolsado;
- mantenimiento y capacidad diaria.

### Fase 3 — Logística

- zonas/radios avanzados;
- franjas horarias;
- rutas y paradas;
- orden de visita;
- retiro y entrega;
- kilómetros, combustible, tiempo y costo;
- incidencias;
- agenda diaria;
- plantillas e historial WhatsApp.

### Fase 4 — Finanzas

- apertura y cierre de caja;
- arqueo y diferencias;
- ingresos/egresos;
- cuentas por cobrar;
- reembolsos;
- costos por pedido y ciclo;
- insumos, servicios, transporte, mano de obra y amortización;
- margen por pedido, servicio, zona y promoción;
- proyecciones e inversión.

### Fase 5 — Crecimiento

- abonos y suscripciones;
- clientes comerciales y SLA;
- inventario y lotes;
- equipamiento y mantenimiento;
- reclamos, evidencias, resolución y compensación;
- políticas versionadas y aceptación;
- mercado y competencia;
- tableros y alertas.

## Criterios de aceptación del MVP

| # | Criterio | Estado 0.1.2 |
|---:|---|---|
| 1 | Crear cliente y domicilio | Cumple |
| 2 | Registrar preferencias | Cumple |
| 3 | Crear pedido | Cumple |
| 4 | Registrar piezas físicas | Cumple |
| 5 | Calcular equivalencias | Cumple |
| 6 | Registrar peso real | Pendiente |
| 7 | Aplicar primer límite | Cumple |
| 8 | Calcular precio vigente | Cumple |
| 9 | Aplicar promoción válida | Cumple para reglas soportadas |
| 10 | Impedir promoción inválida | Cumple |
| 11 | Confirmar precio | Cumple |
| 12 | Programar retiro | Parcial, sin ruta |
| 13 | Recibir pedido | Parcial, solo estado |
| 14 | Evaluar compatibilidad | Pendiente |
| 15 | Asignar dos pedidos a ciclo | Pendiente |
| 16 | Impedir sobrepeso de ciclo | Pendiente |
| 17 | Registrar lavado y secado | Pendiente |
| 18 | Cambiar estados con trazabilidad | Cumple |
| 19 | Registrar pago | Cumple |
| 20 | Programar entrega | Parcial, sin ruta |
| 21 | Entregar y cerrar | Cumple en estados |
| 22 | Calcular costo y margen | Pendiente |
| 23 | Ver agenda diaria | Pendiente real |
| 24 | Ver tablero básico | Parcial |
| 25 | Historial del cliente | Parcial |

Resultado 0.1.2: **13 cumplen, 5 son parciales y 7 están pendientes**.

## Riesgos abiertos

1. No existe idempotencia de proveedores de pago ni webhooks.
2. El rate limit de login se pierde al reiniciar y no se comparte entre instancias.
3. La IP depende de una configuración correcta del proxy de confianza.
4. No existe almacenamiento seguro externo para evidencias.
5. No hay backups automáticos ni prueba periódica de restauración.
6. No existe versionado formal de API pública.
7. Falta administración UI de servicios, precios, equivalencias y promociones.
8. El Compose usa perfil `dev`.
9. Falta observabilidad central y alertas.
10. El flujo físico no debe inferirse únicamente por estados administrativos.

## Próximo orden recomendado

1. Recepción idempotente y evidencias.
2. Historial agregado del cliente.
3. Compatibilidad.
4. Ciclos y máquinas.
5. Logística y agenda.
6. Caja, costos y rentabilidad.
7. Inventario, reclamos, abonos y crecimiento.

No conviene saltar directamente a rutas o ciclos: sin recepción real, la composición y el peso que alimentarían esas decisiones no son confiables.
