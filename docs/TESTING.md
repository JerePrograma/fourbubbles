# Estrategia y estado de pruebas

Versión funcional: `0.4.0`.

## Gates canónicos

### Backend

```bash
cd backend
mvn clean verify
```

Ejecuta unitarias y `*IT` con Java 21, PostgreSQL 16 Testcontainers, Flyway `V1`–`V10` y JPA validate.

### Frontend

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

### PowerShell

```powershell
.\scripts\tests\Local.Common.Tests.ps1
```

### Contenedores

```bash
docker compose config --quiet
docker compose build
```

### Runtime

`.github/workflows/runtime-smoke.yml` valida frontend antes del backend, readiness, SPA, proxy, rechazo anónimo, login, catálogo y al menos diez migraciones.

## Trazabilidad

| Función | Pruebas |
|---|---|
| reglas de programa | `ProductionProgramPolicyTest` |
| flujo máquina/programa/ciclo/calidad | `ProductionFlowIT` |
| permisos productivos | `ProductionAuthorizationIT` |
| idempotencia y competencia por máquina/pedido | `ConcurrentProductionIT` |
| recepción | `ReceptionFlowIT` |
| compatibilidad | `CompatibilityFlowIT`, `ConcurrentCompatibilityIT` |
| administración/pagos | integraciones históricas |
| frontend | Vitest + TypeScript + build |
| entorno local | `Local.Common.Tests.ps1` |
| stack | runtime smoke |

## Casos productivos obligatorios

- capacidad exacta admitida;
- sobrepeso rechazado;
- máquina no disponible rechazada;
- programa incompatible rechazado;
- pedido exclusivo no compartido;
- evaluación vigente requerida;
- excepción efectiva marcada con separación;
- misma clave/mismo plan idempotente;
- misma clave/otro plan en conflicto;
- doble asignación concurrente impedida;
- transiciones de lavado/secado/calidad;
- roles autorizados y denegados;
- protección de programa usado.

## Estados remotos

Los workflows publican:

- `validation/ci-summary`;
- `validation/runtime-smoke`.

Un cambio no está `VERIFICADO` hasta que ambos estén en `success` para el commit de `main`.

## Limitaciones

- sin navegador E2E;
- sin accesibilidad automatizada;
- sin property-based testing;
- sin carga, restore ni DAST;
- sin prueba de separación física;
- sin despliegue productivo.
