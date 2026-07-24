# Decisiones técnicas

Este archivo indexa ADR existentes y registra decisiones posteriores respaldadas por código. No reemplaza los ADR.

## ADR existentes

- [`ADR 0001: monolito modular`](adr/0001-modular-monolith.md).
- [`ADR 0002: configuración comercial versionada`](adr/0002-versioned-commercial-configuration.md).
- [`ADR 0003: JWT corto y refresh opaco`](adr/0003-token-strategy.md).

## ADR-004: Flyway como autoridad del esquema

- Estado: ACEPTADO.
- Fecha o commit de referencia: versión inicial `0.1.0`; vigente en `6f6d3cd`.
- Contexto: el modelo requiere restricciones e historial reproducibles.
- Decisión: Flyway define el esquema; Hibernate usa `ddl-auto=validate`.
- Alternativas observables: generación automática de esquema o edición de migraciones publicadas.
- Consecuencias: `V1`–`V8` son inmutables; todo cambio nuevo es aditivo en `V9+`.
- Archivos relacionados: `application.yml`, `db/migration/`.
- Evidencia: configuración JPA/Flyway, workflows y `Verify-Local.ps1`.

## ADR-005: separar declaración, recepción y snapshots históricos

- Estado: ACEPTADO.
- Fecha o commit de referencia: `0.2.0` y `0.3.0`.
- Contexto: lo declarado antes del retiro puede diferir de lo recibido y las reglas pueden evolucionar.
- Decisión: `order_items` conserva declaración; `order_receptions` y `reception_items` conservan realidad física; `compatibility_evaluations` conserva razones y recomendación JSONB por versión.
- Alternativas observables: sobrescribir el pedido o recalcular resultados históricos.
- Consecuencias: mayor almacenamiento y mapeo, a cambio de auditoría y explicabilidad.
- Archivos relacionados: `ReceptionService`, `V7__order_reception.sql`, `CompatibilityService`, `V8__compatibility_engine.sql`.
- Evidencia: constraints, DTO y servicios.

## ADR-006: identidad y bloqueo canónico para compatibilidad

- Estado: ACEPTADO.
- Fecha o commit de referencia: `0.3.0`.
- Contexto: A/B y B/A representan el mismo par y pueden ejecutarse simultáneamente.
- Decisión: ordenar UUID por su representación canónica, bloquear ambos pedidos en ese orden y aplicar una restricción única por versiones/regla.
- Alternativas observables: orden de entrada o bloqueo sin orden estable.
- Consecuencias: evita snapshots duplicados e interbloqueos por orden inverso.
- Archivos relacionados: `CompatibilityService.compareCanonical`, `ConcurrentCompatibilityIT`, `V8__compatibility_engine.sql`.
- Evidencia: código, constraint `ck_compatibility_ordering` y prueba concurrente.

## ADR-007: reglas de compatibilidad versionadas e inmutables

- Estado: ACEPTADO.
- Fecha o commit de referencia: `0.3.0`.
- Contexto: una evaluación histórica no puede cambiar porque el algoritmo futuro cambie.
- Decisión: `CompatibilityEngine.RULE_VERSION="COMPAT-1"` forma parte de la identidad persistida.
- Alternativas observables: reutilizar el mismo identificador con semántica nueva.
- Consecuencias: cualquier cambio semántico exige `COMPAT-2` o posterior y pruebas de convivencia.
- Archivos relacionados: `CompatibilityEngine`, `CompatibilityService`, `compatibility_evaluations`.
- Evidencia: constante, restricción única y snapshots JSONB.

## ADR-008: excepción separada del resultado original

- Estado: ACEPTADO.
- Fecha o commit de referencia: `0.3.0`.
- Contexto: una decisión administrativa puede aceptar un riesgo sin falsificar el motor.
- Decisión: persistir una excepción 0..1 por evaluación; derivar `effectivelyCompatible = compatible OR exception`.
- Alternativas observables: cambiar `compatible` o eliminar razones.
- Consecuencias: producción futura debe distinguir compatibilidad nativa de excepción.
- Archivos relacionados: `CompatibilityException`, `CompatibilityService.authorizeException`, `compatibility_exceptions`.
- Evidencia: servicio, tabla y API.

## ADR-009: entorno local idempotente y aislado

- Estado: ACEPTADO.
- Fecha o commit de referencia: hardening del 2026-07-21, base `6f6d3cd`.
- Contexto: coexistencia con otros proyectos Docker y puertos ocupados.
- Decisión: `COMPOSE_PROJECT_NAME`, puertos host configurables, publicación en loopback, preflight y no detener recursos ajenos.
- Alternativas observables: nombres/puertos fijos o limpieza indiscriminada.
- Consecuencias: los procedimientos deben resolver puertos efectivos y usar scripts oficiales.
- Archivos relacionados: `.env.example`, `docker-compose.yml`, `Start-Local.ps1`, `Local.Common.ps1`.
- Evidencia: scripts, pruebas PowerShell y runtime smoke con puertos alternativos.
