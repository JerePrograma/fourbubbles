# Estado integral del proyecto

Última actualización: 2026-07-20.

Versión documentada: `0.2.0`.

## Resumen ejecutivo

Four Bubbles / Ropa Lista dispone de una plataforma reproducible y de dos verticales utilizables:

1. circuito administrativo de clientes, domicilios, pedidos, precios, promociones, estados, pagos y auditoría;
2. recepción física idempotente con peso/conteo reales, inspección, diferencias, decisión y trazabilidad.

La recepción ya no es un simple cambio de estado. Conserva separados los datos declarados y reales, registra composición por prenda, genera etiqueta, admite bolsa y evidencia metadata, y decide si el pedido puede clasificarse o debe esperar aprobación.

El sistema todavía no ejecuta compatibilidad, ciclos, máquinas, rutas, caja ni costos. Los estados posteriores a `CLASSIFIED` continúan siendo una política administrativa hasta que se implementen sus módulos físicos.

## Estado por fase

| Fase | Estado | Entregado | Pendiente principal |
|---|---|---|---|
| 0. Diagnóstico | Finalizado | arquitectura, dominio, riesgos, supuestos y roadmap | mantener actualizado |
| Plataforma | Finalizado para desarrollo | seguridad, PostgreSQL, Flyway, React, Docker, CI y smoke | endurecimiento productivo |
| 1. Administración | Finalizado base | clientes, domicilios, catálogo, pedidos, precios, promociones, pagos y auditoría | historial agregado y administración comercial |
| 1.5 Recepción | Finalizado base | V7, idempotencia, peso/conteo real, inspección, diferencias, aprobación, etiqueta y evidencia metadata | almacenamiento binario gestionado y correcciones versionadas |
| 2. Compatibilidad/producción | Pendiente | preferencias e ítems reales como insumos | matriz, ciclos, máquinas y calidad |
| 3. Logística | Pendiente | retiro/promesa | rutas, paradas, kilómetros, agenda y WhatsApp |
| 4. Finanzas | Parcial mínimo | cobros e historial | caja, costos, margen y rentabilidad |
| 5. Crecimiento | Pendiente | catálogo/promociones base | abonos, comercios, inventario, mantenimiento y reclamos |

## Finalizado

### Plataforma

- [x] Java 21, Spring Boot 3, Maven.
- [x] React 18, TypeScript, Vite, Vitest.
- [x] PostgreSQL 16.
- [x] Flyway V1–V7.
- [x] `ddl-auto=validate`.
- [x] monolito modular.
- [x] API uniforme, Bean Validation y OpenAPI.
- [x] Actuator, health/readiness.
- [x] Dockerfiles, Compose y Nginx.
- [x] CI backend/frontend/contenedores.
- [x] runtime smoke con login y API protegida.
- [x] verificación PowerShell autenticada.

### Seguridad y consistencia

