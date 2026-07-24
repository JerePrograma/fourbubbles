# Estrategia y estado de pruebas

Referencia funcional: `0.3.0`.

Base inspeccionada: `6f6d3cd8256408bc574e5b3d4568bf1b2866b0d8`.

## Gates canónicos

### Backend

```bash
cd backend
mvn clean verify
```

Ejecuta pruebas unitarias y `*IT` mediante Failsafe. Las integraciones usan Java 21, PostgreSQL 16 con Testcontainers, Flyway `V1`–`V8` y validación JPA.

Resultado histórico documentado para `0.3.0`:

- 25 pruebas unitarias;
- 19 pruebas de integración;
- 44 casos backend;
- 0 fallos.

Es un dato histórico, no un conteo recalculado por esta actualización.

### Frontend

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

- `lint` ejecuta TypeScript project build sin emisión;
- `test` ejecuta Vitest;
- `build` ejecuta TypeScript y Vite;
- Node 22 se usa en CI.

### PowerShell

```powershell
.\scripts\tests\Local.Common.Tests.ps1
```

Cubre lectura de `.env`, valores con `=`, creación/completado idempotente, preservación de secretos, puertos, normalización de JSON Compose e identidad abreviada de contenedores.

### Contenedores

```bash
docker compose config --quiet
docker compose build
```

CI usa puertos alternativos para detectar dependencias accidentales de `5432`, `8080` o `8081`.

### Runtime smoke

Workflow: `.github/workflows/runtime-smoke.yml`.

Secuencia:

1. inicia frontend sin backend;
2. valida Nginx y DNS diferido;
3. inicia PostgreSQL/backend;
4. espera readiness;
5. valida SPA;
6. valida rechazo anónimo;
7. hace login;
8. consulta catálogo protegido por Nginx;
9. exige ocho migraciones exitosas;
10. muestra diagnóstico y limpia.

### Verificación local

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

`Verify-Local.ps1` comprueba servicios, health, puertos efectivos, readiness, Flyway, SPA, proxy, 401/403, login y catálogo.

## Trazabilidad por funcionalidad

| Funcionalidad | Pruebas representativas | Gate |
|---|---|---|
| equivalencias, límites y transiciones | `GarmentEquivalenceCalculatorTest`, `OrderLimitPolicyTest`, `OrderTransitionPolicyTest` | `mvn clean verify` |
| contrato 401/403/error | `ApiContractIT`, `AdministrativeAuthorizationIT` | `mvn clean verify` |
| flujo administrativo | `OperationalFlowIT`, `AdministrativeFlowIT` | `mvn clean verify` |
| promoción concurrente | pruebas de `PricingService`/integración | `mvn clean verify` |
| pagos sin sobrecobro | `ConcurrentPaymentIT` | `mvn clean verify` |
| diferencias de recepción | `ReceptionDifferencePolicyTest` | `mvn clean verify` |
| recepción e idempotencia | `ReceptionFlowIT` | `mvn clean verify` |
| permiso de recepción | `ReceptionFlowIT.driverCanReadReceptionButCannotCreateIt` | `mvn clean verify` |
| compatibilidad pura | `CompatibilityEngineTest` | `mvn clean verify` |
| perfil/evaluación/excepción | `CompatibilityServiceTest`, `CompatibilityFlowIT` | `mvn clean verify` |
| concurrencia A/B | `ConcurrentCompatibilityIT` | `mvn clean verify` |
| borrador frontend | `frontend/src/order/orderDraft.test.ts` | `npm test` |
| helpers locales | `scripts/tests/Local.Common.Tests.ps1` | PowerShell |
| stack real | `runtime-smoke.yml` | GitHub Actions |

## Estados remotos

Los workflows publican:

- `validation/ci-summary`;
- `validation/runtime-smoke`.

Ambos estaban en `success` para la base `6f6d3cd`.

Jobs esperados:

- `CI / backend`;
- `CI / frontend`;
- `CI / powershell`;
- `CI / containers`;
- `Runtime smoke / compose-smoke`.

## Criterios de aceptación

Un cambio de código no está verificado solo por compilar. Según alcance:

- regla pura: test unitario positivo, negativo y límite;
- persistencia/transacción: integración PostgreSQL real;
- concurrencia/idempotencia: test simultáneo o de repetición;
- API/rol: MockMvc con autorizado y denegado;
- frontend: TypeScript, Vitest y build;
- infraestructura: Compose build y runtime smoke;
- migración: Flyway + JPA validate sobre base nueva.

## Limitaciones actuales

- sin E2E de navegador;
- sin accesibilidad automatizada;
- sin property-based testing;
- sin pruebas de carga;
- sin restore de backup;
- sin DAST;
- sin producción/ciclos;
- sin logística/costos.

## Alcance de esta actualización documental

`NO VERIFICADO`: no se ejecutaron Maven, npm, PowerShell ni Docker en el entorno que generó esta documentación porque no había checkout local ni resolución DNS. Se verificaron comandos, configuración, pruebas existentes y estados remotos de la base mediante GitHub.
