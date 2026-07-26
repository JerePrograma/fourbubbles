# Contexto técnico para agentes

## Identidad y estado

**Four Bubbles / Ropa Lista** gestiona una lavandería doméstica con retiro y entrega.

- repositorio: `JerePrograma/fourbubbles`;
- rama/fuente de verdad: `main` / `origin/main`;
- versión funcional: `0.4.1`;
- actores: `ADMIN`, `OPERATOR`, `DRIVER`, `REPORT_VIEWER`.

Implementado: plataforma, administración, recepción, compatibilidad, producción y separación trazable. Pendiente: E2E, logística, caja/costos, crecimiento y hardening productivo.

## Stack

Java 21/Spring Boot/Maven/JPA/Flyway, PostgreSQL 16, React 18/TypeScript/Vite/Vitest, Docker Compose/Nginx/PowerShell/GitHub Actions.

## Lectura recomendada

- `REPOSITORY_MAP.md`, `ARCHITECTURE.md`, `API.md`, `DATA_MODEL.md`;
- `FUNCTIONAL_SCOPE.md`, `SECURITY.md`, `TESTING.md`;
- `PROJECT_STATUS.md`, `ROADMAP.md`, `KNOWN_ISSUES.md`;
- `RELEASE_0_4_1.md`.

## Invariantes

1. Flyway define el esquema; `V1`–`V11` son inmutables y la siguiente migración es `V12+`.
2. Hibernate usa `ddl-auto=validate`.
3. Pedido, recepción, compatibilidad y producción permanecen separados.
4. `COMPAT-1` no cambia de significado.
5. Una excepción no altera el resultado original.
6. Cargas exceptuadas generan `separationRequired=true`.
7. Cada asignación requerida necesita contenedor único confirmado antes de iniciar.
8. La confirmación registra actor/fecha, pero no es una verificación física automatizada.
9. Ciclos usan idempotencia, advisory lock, bloqueos pesimistas y capacidad en gramos.
10. Programas usados conservan parámetros técnicos.
11. La UI no es autoridad de permisos o reglas.
12. Access token en memoria; refresh en cookie `HttpOnly`.

## Puntos de entrada

```text
ProductionController → ProductionService
ProductionSeparationController → ProductionSeparationService
ProductionCycle.start → exige separaciones confirmadas
frontend/src/pages/ProductionPage.tsx
frontend/src/pages/ProductionSeparationPage.tsx
scripts/Verify-Local.ps1 → Flyway >= 11
```

## Flujo productivo

```text
CLASSIFIED / REWASH_REQUIRED
→ ciclo PLANNED
→ confirmar separación si corresponde
→ WASHING
→ WAITING_DRY o QUALITY_CONTROL
→ DRYING
→ QUALITY_CONTROL
→ FOLDING o REWASH_REQUIRED
```

## Comandos canónicos

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

## Pendiente

- navegador E2E y accesibilidad;
- versiones de artefactos;
- object storage y correcciones de recepción;
- logística/rutas;
- caja, costos y rentabilidad;
- inventario, mantenimiento y reclamos;
- TLS, secretos, backup/restore, observabilidad y rollback.