- [x] JWT HS256 y refresh opaco hasheado/rotativo.
- [x] BCrypt y cookie segura.
- [x] jerarquía `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- [x] autorización por método.
- [x] 401/403 y validaciones uniformes.
- [x] `X-Request-ID`, MDC y logs correlacionados.
- [x] limitador local de login.
- [x] bloqueo pesimista de promociones.
- [x] bloqueo pesimista de pagos.
- [x] bloqueo del pedido en recepción.
- [x] idempotencia de recepción.

### Clientes y domicilios

- [x] alta, búsqueda, consulta y actualización.
- [x] preferencias tipadas.
- [x] WhatsApp único activo.
- [x] múltiples domicilios y principal único.
- [x] vigencia e historial.
- [x] alta, cambio de principal y baja lógica.
- [x] preservación histórica en pedidos.
- [x] interfaz y auditoría.

### Catálogo, precio y promoción

- [x] servicios/equivalencias/precios/promociones versionados.
- [x] piezas físicas separadas de unidades equivalentes.
- [x] peso estimado y restricciones.
- [x] precio automático histórico.
- [x] cotización manual trazable.
- [x] confirmación de precio.
- [x] promoción revalidada y consumida al confirmar.
- [x] cupos y restricción por domicilio protegidos concurrentemente.

### Pedidos

- [x] número legible.
- [x] composición declarada.
- [x] peso declarado.
- [x] límites por unidad/peso/capacidad.
- [x] planificación temprana.
- [x] búsqueda, detalle y paginación.
- [x] 26 estados y política de transiciones.
- [x] historial de estados.
- [x] UI operativa.

### Recepción

- [x] una recepción por pedido.
- [x] `Idempotency-Key` de 16–120 caracteres seguros.
- [x] retry con misma clave devuelve el mismo agregado.
- [x] otra clave no crea una segunda recepción.
- [x] bloqueo transaccional del pedido.
- [x] recepción solo desde `PICKED_UP`.
- [x] fecha real no futura.
- [x] conteo declarado/real.
- [x] peso declarado/real.
- [x] diferencia total y por prenda.
- [x] obligación de informar todos los códigos declarados.
- [x] prendas adicionales solo con equivalencia vigente.
- [x] daño, mancha y observaciones.
- [x] etiqueta `RCV-xxxxxx`.
- [x] código de bolsa opcional.
- [x] evidencia metadata con SHA-256.
- [x] aprobación por diferencia de piezas, daño o peso material.
- [x] umbral mayor a 250 g o 10 %.
- [x] decisión aprobada/rechazada con actor/fecha/notas.
- [x] clasificación automática sin diferencias.
- [x] cancelación por rechazo.
- [x] UI de registro, consulta y decisión.
- [x] pruebas secuenciales y concurrentes de idempotencia.

### Pagos y auditoría

- [x] pagos parciales/totales y saldo.
- [x] historial financiero.
- [x] protección contra sobrecobro concurrente.
- [x] auditoría de cliente, domicilio, pedido, recepción y pago.
- [x] consulta paginada de auditoría.

## Parcial

- [ ] Evidencias: metadata implementada; falta carga/descarga y almacenamiento de objetos administrado.
- [ ] Historial del cliente: datos existen por módulo; falta timeline agregado.
- [ ] Agenda: hay fechas, no rutas ni capacidad diaria.
- [ ] Tablero: estructura visual sin KPIs completos.
- [ ] Finanzas: cobros sin caja, reembolsos, comprobantes ni conciliación.
- [ ] Administración comercial: catálogo en DB, sin CRUD UI completo.
- [ ] Seguridad perimetral: limitador local, sin Redis/WAF.

## Pendiente inmediato — Compatibilidad 0.3.0

- normalizar atributos de tratamiento de la recepción;
- matriz de compatibilidad versionada;
- reglas por color, alergias, bebé, mascotas, fragancia, suavizante, temperatura y contaminación;
- explicación de compatibilidad/incompatibilidad;
- propuestas de combinación;
- bloqueo de incompatibles;
- excepción manual exclusiva de `ADMIN` con motivo;
- pruebas combinatorias y concurrentes.

## Pendiente posterior

### Producción

- máquinas y capacidades;
- programas;
- ciclos compartidos/exclusivos;
- asignación de hasta dos pedidos compatibles;
- bolsas de red;
- lavado, secado, calidad, relavado, doblado y embolsado;
- mantenimiento y capacidad diaria.

### Logística

- franjas;
- rutas/paradas;
- orden de visita;
- retiro/entrega;
- kilometraje, combustible y tiempo;
- incidencias y WhatsApp.

### Finanzas

- caja y arqueo;
- ingresos/egresos;
- reembolsos;
- conciliación;
- costos por pedido/ciclo;
- mano de obra y amortización;
- margen y rentabilidad.

### Crecimiento

- abonos;
- comercios y SLA;
- inventario/lotes;
- equipamiento;
- reclamos y compensaciones;
- políticas y aceptación;
- tableros/alertas.

## Criterios MVP

| # | Criterio | Estado 0.2.0 |
|---:|---|---|
| 1 | Crear cliente y domicilio | Cumple |
| 2 | Registrar preferencias | Cumple |
| 3 | Crear pedido | Cumple |
| 4 | Registrar piezas físicas | Cumple |
| 5 | Calcular equivalencias | Cumple |
| 6 | Registrar peso real | Cumple |
| 7 | Aplicar primer límite | Cumple |
| 8 | Calcular precio vigente | Cumple |
| 9 | Aplicar promoción válida | Cumple para reglas soportadas |
| 10 | Impedir promoción inválida | Cumple |
| 11 | Confirmar precio | Cumple |
| 12 | Programar retiro | Parcial, sin ruta |
| 13 | Recibir pedido | Cumple |
| 14 | Evaluar compatibilidad | Pendiente |
| 15 | Asignar dos pedidos a ciclo | Pendiente |
| 16 | Impedir sobrepeso de ciclo | Pendiente |
| 17 | Registrar lavado y secado | Pendiente |
| 18 | Cambiar estados con trazabilidad | Cumple |
| 19 | Registrar pago | Cumple |
| 20 | Programar entrega | Parcial, sin ruta |
| 21 | Entregar y cerrar | Cumple administrativamente |
| 22 | Calcular costo y margen | Pendiente |
| 23 | Ver agenda diaria | Pendiente real |
| 24 | Ver tablero básico | Parcial |
| 25 | Historial del cliente | Parcial |

Resultado: **15 cumplen, 4 son parciales y 6 están pendientes**.

## Riesgos abiertos

1. Evidencias: solo metadata; el objeto debe existir externamente.
2. No hay endpoint de corrección versionada de una recepción ya confirmada.
3. La aprobación representa decisión operativa, no firma digital del cliente.
4. No existe idempotencia para proveedores de pago externos.
5. Rate limiting de login local.
6. Backups y restauración no automatizados.
7. Compose usa `dev`.
8. Falta observabilidad central y alertas.
9. Falta política formal de datos personales/evidencias.
10. Los estados de producción aún no equivalen a trazabilidad física.

## Próximo orden recomendado

1. Compatibilidad.
2. Ciclos/máquinas.
3. Logística/agenda.
4. Caja/costos/rentabilidad.
5. Inventario/reclamos/abonos.

Ahora sí existen composición y peso reales para alimentar compatibilidad y producción.
