# Estado integral del proyecto

Última actualización: 2026-07-20.

Versión documentada: `0.1.1`.

## Resumen ejecutivo

Four Bubbles / Ropa Lista dispone de una plataforma técnica reproducible y de un flujo operativo inicial utilizable desde la interfaz.

La versión 0.1.1 completa el hardening y el recorrido base de Fase 1:

- autenticación y autorización con contrato uniforme;
- correlación de solicitudes;
- limitación básica de intentos de login;
- clientes con preferencias tipadas y actualización;
- catálogo vigente consultable;
- creación, cotización, búsqueda y detalle de pedidos;
- confirmación de precio;
- transiciones válidas de estado;
- pagos parciales y totales;
- pruebas unitarias, API, integración PostgreSQL y frontend;
- documentación actualizada.

Esto no equivale al MVP completo. Recepción, producción, logística, costos y crecimiento siguen concentrando las reglas más complejas.

## Estado por fase

| Fase | Estado | Entregado | Pendiente principal |
|---|---|---|---|
| 0. Diagnóstico | Finalizado | stack, arquitectura, dominio, riesgos y plan | mantener documentación vigente |
| Plataforma base | Finalizado | Java, React, PostgreSQL, Flyway, seguridad, auditoría, errores, OpenAPI, Docker y CI | hardening productivo distribuido |
| 1. Núcleo operativo | Avanzado | clientes, preferencias, catálogo, pedidos, precios, estados, pagos y UI operativa | recepción, cotización manual, domicilios versionados, administración comercial y agenda |
| 2. Producción | Pendiente | estados preparatorios | compatibilidad, ciclos, máquinas, lavado, secado, calidad y relavados |
| 3. Logística | Pendiente | fechas de retiro y promesa | rutas, paradas, kilómetros, agenda real, retiro, entrega y WhatsApp |
| 4. Finanzas | Pendiente | pagos por pedido | caja, costos, gastos, tiempos, margen, inversión y proyecciones |
| 5. Crecimiento | Pendiente | catálogo y promociones base | abonos, comercios, inventario, mantenimiento, reclamos y tableros |

## Finalizado

### Plataforma y arquitectura

- [x] Java 21, Spring Boot 3 y Maven.
- [x] React 18, TypeScript, Vite, React Router, React Hook Form y Zod.
- [x] Monolito modular por dominio.
- [x] PostgreSQL 16.
- [x] Flyway V1–V5 como única autoridad de esquema.
- [x] `ddl-auto=validate`.
- [x] DTO de entrada y salida.
- [x] Bean Validation.
- [x] errores y respuestas uniformes.
- [x] OpenAPI/Swagger.
- [x] Actuator y health checks.
- [x] perfiles `dev`, `test` y `prod`.
- [x] Dockerfiles multi-stage, Compose y Nginx.
- [x] pipeline de backend, frontend y contenedores.
- [x] lockfile y `npm ci`.

### Seguridad

- [x] Spring Security stateless.
- [x] JWT HS256 de corta duración.
- [x] refresh token opaco, hasheado, rotativo y revocable.
- [x] cookie `HttpOnly`, `SameSite=Strict` y segura en producción.
- [x] BCrypt costo 12.
- [x] roles `ADMIN`, `OPERATOR`, `DRIVER` y `REPORT_VIEWER`.
- [x] autorización por método.
- [x] respuestas JSON uniformes para 401 y 403.
- [x] `X-Request-ID` validado o generado.
- [x] MDC y correlación en logs de producción.
- [x] limitación local de intentos fallidos de login.
- [x] secretos fuera del repositorio.
- [x] errores sin stacktrace ni valores sensibles.

### Auditoría

- [x] campos de creación, modificación, usuario y versión optimista.
- [x] eventos persistidos para alta y actualización de cliente.
- [x] alta y cotización de pedido.
- [x] confirmación de precio.
- [x] cambio de estado.
- [x] registro de pago.

### Clientes

- [x] alta con uno o más domicilios.
- [x] exactamente un domicilio principal activo.
- [x] WhatsApp único entre clientes activos.
- [x] validación de zona.
- [x] búsqueda por apellido.
- [x] consulta individual.
- [x] actualización de perfil y estado.
- [x] preferencias tipadas.
- [x] compatibilidad temporal con `preferencesJson`.
- [x] interfaz de alta, búsqueda y edición.

### Catálogo, precios y promociones

