# Release 0.4.0 — Producción base

Fecha: 2026-07-24.

## Objetivo

Convertir la compatibilidad explicable en asignación y ejecución productiva controlada.

## Cambios por ruta y símbolo

| Ruta | Símbolo | Cambio | Motivo | Prueba |
|---|---|---|---|---|
| `backend/.../order/domain/OrderStatus.java` | `WAITING_DRY` | nuevo estado | separar lavado de secado | `ProductionFlowIT` |
| `backend/.../order/application/OrderTransitionPolicy.java` | transiciones | integra flujo productivo | evitar saltos inválidos | tests de transición/flujo |
| `backend/.../production/api/ProductionController.java` | endpoints `/production` | máquinas, programas, ciclos, calidad | operación HTTP | `ProductionAuthorizationIT` |
| `backend/.../production/api/ProductionDtos.java` | requests/responses | contratos validados | sincronizar API/UI | integración |
| `backend/.../production/application/ProductionService.java` | orquestación | idempotencia, locks, capacidad, estados | consistencia | `ProductionFlowIT`, `ConcurrentProductionIT` |
| `backend/.../production/application/ProductionProgramPolicy.java` | `evaluate` | valida programa contra perfil | seguridad de tratamiento | `ProductionProgramPolicyTest` |
| `backend/.../production/domain/*` | agregados/enums | modelo productivo | persistencia y reglas locales | integración |
| `backend/.../production/persistence/*` | repositorios | consultas y locks | transacciones | integración |
| `V9__production_cycles.sql` | esquema/seeds | tablas, índices y secuencias | persistencia | Flyway/JPA validate |
| `V10__protect_used_production_programs.sql` | trigger | inmutabilidad técnica | historia correcta | integración |
| `frontend/src/pages/ProductionPage.tsx` | página | operación base | uso manual | TS/build |
| `frontend/src/models/production.ts` | tipos | contrato cliente | seguridad de tipos | TS/build |
| `scripts/Verify-Local.ps1` | mínimo Flyway | 8 → 10 | detectar imagen vieja | PowerShell/runtime |
| `runtime-smoke.yml` | mínimo Flyway | 8 → 10 | validar stack final | GitHub Actions |

## Invariantes

- una máquina no tiene dos ciclos activos;
- un pedido no tiene dos asignaciones activas de la misma etapa;
- peso planificado/real no supera capacidad;
- una carga compartida exige evaluación vigente;
- exclusividad nunca se exceptúa;
- excepción implica `separationRequired`;
- programas usados no cambian parámetros técnicos;
- misma clave y planificación devuelve mismo ciclo.

## Límites

- separación no rastreada físicamente;
- sin optimizador, fraccionamiento, insumos ni costos;
- mantenimiento básico por estado;
- UI de administración productiva incompleta;
- sin E2E de navegador.

## Validación

Gates requeridos:

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
