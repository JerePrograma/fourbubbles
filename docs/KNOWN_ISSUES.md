# Incidencias y deuda técnica conocidas

Fecha: 2026-07-26. Fuente: `origin/main`.

## KI-001 — `CatalogController` accede a repositorios

- Estado: `VERIFICADO`.
- Impacto: excepción al límite API → aplicación → persistencia.
- Próximo paso: servicio de consulta con tests.
- Severidad: media.

## KI-002 — Versiones de manifiestos desalineadas

- Estado: `VERIFICADO`.
- Evidencia: versión funcional `0.4.2`; Maven/npm conservan `0.1.0`.
- Próximo paso: actualizar Maven, npm y lockfile mediante generación reproducible y agregar gate de consistencia.
- Severidad: media.

## KI-003 — Evidencias solo metadata

- Estado: `VERIFICADO`.
- Impacto: una referencia puede apuntar a un objeto inexistente.
- Próximo paso: object storage privado, autorización, integridad y retención.
- Severidad: alta antes de producción.

## KI-004 — Producción física ausente

- Estado: `RESUELTO` en `0.4.0`.

## KI-005 — Despliegue productivo no definido

- Estado: `VERIFICADO`.
- Faltan TLS, secretos, backup/restore, recursos, observabilidad y rollback.
- Severidad: `BLOQUEANTE` para datos reales.

## KI-006 — Sin E2E de navegador

- Estado: `VERIFICADO`.
- Impacto: navegación, formularios y accesibilidad pueden degradarse.
- Próximo paso: Playwright para producción, separación y métricas.
- Severidad: media.

## KI-007 — Rate limit local

- Estado: `VERIFICADO`.
- Impacto: múltiples instancias no comparten bloqueos.
- Severidad: alta al escalar.

## KI-008 — Recepción sin corrección versionada

- Estado: `VERIFICADO`.
- Próximo paso: enmienda inmutable con motivo, actor y snapshots.
- Severidad: media.

## KI-009 — Reglas `COMPAT-1` codificadas

- Estado: `VERIFICADO`.
- Severidad: baja para volumen inicial.

## KI-010 — Privacidad y protección legal incompletas

- Estado: `VERIFICADO`.
- Severidad: alta antes de fotos/datos reales.

## KI-011 — Separación productiva no rastreada

- Estado: `RESUELTO BASE` en `0.4.1`.
- Entregado: contenedor, actor, fecha, unicidad y bloqueo de inicio.
- Restante: evidencia automatizada o control externo independiente.

## KI-012 — Sin optimizador ni costos productivos

- Estado: `PARCIAL`.
- Resuelto en `0.4.2`: conteos, pesos, duración y separación por período.
- Pendiente: consumos, capacidad histórica, costo, margen y optimización.
- Severidad: media.
