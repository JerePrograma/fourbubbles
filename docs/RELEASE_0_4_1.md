# Release 0.4.1 — Separación física trazable

Fecha: 2026-07-26.

Commit funcional: `092a4ef78e5f062927f68d4a6548c1b5ab7156d2`.

## Objetivo final

Impedir que un ciclo compartido habilitado únicamente mediante una excepción de compatibilidad comience sin identificar y confirmar la separación física de cada pedido.

## Cambios por ruta y símbolo

| Ruta | Clase o símbolo | Cambio exacto | Motivo | Prueba asociada |
|---|---|---|---|---|
| `backend/src/main/java/ar/com/ropalista/production/domain/ProductionCycleOrder.java` | `ProductionCycleOrder` | persiste código, actor y fecha; confirma de forma idempotente | relacionar cada pedido con un contenedor trazable | `ProductionCycleSeparationTest`, `ProductionSeparationIT` |
| `backend/src/main/java/ar/com/ropalista/production/domain/ProductionCycle.java` | `start` | rechaza separaciones requeridas no confirmadas | impedir ejecución prematura | `ProductionCycleSeparationTest`, `ProductionSeparationIT` |
| `backend/src/main/java/ar/com/ropalista/production/api/ProductionSeparationDtos.java` | request/response | valida código seguro de 3–80 caracteres | contrato HTTP explícito | integración y TypeScript |
| `backend/src/main/java/ar/com/ropalista/production/application/ProductionSeparationService.java` | `list`, `confirm` | bloquea ciclo, valida etapa/asignación/unicidad y audita | consistencia transaccional | `ProductionSeparationIT` |
| `backend/src/main/java/ar/com/ropalista/production/api/ProductionSeparationController.java` | endpoints de separación | lectura para cuatro roles; escritura `ADMIN`/`OPERATOR` | operación y RBAC | `ProductionSeparationIT` |
| `backend/src/main/resources/db/migration/V11__production_separation_tracking.sql` | esquema/constraints | agrega campos, check e índice único por ciclo | última defensa de persistencia | Flyway/JPA validate |
| `frontend/src/pages/ProductionSeparationPage.tsx` | `ProductionSeparationPage` | lista ciclos pendientes y confirma contenedores | flujo operativo visible | TypeScript/build |
| `frontend/src/production/separationState.ts` | normalización/conteo | normaliza código y calcula pendientes | lógica cliente determinista | `separationState.test.ts` |
| `frontend/src/App.tsx`, `components/AppShell.tsx` | ruta/navegación | agrega `/production/separation` | acceso operativo | TypeScript/build |
| `scripts/Verify-Local.ps1` | mínimo Flyway | `10 → 11` | detectar imagen o base desactualizada | PowerShell/runtime |
| `.github/workflows/runtime-smoke.yml` | mínimo Flyway | `10 → 11` | validar el esquema final | runtime smoke |

## Contrato HTTP

### Consultar

```http
GET /api/production/cycles/{cycleId}/separations
```

Permisos: `ADMIN`, `OPERATOR`, `DRIVER`, `REPORT_VIEWER`.

### Confirmar

```http
PUT /api/production/cycles/{cycleId}/separations/{orderId}
Content-Type: application/json
```

```json
{"containerCode":"BAG-001"}
```

Permisos: `ADMIN`, `OPERATOR`.

## Invariantes

1. Solo se confirma una asignación con `separationRequired=true`.
2. La confirmación ocurre exclusivamente mientras el ciclo está `PLANNED`.
3. Cada código es único dentro del ciclo, sin distinguir mayúsculas/minúsculas.
4. Repetir el mismo código para la misma asignación es idempotente.
5. Intentar cambiarlo después de confirmar devuelve conflicto.
6. El ciclo no inicia hasta confirmar todas las separaciones requeridas.
7. Actor, fecha y auditoría quedan persistidos.
8. La confirmación no altera `compatible`, razones, recomendación ni excepción.

## Códigos de error principales

- `PRODUCTION_CYCLE_NOT_FOUND`;
- `PRODUCTION_CYCLE_ORDER_NOT_FOUND`;
- `PRODUCTION_CYCLE_ALREADY_STARTED`;
- `SEPARATION_NOT_REQUIRED`;
- `SEPARATION_CONTAINER_ALREADY_USED`;
- `SEPARATION_ALREADY_CONFIRMED`;
- `PRODUCTION_CYCLE_NOT_STARTABLE`.

## Validación requerida

```bash
cd backend && mvn clean verify
cd frontend && npm ci --no-audit --no-fund && npm run lint && npm test && npm run build
docker compose config --quiet
docker compose build
```

```powershell
.\scripts\tests\Local.Common.Tests.ps1
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

Estados remotos requeridos para declarar el corte `VERIFICADO`:

- `validation/ci-summary=success`;
- `validation/runtime-smoke=success`.

## Límites conscientes

- La aplicación prueba que un usuario autenticado confirmó un código, no que un sensor o tercero verificó la separación.
- No hay fotografía obligatoria, escaneo de etiqueta ni seguimiento durante el ciclo.
- Un procedimiento operativo incorrecto todavía puede incumplir la separación después de confirmarla.

## Pendiente posterior

- navegador E2E y accesibilidad;
- alineación de versiones de artefactos;
- administración completa de máquinas/programas;
- métricas productivas;
- logística 0.5;
- caja y costos 0.6;
- crecimiento 0.7;
- hardening productivo transversal.
