# Estado integral del proyecto

Última actualización: 2026-07-20.

Versión documentada: `0.1.0`.

## Resumen ejecutivo

Four Bubbles / Ropa Lista partió de un repositorio prácticamente vacío. La versión 0.1.0 entrega:

- plataforma técnica reproducible;
- arquitectura de monolito modular;
- seguridad y auditoría base;
- esquema PostgreSQL gestionado íntegramente por Flyway;
- primera vertical funcional de clientes, catálogo comercial, pedidos, estados y pagos;
- frontend inicial mobile first;
- pruebas unitarias e integración con PostgreSQL real;
- contenedores y pipeline CI;
- documentación funcional, técnica, operativa y de uso.

La plataforma base está finalizada. La Fase 1 está parcialmente implementada. Las fases de producción, logística, finanzas y crecimiento continúan pendientes.

La cantidad de clases o tablas no debe confundirse con porcentaje de negocio terminado: compatibilidad, ciclos, logística, costos y reclamos concentran reglas críticas aún no implementadas.

## Leyenda

- **Finalizado:** implementado, validado y documentado en 0.1.0.
- **Parcial:** existe una base funcional, pero faltan recorridos, reglas o pantallas.
- **Pendiente:** no existe implementación funcional utilizable.
- **Bloqueado:** requiere una decisión estructural previa. No hay bloqueos estructurales activos en este corte.

## Estado por fase

| Fase | Estado | Entregado | Pendiente principal |
|---|---|---|---|
| 0. Diagnóstico | Finalizado | stack, arquitectura, dominio, modelo inicial, riesgos y plan | mantenerlo actualizado |
| Plataforma base | Finalizado | backend, frontend, PostgreSQL, Flyway, seguridad, auditoría, errores, perfiles, OpenAPI, Docker, Nginx y CI | hardening productivo |
| 1. Núcleo operativo | Parcial | usuarios, zonas, clientes, domicilios, equivalencias, precios, promociones seguras, pedidos, estados, cotización y pagos | UI de pedidos, recepción, peso real, evidencias, administración comercial y agenda |
| 2. Producción | Pendiente | estados preparatorios en pedido | compatibilidad, ciclos, máquinas, bolsas, secado, calidad y relavados |
| 3. Logística | Pendiente | fechas de retiro/promesa en pedido | rutas, paradas, kilómetros, agenda, retiro, entrega y WhatsApp |
| 4. Finanzas | Pendiente | pago básico por pedido | caja, costos, gastos, tiempo, margen, inversión y proyecciones |
| 5. Crecimiento | Pendiente | catálogo y semillas promocionales | abonos, comercios, inventario, mantenimiento, reclamos, mercado y tableros |

## Alcance finalizado

### Repositorio y entrega

- [x] Repositorio inicializado desde cero.
- [x] Código organizado por módulos funcionales.
- [x] Historial de desarrollo consolidable mediante squash en `main`.
- [x] `.gitignore`, `.dockerignore`, `.editorconfig` y `.env.example`.
- [x] README y changelog.
- [x] ADR y documentación transversal.
- [x] Scripts PowerShell de inicio y smoke test local.

### Backend y arquitectura

- [x] Java 21 LTS.
- [x] Spring Boot 3.
- [x] Maven.
- [x] Monolito modular.
- [x] Separación API, aplicación, dominio, persistencia e infraestructura donde corresponde.
- [x] Spring Web.
- [x] Spring Validation.
- [x] Spring Data JPA/Hibernate.
- [x] Transacciones en casos de uso de escritura.
- [x] DTO de entrada y salida; entidades JPA no expuestas.
- [x] Manejo global de excepciones.
- [x] Respuesta API uniforme.
- [x] OpenAPI/Swagger.
- [x] Actuator y health checks.
- [x] Perfiles `dev`, `test` y `prod`.
- [x] Zona horaria `America/Argentina/Buenos_Aires`.

### Base de datos

- [x] PostgreSQL 16 como única base principal.
- [x] Flyway desde la primera tabla.
- [x] `ddl-auto=validate`; sin modificación automática de esquema.
- [x] Migraciones V1 a V5.
- [x] UUID, claves foráneas, restricciones, índices e índices parciales.
- [x] Secuencia de número de pedido.
- [x] Eliminación lógica en entidades que deben preservar historial.
- [x] Vigencia temporal de configuración comercial.
- [x] Dinero con `NUMERIC(15,2)`/`BigDecimal`.
- [x] Peso en gramos enteros.
- [x] Código de moneda `VARCHAR(3)`, inicialmente ARS.
- [x] Seeds reproducibles.

