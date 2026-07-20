# Estado del proyecto

Última actualización documental: 2026-07-20.

## Resumen ejecutivo

El repositorio partió prácticamente vacío. Este corte crea la base técnica y una vertical funcional de Fase 1. El backend ya representa reglas de lavandería: no confunde piezas con unidades equivalentes, no usa punto flotante para dinero, conserva el precio aplicado y registra estados y pagos.

No debe interpretarse la cantidad de archivos como porcentaje real del negocio terminado. Los módulos de producción, logística, finanzas y crecimiento concentran la mayor complejidad pendiente.

## Leyenda

- **Finalizado:** implementado y documentado en este corte.
- **Parcial:** existe modelo o recorrido, pero faltan reglas/pantallas.
- **Pendiente:** no implementado.
- **Bloqueado:** requiere definición estructural antes de continuar.

## Estado por fase

| Fase | Estado | Alcance entregado | Pendiente principal |
|---|---|---|---|
| 0. Diagnóstico | Finalizado | repositorio, stack, arquitectura, dominio, riesgos y plan | revisar resultados de CI |
| Plataforma base | Finalizado | backend, frontend, BD, Flyway, errores, API uniforme, seguridad, auditoría, perfiles, OpenAPI, Docker, CI | lockfile npm y smoke test completo |
| 1. Núcleo operativo | Parcial | usuarios, clientes, domicilios, zonas, equivalencias, precios, promociones iniciales, pedidos, estados, cotización y pagos | UI completa, recepción, peso real, promoción avanzada, agenda básica |
| 2. Producción | Pendiente | estructura prevista | compatibilidad, ciclos, máquinas, bolsas, secado, calidad y relavados |
| 3. Logística | Pendiente | campos de retiro prometido en pedido | rutas, paradas, kilómetros, adicionales, agenda y comunicaciones |
| 4. Finanzas | Pendiente | pago básico | costos, gastos, caja, margen, mano de obra, inversión y proyecciones |
| 5. Crecimiento | Pendiente | semillas promocionales | abonos, comercios, inventario, mantenimiento, reclamos, mercado y tableros |

## Tareas finalizadas

### Ingeniería de plataforma

- [x] Java 21 y Spring Boot 3.
- [x] Maven y organización modular.
- [x] React, TypeScript y Vite.
- [x] PostgreSQL como base principal.
- [x] Flyway sin `ddl-auto` destructivo.
- [x] Manejo centralizado de excepciones.
- [x] Envoltorio uniforme de respuestas exitosas y errores.
- [x] Validación Bean Validation en API.
- [x] Spring Security stateless.
- [x] Access token JWT.
- [x] Refresh token opaco, hasheado y rotativo.
- [x] Cookie `HttpOnly`, `SameSite=Strict` y `Secure` en producción.
- [x] Auditoría JPA y eventos de negocio.
- [x] OpenAPI/Swagger.
- [x] Perfiles dev/test/prod.
- [x] Dockerfiles y Docker Compose.
- [x] Nginx como servidor SPA y proxy.
- [x] GitHub Actions para backend, integración, frontend y contenedores.
- [x] `.env.example` sin secretos reales.
- [x] Zona horaria operativa documentada.

### Núcleo funcional

- [x] Roles iniciales.
- [x] Usuario administrador de desarrollo generado desde variables.
- [x] Zonas iniciales Marcos Paz y Mariano Acosta.
- [x] Cliente con un único domicilio principal activo.
- [x] Preferencias de cliente preparadas en JSON versionable.
- [x] Catálogo de servicios con límites configurables.
- [x] Equivalencias administrables y vigentes.
- [x] Diferenciación entre piezas físicas, grupos y unidades equivalentes.
- [x] Agrupación con redondeo hacia arriba.
- [x] Peso en gramos enteros.
- [x] Precios `NUMERIC`/`BigDecimal`.
- [x] Precio versionado por servicio y zona.
- [x] Precio histórico guardado en pedido.
- [x] Desglose persistido del cálculo.
- [x] Promociones versionadas con vigencia, cupos y una por domicilio.
- [x] Bloqueo de promociones cuyas condiciones aún requieren validación manual.
- [x] Número de pedido `RL-000001` mediante secuencia.
- [x] Estados y transiciones explícitas.
- [x] Historial de estados.
- [x] Pago parcial, total y saldo.
- [x] Auditoría de alta de cliente, pedido, precio, estado y pago.

