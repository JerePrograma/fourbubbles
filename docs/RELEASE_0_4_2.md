# Release 0.4.2 — Métricas productivas

Fecha: 2026-07-26.

## Objetivo

Agregar visibilidad operativa sobre ciclos persistidos sin introducir estimaciones contables ni reinterpretar snapshots históricos.

## Cambios por ruta y símbolo

| Ruta | Símbolo | Cambio | Motivo | Prueba |
|---|---|---|---|---|
| `production/api/ProductionMetricsController.java` | `get` | endpoint de lectura por rango | exponer KPIs con RBAC | `ProductionMetricsIT` |
| `production/api/ProductionMetricsDtos.java` | `MetricsResponse` | contrato tipado | sincronizar API/UI | integración/TypeScript |
| `production/application/ProductionMetricsService.java` | `get` | agregación SQL, rango y porcentajes | cálculo único y transaccional | `ProductionMetricsIT` |
| `frontend/src/pages/ProductionMetricsPage.tsx` | página | tarjetas y desglose | operación visible | TypeScript/build |
| `frontend/src/production/metricsState.ts` | labels | lógica de presentación pura | test unitario | `metricsState.test.ts` |
| `App.tsx`, `AppShell.tsx` | ruta/nav | `/production/metrics` | acceso para cuatro roles | TypeScript/build |

## Invariantes

- intervalo `[from,to)` por fecha de creación del ciclo;
- ventana predeterminada 30 días;
- máximo 366 días;
- peso real y duración solo de ciclos completados;
- sin uso de capacidad actual para reinterpretar historia;
- acceso de solo lectura para cuatro roles;
- sin costos, margen ni consumo inferido.

## Validación requerida

`mvn clean verify`, TypeScript, Vitest, build, PowerShell, Compose build y runtime smoke, con ambos estados agregados en `success` para el SHA final.

## Pendiente

E2E de navegador, versiones de artefactos, UI administrativa completa, capacidad histórica, consumos/costos, logística y hardening productivo.
