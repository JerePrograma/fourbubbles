# Estrategia y estado de pruebas

Versión funcional: `0.4.1`.

## Gates canónicos

```bash
cd backend && mvn clean verify
```

Ejecuta unitarias y `*IT` con PostgreSQL 16 Testcontainers, Flyway `V1`–`V11` y JPA validate.

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

```powershell
.\scripts\tests\Local.Common.Tests.ps1
```

```bash
docker compose config --quiet
docker compose build
```

El runtime smoke valida frontend antes del backend, readiness, SPA, proxy, login, catálogo y al menos once migraciones.

## Trazabilidad

| Función | Pruebas |
|---|---|
| programa/ciclo/calidad | `ProductionProgramPolicyTest`, `ProductionFlowIT` |
| permisos/concurrencia | `ProductionAuthorizationIT`, `ConcurrentProductionIT` |
| separación de dominio | `ProductionCycleSeparationTest` |
| separación integrada | `ProductionSeparationIT` |
| frontend separación | `separationState.test.ts` + TypeScript/build |
| recepción/compatibilidad | integraciones existentes |
| stack | runtime smoke |

## Casos de separación obligatorios

- ciclo exceptuado no inicia sin confirmaciones;
- contenedores distintos por pedido;
- replay del mismo código es idempotente;
- código duplicado en otro pedido es conflicto;
- cambio posterior de código es conflicto;
- confirmación tras iniciar es conflicto;
- lectura habilitada a cuatro roles;
- escritura denegada a `DRIVER`/`REPORT_VIEWER`;
- migración V11 y JPA validate.

## Estados remotos

Un cambio no está `VERIFICADO` hasta que `validation/ci-summary` y `validation/runtime-smoke` estén en `success` para el commit de `main`.

## Limitaciones

Sin navegador E2E, accesibilidad automatizada, property-based testing, carga, restore, DAST ni verificación física independiente de separación.