## Tareas parciales

- [ ] Preferencias de cliente: persistencia disponible; falta formulario especializado y modelo tipado por atributo.
- [ ] Promociones: precio fijo y porcentaje automáticos; faltan crédito, acumulación avanzada, condiciones compuestas y autorización.
- [ ] Pedidos: alta API completa; faltan edición, recepción guiada, peso real, fotos y daños.
- [ ] Estados: secuencia base; falta configuración de pasos opcionales y permisos por transición.
- [ ] Pagos: registro por pedido; faltan comprobantes, reembolsos, caja y cuentas por cobrar.
- [ ] Frontend: login, clientes y estructura; faltan pantallas de pedido y la mayoría de módulos.
- [ ] Logs: patrón estructurado básico en producción; falta correlación de solicitudes.

## Próximas tareas inmediatas

1. Corregir cualquier fallo detectado por el primer CI.
2. Generar y versionar `frontend/package-lock.json`.
3. Agregar pruebas MockMvc de autenticación, permisos y contrato de errores.
4. Implementar actualización de cliente y preferencias tipadas.
5. Implementar pantalla y API de recepción: peso real, conteo, fotos y confirmación de diferencias.
6. Completar administrador de precios, equivalencias y promociones.
7. Agregar listado/filtros de pedidos y agenda básica basada en fechas del pedido.
8. Implementar reglas de cupo promocional bajo concurrencia con bloqueo transaccional.
9. Definir almacenamiento externo de evidencias.
10. Comenzar Fase 2 con matriz de compatibilidad antes de crear ciclos.

## Criterios de aceptación del MVP

| # | Criterio | Estado | Evidencia/pendiente |
|---:|---|---|---|
| 1 | Crear cliente y domicilio | Cumple backend/UI básica | `/clients` |
| 2 | Registrar preferencias | Parcial | JSON persistido, falta formulario completo |
| 3 | Crear pedido | Cumple backend | `/orders` |
| 4 | Registrar piezas físicas | Cumple backend | `order_items.physical_pieces` |
| 5 | Calcular equivalencias | Cumple | calculador y pruebas |
| 6 | Registrar peso real | Pendiente | solo peso declarado |
| 7 | Aplicar primer límite | Parcial | evalúa unidades/peso; falta UX de recepción |
| 8 | Calcular precio vigente | Cumple | `PricingService` |
| 9 | Aplicar promoción válida | Parcial | reglas automáticas seguras |
| 10 | Impedir promociones inválidas | Cumple para reglas implementadas | errores de dominio |
| 11 | Confirmar precio | Cumple | precio histórico |
| 12 | Programar retiro | Parcial | fecha en pedido, sin ruta |
| 13 | Recibir pedido | Parcial | transición disponible, falta pantalla |
| 14 | Evaluar compatibilidad | Pendiente | Fase 2 |
| 15 | Asignar dos pedidos a ciclo | Pendiente | Fase 2 |
| 16 | Impedir sobrepeso de ciclo | Pendiente | Fase 2 |
| 17 | Registrar lavado y secado | Pendiente | Fase 2 |
| 18 | Cambiar estados con trazabilidad | Cumple | historial y auditoría |
| 19 | Registrar pago | Cumple básico | `/payments` |
| 20 | Programar entrega | Parcial | estado/fecha prometida, sin ruta |
| 21 | Entregar y cerrar | Cumple como transición | falta UI y prueba E2E |
| 22 | Calcular costo y margen | Pendiente | Fase 4 |
| 23 | Ver agenda diaria | Pendiente real | vista placeholder |
| 24 | Ver tablero básico | Parcial | estructura sin métricas inventadas |
| 25 | Historial del cliente | Parcial | pedidos aún sin endpoint agregado |

## Riesgos abiertos

- Las promociones complejas no deben automatizarse con una columna booleana; requieren un modelo de condiciones explícito.
- Los cupos se validan al cotizar y se consumen al confirmar; pueden sufrir carrera bajo alta concurrencia. Debe agregarse bloqueo o contador atómico.
- El almacenamiento JSON de preferencias es útil para arrancar, pero no sustituye un modelo consultable para compatibilidad.
- La política de estados está en código; los pasos opcionales deben modelarse sin volver arbitrario el flujo.
- Falta ejecutar CI antes de afirmar compilación y pruebas exitosas.
