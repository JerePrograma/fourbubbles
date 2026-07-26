# Mapa del repositorio

Versión funcional: `0.4.2`.

## Raíz

| Ruta | Responsabilidad |
|---|---|
| `README.md` | presentación e inicio |
| `AGENTS.md` | reglas de agentes |
| `CHANGELOG.md` | evolución |
| `.github/workflows/ci.yml` | backend, frontend, PowerShell y contenedores |
| `.github/workflows/runtime-smoke.yml` | stack real y Flyway >= 11 |

## Backend

Raíz: `backend/src/main/java/ar/com/ropalista`.

| Módulo | Símbolos principales | Responsabilidad |
|---|---|---|
| `auth` | `AuthController`, `AuthService` | identidad/sesiones |
| `customer/order/pricing/payment` | servicios de administración | operación comercial |
| `reception` | `ReceptionService` | snapshot físico |
| `compatibility` | `CompatibilityService`, `CompatibilityEngine` | perfiles/evaluaciones |
| `production` | `ProductionController`, `ProductionService` | máquinas, programas, ciclos, calidad |
| `production` | `ProductionSeparationController`, `ProductionSeparationService` | contenedores y confirmación |
| `production` | `ProductionMetricsController`, `ProductionMetricsService` | KPIs por período |

### Producción y separación

```text
production/api/ProductionController.java
production/api/ProductionSeparationController.java
production/api/ProductionSeparationDtos.java
production/application/ProductionService.java
production/application/ProductionSeparationService.java
production/domain/ProductionCycle.java
production/domain/ProductionCycleOrder.java
production/api/ProductionMetricsController.java
production/application/ProductionMetricsService.java
```

### Migraciones

- `V9`: producción;
- `V10`: protección de programas;
- `V11`: separación trazable.

### Pruebas nuevas

- `ProductionCycleSeparationTest`;
- `ProductionSeparationIT`;
- `ProductionMetricsIT`.

## Frontend

| Ruta | Responsabilidad |
|---|---|
| `pages/ProductionPage.tsx` | ciclos y calidad |
| `pages/ProductionSeparationPage.tsx` | confirmación de contenedores |
| `pages/ProductionMetricsPage.tsx` | KPIs productivos |
| `models/productionSeparation.ts` | contrato TypeScript |
| `production/separationState.ts` | normalización y pendientes |
| `production/metricsState.ts` | labels de métricas |
| `production-separation.css` | estilos responsive |
| `App.tsx`, `AppShell.tsx` | ruta y navegación |

## Operación

- `scripts/Verify-Local.ps1`: smoke autenticado y Flyway >= 11;
- `runtime-smoke.yml`: stack real y Flyway >= 11.
