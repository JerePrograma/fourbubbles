# Incidencias y deuda técnica conocidas

Fecha: 2026-07-24. Fuente: `origin/main`.

## KI-001 — `CatalogController` accede a repositorios

- Estado: `VERIFICADO`.
- Impacto: excepción al límite API → aplicación → persistencia.
- Próximo paso: introducir servicio de consulta con tests.
- Severidad: media.

## KI-002 — Versiones de manifiestos desalineadas

- Estado: `VERIFICADO`.
- Evidencia: versión funcional `0.4.0`; manifiestos conservan `0.1.0`.
- Próximo paso: definir política única y actualizar backend/frontend juntos.
- Severidad: media.

## KI-003 — Evidencias solo metadata

- Estado: `VERIFICADO`.
- Impacto: una referencia puede apuntar a un objeto inexistente.
- Próximo paso: object storage privado, autorización, integridad y retención.
- Severidad: alta antes de producción.

## KI-004 — Producción física ausente

- Estado: `RESUELTO` en `0.4.0`.
- Entregado: máquinas, programas, ciclos, capacidad, lavado, secado y calidad.
- Restante: optimización, insumos, costos y mantenimiento completo.

## KI-005 — Despliegue productivo no definido

- Estado: `VERIFICADO`.
- Faltan TLS, secretos administrados, backup/restore, recursos, observabilidad y rollback.
- Severidad: `BLOQUEANTE` para datos reales.

## KI-006 — Sin E2E de navegador

- Estado: `VERIFICADO`.
- Impacto: navegación, formularios y accesibilidad pueden degradarse.
- Próximo paso: Playwright para cliente → pedido → recepción → compatibilidad → producción.
- Severidad: media.

## KI-007 — Rate limit local

- Estado: `VERIFICADO`.
- Impacto: múltiples instancias no comparten bloqueos.
- Próximo paso: almacén compartido o control perimetral.
- Severidad: alta al escalar.

## KI-008 — Recepción sin corrección versionada

- Estado: `VERIFICADO`.
- Próximo paso: enmienda inmutable con motivo, actor y snapshots.
- Severidad: media.

## KI-009 — Reglas `COMPAT-1` codificadas

- Estado: `VERIFICADO`.
- Próximo paso: medir necesidad antes de motor administrable; no mutar `COMPAT-1`.
- Severidad: baja para volumen inicial.

## KI-010 — Privacidad y protección legal incompletas

- Estado: `VERIFICADO`.
- Impacto: auditoría técnica no equivale a consentimiento ni firma.
- Severidad: alta antes de fotos/datos reales.

## KI-011 — Separación productiva no rastreada físicamente

- Estado: `VERIFICADO`.
- Evidencia: `ProductionCycleOrder.separationRequired`.
- Impacto: se conserva la advertencia, pero no existe identificación de bolsa, compartimento, control o confirmación.
- Próximo paso: modelar unidades de contención y verificaciones antes de usar excepciones en operación real.
- Severidad: alta si se habilitan mezclas exceptuadas.

## KI-012 — Sin optimizador ni costos productivos

- Estado: `VERIFICADO`.
- Impacto: selección manual de cargas y ausencia de rentabilidad por ciclo.
- Próximo paso: primero instrumentar tiempos, consumos y costo; después optimizar.
- Severidad: media.