- [x] servicios versionados.
- [x] equivalencias versionadas.
- [x] endpoint de servicios vigentes.
- [x] endpoint de equivalencias vigentes.
- [x] piezas físicas, grupos y unidades equivalentes separadas.
- [x] agrupación con redondeo hacia arriba.
- [x] peso estimado opcional.
- [x] reglas de secadora, presupuesto y ciclo exclusivo.
- [x] precios versionados y desglose persistido.
- [x] promociones automáticas seguras soportadas.
- [x] consumo promocional al confirmar.
- [x] bloqueo de promociones no automatizables.

### Pedidos

- [x] número `RL-000001`.
- [x] cliente, domicilio, servicio, precio y promoción.
- [x] piezas físicas, grupos y unidades equivalentes.
- [x] peso declarado y estimado.
- [x] límites por unidades, peso y capacidad segura.
- [x] precio cotizado y confirmado.
- [x] retiro y promesa.
- [x] 26 estados de negocio.
- [x] política explícita de transiciones.
- [x] historial de estados.
- [x] búsqueda por número, cliente y estado.
- [x] paginación y resumen operativo.
- [x] transiciones permitidas incluidas en el detalle.
- [x] interfaz de alta guiada.
- [x] vista previa de equivalencias.
- [x] interfaz de listado, filtros y detalle.
- [x] confirmación y cambio de estado desde la UI.

### Pagos

- [x] efectivo, transferencia, Mercado Pago y otro.
- [x] pago parcial y total.
- [x] total pagado y saldo.
- [x] bloqueo sin precio confirmado.
- [x] bloqueo de pago no positivo o superior al saldo.
- [x] registro desde el detalle del pedido.

### Pruebas

- [x] JUnit 5 y Mockito.
- [x] Testcontainers PostgreSQL 16.
- [x] Flyway y validación JPA.
- [x] reglas de equivalencias, límites, estados, precios y promociones.
- [x] limitador de login.
- [x] MockMvc para 401, 403, correlación, validación y parámetros inválidos.
- [x] flujo integrado cliente → pedido → confirmación → pagos.
- [x] Vitest para cálculo de borrador de pedido.
- [x] TypeScript, tests y build frontend en CI.
- [x] construcción de imágenes.

### Repositorio

- [x] PR alternativo incompatible cerrado como reemplazado.
- [x] una única línea técnica basada en `main`.
- [x] documentación transversal y changelog.
- [x] scripts PowerShell de inicio y verificación.

## Parcial

- [ ] Domicilios: creación disponible; falta alta de domicilios alternativos, vigencia, cambio de principal e historial.
- [ ] Preferencias: tipadas para operación; falta convertirlas en reglas de compatibilidad de producción.
- [ ] Promociones: falta consumo atómico, créditos, condiciones compuestas y autorizaciones.
- [ ] Cotización: cálculo automático disponible; falta ajuste manual para `requiresQuote=true`.
- [ ] Pedidos: falta edición controlada antes de confirmación y recepción.
- [ ] Pagos: falta historial consultable, comprobantes, reembolsos y caja.
- [ ] Estados: falta permiso específico por transición y evidencia obligatoria por etapa.
- [ ] Auditoría: falta interfaz de consulta.
- [ ] Login: bloqueo local; falta almacenamiento compartido y limitación perimetral.
- [ ] Agenda y tablero: páginas presentes, sin consultas operativas completas.

## Pendiente por fase

### Recepción / cierre de Fase 1

- peso real;
- recuento contra lo declarado;
- diferencias de piezas, peso y precio;
- ajuste manual de cotización;
- aprobación del cliente;
- fotografías y daños preexistentes;
- manchas y observaciones;
- etiquetas y bolsa;
- edición controlada del pedido;
- historial agregado del cliente;
- administración de servicios, equivalencias, precios y promociones.

### Fase 2 — Producción

- matriz de compatibilidad;
- reglas por colores, alergias, bebé, mascotas, fragancia y contaminación;
- propuestas de combinación;
- autorización de excepciones;
- ciclos compartidos de hasta dos pedidos;
- límite de 5 kg por ciclo;
- ciclos exclusivos;
- máquinas y programas;
- bolsas de red y trazabilidad física;
- lavado, secado, calidad, relavado, doblado y embolsado;
- capacidad diaria y mantenimiento.

### Fase 3 — Logística

- radios, barrios y restricciones avanzadas;
- rutas y paradas;
- orden de visita;
- retiro y entrega;
- kilómetros, combustible, tiempo y costo;
- adicionales fuera de ruta;
- agenda diaria real;
- plantillas e historial WhatsApp.

