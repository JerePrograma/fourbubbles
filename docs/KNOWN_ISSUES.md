# Incidencias y deuda técnica conocidas

Referencia: `origin/main` en `6f6d3cd8256408bc574e5b3d4568bf1b2866b0d8`. Fecha: `2026-07-24`.

## KI-001: `CatalogController` accede directamente a repositorios

- Estado: VERIFICADO.
- Evidencia: `backend/src/main/java/ar/com/ropalista/catalog/api/CatalogController.java`.
- Impacto: contradice el límite documentado controlador → aplicación → persistencia y facilita reglas/consultas en la capa HTTP.
- Próximo paso exacto: crear un servicio de consulta de catálogo y cubrirlo con tests antes de mover las dependencias.
- Severidad: media; no rompe funcionalidad actual.

## KI-002: versiones de manifiestos no representan la versión funcional

- Estado: VERIFICADO.
- Evidencia: documentación `0.3.0`; `backend/pom.xml` usa `0.1.0-SNAPSHOT`; `frontend/package.json` usa `0.1.0`.
- Impacto: artefactos e informes pueden identificar una versión incorrecta.
- Próximo paso exacto: definir una política única de versionado y actualizar backend/frontend juntos en un cambio separado.
- Severidad: media.

## KI-003: evidencias de recepción almacenan solo metadata

- Estado: VERIFICADO.
- Evidencia: `ReceptionDtos.EvidenceRequest`, `ReceptionEvidence` y `V7__order_reception.sql`.
- Impacto: la base puede referenciar un objeto inexistente; no hay carga, descarga, autorización ni retención.
- Próximo paso exacto: diseñar object storage privado, URLs temporales, integridad y borrado.
- Severidad: alta antes de producción.

## KI-004: no existe producción física en `main`

- Estado: VERIFICADO.
- Evidencia: ausencia de migraciones posteriores a `V8` y de módulo de ciclos/máquinas en `main`.
- Impacto: estados posteriores a `CLASSIFIED` no prueban lavado/secado real.
- Próximo paso exacto: implementar el corte `0.4.0` desde la base actual, con migración aditiva y pruebas.
- Nota: el PR abierto `#7` no es fuente de verdad y no debe documentarse como integrado.

## KI-005: despliegue productivo no definido

- Estado: VERIFICADO.
- Evidencia: Compose activa `dev`, publica en `127.0.0.1` y no hay manifiestos productivos.
- Impacto: faltan TLS, secretos administrados, backup/restore, recursos, rollback y observabilidad central.
- Próximo paso exacto: definir plataforma y SLO antes de almacenar datos reales.
- Severidad: bloqueante para producción.

## KI-006: cobertura E2E limitada a HTTP/stack

- Estado: VERIFICADO.
- Evidencia: workflows prueban SPA, login y API; no existe Playwright/Cypress en manifiestos.
- Impacto: regresiones de navegación, formularios y accesibilidad pueden pasar.
- Próximo paso exacto: agregar un smoke de navegador sobre el flujo cliente → pedido → recepción → compatibilidad.
- Severidad: media.

## KI-007: rate limiting local

- Estado: VERIFICADO.
- Evidencia: `LoginAttemptService` y documentación de seguridad.
- Impacto: múltiples instancias no comparten intentos ni bloqueos.
- Próximo paso exacto: usar un almacén compartido o control perimetral antes de escalar.
- Severidad: alta en despliegue distribuido.

## KI-008: recepción no tiene corrección versionada

- Estado: VERIFICADO.
- Evidencia: una recepción única por pedido y servicio sin endpoint de corrección.
- Impacto: un error confirmado no puede rectificarse con historial explícito.
- Próximo paso exacto: diseñar una enmienda inmutable con motivo, actor y snapshot previo/posterior.
- Severidad: media.

## KI-009: compatibilidad está codificada

- Estado: VERIFICADO.
- Evidencia: `CompatibilityEngine` y `RULE_VERSION="COMPAT-1"`.
- Impacto: cualquier ajuste requiere release; no hay matriz administrable ni simulador.
- Próximo paso exacto: medir necesidad real antes de introducir un motor configurable; no mutar `COMPAT-1`.
- Severidad: baja para el volumen inicial.

## KI-010: protección legal y privacidad incompletas

- Estado: VERIFICADO como ausencia documental/operativa.
- Evidencia: aprobación de recepción no es firma digital y las futuras imágenes pueden contener datos personales.
- Impacto: trazabilidad técnica no equivale a consentimiento, firma ni política de retención.
- Próximo paso exacto: definir política de privacidad, autorización y retención antes de fotos reales.
- Severidad: alta antes de producción.