### Seguridad

- [x] Spring Security stateless.
- [x] JWT de acceso de corta duración con HS256 explícito.
- [x] Secreto mínimo de 256 bits.
- [x] Refresh token opaco y aleatorio.
- [x] Persistencia exclusiva del hash SHA-256 del refresh token.
- [x] Rotación y revocación.
- [x] Cookie `HttpOnly`, `SameSite=Strict` y segura en producción.
- [x] BCrypt para contraseñas.
- [x] Roles `ADMIN`, `OPERATOR`, `DRIVER` y `REPORT_VIEWER`.
- [x] Autorización por método.
- [x] CORS parametrizado.
- [x] Usuario administrador generado solo bajo perfil `dev`.
- [x] Secretos fuera del repositorio.
- [x] Errores sin stacktrace ni valores sensibles rechazados.

### Auditoría

- [x] Campos de creación, modificación, usuario y versión optimista.
- [x] Eventos persistidos para operaciones sensibles implementadas.
- [x] Alta de cliente auditada.
- [x] Alta y cotización de pedido auditadas.
- [x] Confirmación de precio auditada.
- [x] Cambio de estado auditado.
- [x] Pago auditado.

### Clientes y domicilios

- [x] Alta de cliente.
- [x] Consulta individual.
- [x] Búsqueda paginada.
- [x] Nombre, apellido, teléfono, WhatsApp, correo, fuente, notas y preferencias JSON.
- [x] WhatsApp único entre clientes activos.
- [x] Uno o más domicilios.
- [x] Un único domicilio principal activo.
- [x] Domicilio vinculado a zona habilitada.
- [x] Zonas iniciales Marcos Paz y Mariano Acosta.
- [x] Interfaz básica de listado y alta.

### Catálogo y equivalencias

- [x] Servicios versionados y configurables.
- [x] Equivalencias versionadas y administrables en base.
- [x] 21 equivalencias iniciales.
- [x] Separación de piezas físicas, grupos y unidades equivalentes.
- [x] Agrupación con redondeo hacia arriba.
- [x] Peso estimado opcional.
- [x] Admisión de lavado común y secadora.
- [x] Requerimiento de ciclo exclusivo o presupuesto.
- [x] Endpoint de consulta de equivalencias vigentes.

### Precios y promociones

- [x] 11 servicios y precios iniciales configurados como datos.
- [x] Precio por servicio, zona opcional y vigencia.
- [x] Motivo y responsable.
- [x] Precio histórico conservado en pedido.
- [x] Desglose explicable persistido.
- [x] Nueve promociones iniciales configuradas.
- [x] Precio fijo y porcentaje para reglas automáticas soportadas.
- [x] Primera compra.
- [x] Una promoción por domicilio.
- [x] Cupos total, diario y mensual modelados.
- [x] No acumulación por defecto.
- [x] Bloqueo de promociones con condiciones aún no automatizables.
- [x] Consumo promocional al confirmar, no al cotizar.

### Pedidos

- [x] Número legible `RL-000001`.
- [x] Cliente, domicilio, zona indirecta, servicio y precio aplicado.
- [x] Promoción opcional.
- [x] Piezas físicas, grupos y unidades equivalentes.
- [x] Peso declarado.
- [x] Modalidad exclusiva.
- [x] Requerimiento de presupuesto.
- [x] Límite alcanzado.
- [x] Precio cotizado y confirmado.
- [x] Retiro programado y fecha prometida.
- [x] Observaciones.
- [x] 26 estados de negocio.
- [x] Política explícita de transiciones.
- [x] Historial de estados con usuario, fecha, observación, ubicación y notificación.
- [x] Regla de 12 unidades, 2.500 gramos y capacidad segura.

### Pagos

- [x] Efectivo, transferencia, Mercado Pago y otro configurable.
- [x] Importe, fecha, referencia, notas, pedido, cliente y usuario.
- [x] Pago parcial y total.
- [x] Total pagado y saldo.
- [x] Bloqueo de pago sin precio confirmado.
- [x] Bloqueo de importes no positivos o superiores al saldo.

### Frontend

- [x] React 18.
- [x] TypeScript.
- [x] Vite.
- [x] React Router.
- [x] React Hook Form y Zod.
- [x] Cliente HTTP centralizado.
- [x] Access token solo en memoria.
- [x] Renovación transparente y deduplicada.
- [x] Rutas protegidas.
- [x] Login y logout.
- [x] Shell responsive/mobile first.
- [x] Clientes y alta de cliente.
- [x] Tablero, agenda y pedidos identificados honestamente como incompletos.
- [x] `package-lock.json` versionado.
- [x] Instalación reproducible con `npm ci`.

