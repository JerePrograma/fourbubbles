# Changelog

## 0.4.0 - Producción física base

Fecha: 2026-07-20.

### Agregado

- Flyway V9 con máquinas, programas, ciclos, asignaciones e historial.
- Flyway V10 para congelar parámetros técnicos de programas ya utilizados.
- Máquinas `WASHER` y `DRYER` con capacidad, estado y vigencia.
- Programas `WASH` y `DRY` con duración y parámetros seguros.
- Ciclos `PLANNED`, `RUNNING`, `COMPLETED` y `CANCELLED`.
- Planificación idempotente con `Idempotency-Key` y advisory lock PostgreSQL.
- Uno o dos pedidos por ciclo.
- Peso planificado basado en recepción real y peso real al completar.
- Capacidad de máquina aplicada antes de planificar y al completar.
- Máquina única por ciclo activo.
- Pedido único por etapa activa.
- Compatibilidad efectiva vigente para ciclos compartidos.
- Separación requerida cuando la combinación depende de excepción.
- Estados `WAITING_DRY`, ciclos de lavado/secado y control de calidad.
- Resultado de calidad `PASS` o `REWASH`.
- Historial propio de ciclo y del pedido.
- API de configuración y operación.
- Pantalla de Producción con planificación, ejecución, calidad y alta básica de máquinas/programas.

### Endurecido

- Un pedido exclusivo no puede compartir ciclo aunque exista excepción.
- El programa no puede ser más agresivo que el perfil de ningún pedido.
- Los parámetros técnicos de un programa usado son inmutables.
- Máquina, programa y pedidos se bloquean transaccionalmente.
- Dos claves distintas compitiendo por una máquina permiten una sola planificación.
- Dos solicitudes concurrentes con la misma clave/payload reciben el mismo ciclo.
- La cancelación solo se permite antes de iniciar.
- El secado mecánico no se inventa: perfiles que no admiten secadora pasan de lavado a calidad.

### Pruebas

- 28 unitarias.
- 25 integraciones PostgreSQL/Flyway.
- 53 casos backend totales.
- Flujo lavado → secado → calidad.
- Capacidad excedida.
- Idempotencia concurrente.
- Competencia por máquina.
- Permisos de lectura/configuración/operación.
- Frontend TypeScript, Vitest y build.

### Pendiente

- rutas, agenda, kilómetros y WhatsApp;
- caja, costos, margen y conciliación;
- inventario y consumo de insumos;
- mantenimiento completo;
- object storage de evidencias;
- abonos, reclamos y compensaciones.

## 0.3.0 - Compatibilidad explicable

Fecha: 2026-07-20.

- Flyway V8.
- Perfil efectivo por pedido/recepción.
- Motor `COMPAT-1`, razones y recomendación.
- Evaluaciones históricas y excepciones administrativas.
- Orden UUID canónico y concurrencia A/B-B/A.
- 25 unitarias y 19 integraciones.

## 0.2.0 - Recepción física idempotente

Fecha: 2026-07-20.

- Flyway V7.
- Recepción única por pedido e `Idempotency-Key`.
- Peso/conteo real, diferencias, inspección y decisión.
- Etiqueta, bolsa y evidencia metadata.

## 0.1.2 - Cierre administrativo de Fase 1

Fecha: 2026-07-20.

- Domicilios versionados.
- Cotización manual y planificación.
- Promociones/pagos concurrentes.
- Historial financiero, auditoría y RBAC.

## 0.1.1 - Hardening y flujo operativo

Fecha: 2026-07-20.

- Seguridad, correlación, preferencias tipadas y UI operativa.

## 0.1.0 - Plataforma y núcleo inicial

Fecha: 2026-07-20.

- Java/Spring, React, PostgreSQL, Flyway, seguridad, catálogo, pedidos, pagos, Docker y CI.
