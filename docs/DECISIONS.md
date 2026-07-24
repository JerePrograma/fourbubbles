# Decisiones técnicas

## ADR existentes

- `adr/0001-modular-monolith.md`;
- `adr/0002-versioned-commercial-configuration.md`;
- `adr/0003-token-strategy.md`.

## ADR-004 — Flyway como autoridad

- Estado: ACEPTADO.
- Decisión: Flyway define esquema; Hibernate valida.
- Consecuencia: `V1`–`V10` son inmutables; siguiente cambio `V11+`.

## ADR-005 — Declaración, recepción y snapshots separados

- Estado: ACEPTADO.
- Decisión: no sobrescribir pedido declarado; persistir recepción y evaluaciones históricas.
- Consecuencia: mayor almacenamiento a cambio de trazabilidad.

## ADR-006 — Orden UUID canónico

- Estado: ACEPTADO.
- Decisión: bloquear pares en orden canónico.
- Consecuencia: evita duplicados e interbloqueos A/B contra B/A.

## ADR-007 — Reglas de compatibilidad versionadas

- Estado: ACEPTADO.
- Decisión: `COMPAT-1` identifica semántica inmutable.
- Consecuencia: cambios futuros usan `COMPAT-2+`.

## ADR-008 — Excepción separada

- Estado: ACEPTADO.
- Decisión: la excepción no cambia `compatible`, razones ni recomendación.
- Consecuencia: producción distingue compatibilidad nativa de excepción.

## ADR-009 — Entorno local aislado

- Estado: ACEPTADO.
- Decisión: proyecto Compose explícito, puertos host configurables, loopback, preflight y no detener recursos ajenos.

## ADR-010 — Ciclos idempotentes y bloqueos coordinados

- Estado: ACEPTADO.
- Fecha: 2026-07-24.
- Contexto: dos solicitudes pueden competir por la misma clave, máquina o pedido.
- Decisión: advisory lock por clave, bloqueo pesimista de máquina/programa/pedidos, orden UUID y constraints parciales.
- Consecuencia: mayor serialización; evita doble asignación y ciclos duplicados.
- Evidencia: `ProductionService.createCycle`, `ConcurrentProductionIT`, `V9__production_cycles.sql`.

## ADR-011 — Parámetros técnicos de programas usados inmutables

- Estado: ACEPTADO.
- Fecha: 2026-07-24.
- Decisión: `V10` bloquea cambios de etapa, tipo, duración, temperatura, gentle, suavizante y fragancia cuando el programa ya fue usado.
- Consecuencia: nombre, notas y activación pueden variar; la interpretación técnica histórica no.

## ADR-012 — Excepción productiva con marca de separación

- Estado: ACEPTADO CON LIMITACIÓN.
- Fecha: 2026-07-24.
- Decisión: dos pedidos originalmente incompatibles pero exceptuados pueden compartir ciclo con `separationRequired=true`.
- Consecuencia: la marca preserva el riesgo, pero no implementa trazabilidad física; esa capacidad sigue pendiente.
