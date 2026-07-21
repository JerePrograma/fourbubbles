# Changelog

## 0.3.0 - Compatibilidad explicable

Fecha: 2026-07-20.

### Agregado

- Migración Flyway V8.
- Perfil de tratamiento único por pedido y recepción.
- Atributos de color, material, temperatura, secadora, fragancia, suavizante, tratamiento hipoalergénico, ropa de bebé, mascotas, suciedad pesada y exclusividad.
- Motor `COMPAT-1` con razones `HARD` y `WARNING`.
- Recomendación compartida de temperatura, secadora, suavizante, fragancia, programa y modo.
- Evaluación histórica por par ordenado, versiones de perfil y versión de reglas.
- Excepción administrativa separada y auditada.
- UI de perfil, selección de candidato, evaluación, explicación y excepción.
- 25 pruebas unitarias y 19 integraciones backend.
- Prueba concurrente A/B contra B/A con reutilización del mismo snapshot.

### Corregido o endurecido

- Las preferencias del cliente no pueden relajarse desde el formulario de compatibilidad.
- La prohibición de secadora o suavizante se conserva.
- El tratamiento hipoalergénico se conserva y fuerza fragancia `NONE`.
- La exclusividad del pedido o cliente se conserva.
- El orden UUID usa la representación canónica y coincide con el constraint de PostgreSQL.
- Las evaluaciones bloquean ambos pedidos en orden estable para evitar duplicados concurrentes.
- La autorización de excepción bloquea la evaluación antes de modificarla.
- Una excepción no cambia el resultado original; solo altera la compatibilidad efectiva.

### Pendiente

- ciclos y máquinas;
- asignación de pedidos compatibles a ciclos;
- capacidad, lavado, secado y control de calidad;
- almacenamiento binario de evidencias;
- logística, caja, costos, inventario y reclamos.

## 0.2.0 - Recepción física idempotente

Fecha: 2026-07-20.

- Migración Flyway V7.
- Recepción única por pedido e `Idempotency-Key`.
- Peso y conteo reales separados de la declaración.
- Diferencias por pedido y prenda.
- Daños, manchas, observaciones, etiqueta, bolsa y evidencia metadata.
- Política de aprobación por piezas, daño o peso material.
- Decisión aprobada/rechazada con actor, fecha y notas.
- UI y pruebas de idempotencia secuencial/concurrente.

## 0.1.2 - Cierre administrativo de Fase 1

Fecha: 2026-07-20.

- Domicilios versionados e historial.
- Cotización manual trazable.
- Planificación temprana controlada.
- Promociones y pagos con control de concurrencia.
- Historial financiero y auditoría.
- Jerarquía RBAC.
- Verificación local autenticada.

## 0.1.1 - Hardening y flujo operativo

Fecha: 2026-07-20.

- Contratos de seguridad, correlación y protección de login.
- Preferencias tipadas.
- UI operativa de clientes, pedidos, estados y pagos.

## 0.1.0 - Plataforma y núcleo inicial

Fecha: 2026-07-20.

- Java/Spring, React, PostgreSQL, Flyway, seguridad, catálogo, pedidos, pagos, Docker y CI.
