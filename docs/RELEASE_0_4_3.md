# Release 0.4.3 — Límite de catálogo restaurado

Fecha: 2026-07-26.

## Objetivo

Eliminar la excepción arquitectónica en la que `CatalogController` consultaba repositorios directamente, sin cambiar el contrato HTTP ni las reglas de vigencia.

## Cambios por ruta y símbolo

| Ruta | Símbolo | Cambio | Motivo | Prueba |
|---|---|---|---|---|
| `catalog/application/CatalogQueryService.java` | `equivalences`, `services` | consulta, deduplicación y proyección | restaurar capas | `CatalogQueryServiceTest` |
| `catalog/api/CatalogController.java` | endpoints | delegación exclusiva | eliminar persistencia en HTTP | runtime smoke/contratos |
| `catalog/application/CatalogQueryServiceTest.java` | tests | campos y primera versión por código | evitar regresión | Maven |

## Compatibilidad

Se conservan rutas, autenticación global, orden y nombres/campos JSON. No hay migración ni cambios de dependencias.

## Validación requerida

`mvn clean verify`, frontend completo, PowerShell, Compose build y runtime smoke; ambos estados agregados deben estar en `success` para el SHA final.

## Pendiente

Versionado de artefactos, E2E, UI administrativa de catálogo, logística, costos y hardening productivo.