### Infraestructura y CI

- [x] Dockerfile backend multi-stage.
- [x] Dockerfile frontend multi-stage.
- [x] PostgreSQL, backend y frontend en Compose.
- [x] Volumen persistente local.
- [x] Health checks y dependencias por salud.
- [x] Nginx como servidor SPA y proxy `/api`.
- [x] Pipeline backend, frontend y contenedores.
- [x] Java 21, Node 22, cache Maven/npm.
- [x] Cancelación de ejecuciones obsoletas.
- [x] Build frontend e imágenes con lockfile.
- [x] Workflows temporales de diagnóstico retirados.

### Pruebas

- [x] JUnit 5.
- [x] Mockito.
- [x] Spring Boot Test.
- [x] Testcontainers PostgreSQL.
- [x] Maven Failsafe para `*IT`.
- [x] 13 casos unitarios.
- [x] Contexto completo, Flyway y validación JPA en PostgreSQL real.
- [x] Lint TypeScript y build Vite.
- [x] Validación y build de Compose.

### Documentación

- [x] README reproducible.
- [x] Arquitectura.
- [x] Modelo de datos.
- [x] Contrato API.
- [x] Seguridad.
- [x] Pruebas y matriz original de 30 reglas.
- [x] Operación y despliegue.
- [x] Guía Windows/PowerShell.
- [x] Guía funcional con JSON copiables.
- [x] Supuestos.
- [x] Roadmap.
- [x] Changelog.
- [x] ADR de monolito, configuración versionada y tokens.

## Alcance parcial

- [ ] Preferencias: persisten como JSON; falta modelo tipado y formulario especializado.
- [ ] Clientes: falta actualización, domicilios alternativos avanzados, métricas e historial agregado.
- [ ] Catálogo: falta interfaz administrativa para crear nuevas versiones.
- [ ] Promociones: faltan créditos, condiciones compuestas, autorizaciones y concurrencia atómica.
- [ ] Pedidos: falta edición, listado/filtros y flujo completo de interfaz.
- [ ] Recepción: falta peso real, fotografías, daños, manchas y aprobación de diferencia.
- [ ] Estados: falta permiso específico por transición y configuración de pasos opcionales.
- [ ] Pagos: faltan comprobantes, reembolsos, deuda y caja.
- [ ] Auditoría: falta interfaz de consulta y comparación amigable de valores.
- [ ] Logs: falta identificador de correlación y agregación central.
- [ ] Agenda/tablero: existen páginas estructurales, no métricas operativas reales.

## Alcance pendiente por módulo

### Fase 2 — Producción

- matriz de compatibilidad;
- colores, suciedad, alergias, fragancia, suavizante, bebé, mascotas y contaminación;
- propuesta de pedidos compatibles;
- bloqueo de incompatibles;
- excepción manual con motivo y autorización;
- ciclos compartidos de hasta dos pedidos y 5 kg;
- ciclos exclusivos;
- lavarropas, secarropas y programas;
- bolsas reutilizables y bolsas de red;
- productos y dosificación;
- lavado, secado, calidad, relavado, doblado y embolsado;
- capacidad diaria y mantenimiento.

### Fase 3 — Logística

- localidades, barrios y radios avanzados;
- rutas por fecha, zona, tipo y franja;
- paradas y orden;
- retiros y entregas;
- kilómetros, combustible, costo y tiempo;
- bloqueo/autorización de recorrido ineficiente;
- adicionales fuera de ruta;
- agenda diaria real;
- plantillas e historial WhatsApp;
- enlaces `wa.me`.

### Fase 4 — Finanzas

- costos por pedido y ciclo;
- gastos fijos;
- inventario de costos históricos;
- mano de obra y tiempos;
- caja diaria, ingresos, egresos y arqueos;
- cuentas por cobrar;
- créditos y reembolsos;
- margen de contribución y resultado operativo;
- costo por kg/unidad/cliente;
- rentabilidad por promoción, zona y ruta;
- inversión, amortización y recuperación;
- simulador de escenarios.

### Fase 5 — Crecimiento

- planes y suscripciones;
- usos, vencimientos, pausa, renovación y mora;
- clientes comerciales, cuenta corriente y SLA;
- inventario y movimientos;
- equipamiento, fallas y mantenimiento;
- reclamos, evidencias, resolución y compensación;
- políticas versionadas y aceptación;
- competencia y referencias de mercado;
- tableros semanales/mensuales y alertas.

## Criterios de aceptación del MVP