### Fase 4 — Finanzas

- caja diaria y arqueos;
- ingresos y egresos;
- cuentas por cobrar;
- costos por pedido y ciclo;
- gastos fijos;
- tiempo y mano de obra;
- margen y resultado;
- rentabilidad por zona, servicio y promoción;
- inversión, amortización y escenarios.

### Fase 5 — Crecimiento

- abonos y suscripciones;
- clientes comerciales y SLA;
- inventario y movimientos;
- equipamiento y mantenimiento;
- reclamos y compensaciones;
- políticas versionadas;
- referencias de mercado;
- tableros y alertas.

## Criterios de aceptación del MVP

| # | Criterio | Estado |
|---:|---|---|
| 1 | Crear cliente y domicilio | Cumple |
| 2 | Registrar preferencias | Cumple |
| 3 | Crear pedido | Cumple UI/backend |
| 4 | Registrar piezas físicas | Cumple |
| 5 | Calcular equivalencias | Cumple |
| 6 | Registrar peso real | Pendiente |
| 7 | Aplicar primer límite | Cumple dominio/UI |
| 8 | Calcular precio vigente | Cumple |
| 9 | Aplicar promoción válida | Parcial |
| 10 | Impedir promoción inválida | Cumple reglas soportadas |
| 11 | Confirmar precio | Cumple UI/backend |
| 12 | Programar retiro | Parcial, sin ruta |
| 13 | Recibir pedido | Parcial, solo transición |
| 14 | Evaluar compatibilidad | Pendiente |
| 15 | Asignar dos pedidos | Pendiente |
| 16 | Impedir sobrepeso de ciclo | Pendiente |
| 17 | Registrar lavado y secado | Pendiente |
| 18 | Cambiar estados con trazabilidad | Cumple |
| 19 | Registrar pago | Cumple UI/backend |
| 20 | Programar entrega | Parcial, sin ruta |
| 21 | Entregar y cerrar | Cumple en flujo de estados |
| 22 | Calcular costo y margen | Pendiente |
| 23 | Ver agenda diaria | Pendiente real |
| 24 | Ver tablero básico | Parcial |
| 25 | Historial del cliente | Parcial |

Resultado 0.1.1: **12 cumplen, 6 son parciales y 7 están pendientes**.

## Riesgos abiertos

1. Los cupos promocionales requieren bloqueo transaccional o contador atómico.
2. El limitador de login no se comparte entre instancias.
3. La dirección de origen depende de una configuración correcta del proxy.
4. Las preferencias deben integrarse a un modelo consultable de compatibilidad.
5. Falta idempotencia para pagos externos y webhooks.
6. Falta almacenamiento externo y seguro de evidencias.
7. Falta automatización y prueba periódica de backups.
8. Falta versionado formal de API.
9. Falta historial consultable de pagos y auditoría.
10. El Compose actual usa perfil `dev` y no representa producción.
11. Agenda y tablero no son fuentes de verdad completas.
12. Los pedidos con presupuesto manual no tienen ajuste operativo.

## Próximo orden recomendado

### 0.1.2 — Cierre administrativo de Fase 1

1. versionado de domicilios;
2. historial de pagos y comprobantes;
3. ajuste manual de cotización con motivo y autorización;
4. edición controlada de pedido antes de confirmar;
5. consumo promocional atómico;
6. auditoría consultable;
7. rate limiter compartido o adaptador Redis;
8. smoke test HTTP del Compose iniciado.

### 0.2.0 — Recepción

1. peso real;
2. recuento y diferencia;
3. fotografías y daños;
4. manchas e instrucciones;
5. aprobación de diferencia de precio;
6. etiqueta y bolsa;
7. historial operativo del cliente;
8. administración comercial.

### 0.3.0 — Producción

1. atributos de compatibilidad;
2. matriz y explicación de incompatibilidades;
3. máquinas y programas;
4. ciclos compartidos y exclusivos;
5. capacidad y sobrepeso;
6. lavado, secado, calidad y relavado;
7. trazabilidad física.

Después continuar con logística, finanzas y crecimiento. Implementar tableros antes de datos confiables produciría decoración, no gestión.

## Definición de terminado

Cada corte debe cerrar con:

- compilación exitosa;
- migraciones aplicables y validación JPA;
- pruebas de reglas críticas;
- frontend con `npm ci`, lint, tests y build;
- imágenes construibles;
- documentación y changelog actualizados;
- riesgos y pendientes explícitos;
- CI verde antes de integrar a `main`.