| # | Criterio | Estado | Evidencia o pendiente |
|---:|---|---|---|
| 1 | Crear cliente y domicilio | Cumple | UI y `/clients` |
| 2 | Registrar preferencias | Parcial | JSON persistido |
| 3 | Crear pedido | Cumple backend | `/orders` |
| 4 | Registrar piezas físicas | Cumple | `order_items.physical_pieces` |
| 5 | Calcular equivalencias | Cumple | dominio y pruebas |
| 6 | Registrar peso real | Pendiente | solo peso declarado |
| 7 | Aplicar primer límite | Cumple dominio | falta UX recepción |
| 8 | Calcular precio vigente | Cumple | `PricingService` |
| 9 | Aplicar promoción válida | Parcial | reglas automáticas seguras |
| 10 | Impedir promoción inválida | Cumple para reglas soportadas | errores de dominio |
| 11 | Confirmar precio | Cumple | endpoint e histórico |
| 12 | Programar retiro | Parcial | fecha sin ruta |
| 13 | Recibir pedido | Parcial | transición sin pantalla |
| 14 | Evaluar compatibilidad | Pendiente | Fase 2 |
| 15 | Asignar hasta dos pedidos | Pendiente | Fase 2 |
| 16 | Impedir sobrepeso de ciclo | Pendiente | Fase 2 |
| 17 | Registrar lavado y secado | Pendiente | Fase 2 |
| 18 | Cambiar estados con trazabilidad | Cumple | historial y auditoría |
| 19 | Registrar pago | Cumple básico | `/payments` |
| 20 | Programar entrega | Parcial | fecha/estado sin ruta |
| 21 | Entregar y cerrar | Cumple en dominio | falta UI/E2E |
| 22 | Calcular costo y margen | Pendiente | Fase 4 |
| 23 | Ver agenda diaria | Pendiente real | página estructural |
| 24 | Ver tablero básico | Parcial | sin métricas inventadas |
| 25 | Historial del cliente | Parcial | consulta de cliente; falta agregado operacional |

Resultado: 10 criterios cumplen, 8 son parciales y 7 continúan pendientes. El MVP completo todavía no está terminado.

## Próximo orden de ejecución recomendado

### Corte 0.1.1 — Hardening de Fase 1

1. MockMvc de autenticación, errores y permisos.
2. Pruebas integradas cliente → pedido → confirmación → pago.
3. Actualización de cliente y preferencias tipadas.
4. Listado, búsqueda y filtros de pedidos.
5. Pantalla de creación de pedido.
6. Correlación de logs.
7. Protección contra fuerza bruta/rate limiting.

### Corte 0.2.0 — Recepción y control

1. Peso real.
2. Conteo y recálculo.
3. Fotografías y daños preexistentes.
4. Diferencia de precio y aprobación.
5. Etiquetas e identificación de bolsa.
6. Historial completo del cliente.
7. Administradores de precios, equivalencias y promociones.

### Corte 0.3.0 — Producción

1. Modelo tipado de preferencias y atributos de compatibilidad.
2. Matriz de compatibilidad.
3. Equipos y programas.
4. Ciclos compartidos/exclusivos.
5. Capacidad y sobrepeso.
6. Secado, calidad y relavado.
7. Bolsas y trazabilidad física.

Después continuar con logística, finanzas y crecimiento en ese orden. Implementar tableros antes de datos confiables produciría decoración, no gestión.

## Riesgos abiertos

1. Los cupos promocionales requieren consumo atómico o bloqueo transaccional.
2. Las preferencias JSON no son suficientes para consultas de compatibilidad.
3. Las promociones complejas necesitan un modelo explícito de condiciones.
4. Falta idempotencia para pagos externos y webhooks.
5. Falta almacenamiento externo de evidencias.
6. Falta automatización y prueba periódica de backups.
7. Falta rate limiting de autenticación.
8. Falta política formal de versionado API.
9. Falta observabilidad y correlación.
10. El Compose actual usa `dev` y no debe considerarse despliegue productivo.
11. Agenda y tablero no deben usarse como fuente de verdad hasta implementar sus consultas.
12. El flujo de pedido todavía depende de Swagger en varias operaciones.

## Definición de terminado para próximos cortes

Cada corte debe finalizar con:

- compilación exitosa;
- migraciones nuevas aplicables sobre PostgreSQL vacío y actualización controlada;
- validación JPA exitosa;
- pruebas de las reglas críticas;
- frontend con `npm ci`, lint y build;
- imágenes construibles;
- documentación y changelog actualizados;
- lista explícita de riesgos y pendientes;
- evidencia CI verde antes de integrar a `main`.
