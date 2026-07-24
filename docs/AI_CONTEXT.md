# Contexto técnico para agentes

## Identidad y estado

**Four Bubbles / Ropa Lista** gestiona una lavandería doméstica con retiro y entrega.

- repositorio: `JerePrograma/fourbubbles`;
- rama/fuente de verdad: `main` / `origin/main`;
- versión funcional: `0.4.0`;
- actores: `ADMIN`, `OPERATOR`, `DRIVER`, `REPORT_VIEWER`.

Implementado: plataforma, administración, recepción, compatibilidad explicable y producción base. Pendiente: logística, caja/costos, crecimiento y hardening productivo.

## Stack

- Java 21, Spring Boot, Maven, JPA/Hibernate, Flyway;
- PostgreSQL 16;
- React 18, TypeScript, Vite, React Router, React Hook Form, Zod y Vitest;
- Docker Compose, Nginx, PowerShell 7 y GitHub Actions.

## Lectura recomendada

- mapa: `REPOSITORY_MAP.md`;
- arquitectura: `ARCHITECTURE.md`;
- API: `API.md`;
- esquema: `DATA_MODEL.md`;
- alcance: `FUNCTIONAL_SCOPE.md`;
- seguridad: `SECURITY.md`;
- pruebas: `TESTING.md`;
- estado: `PROJECT_STATUS.md`;
- pendientes: `ROADMAP.md` y `KNOWN_ISSUES.md`;
- release: `RELEASE_0_4_0.md`.

## Invariantes

1. Flyway define el esquema; `V1`–`V10` son inmutables y la siguiente migración es `V11+`.
2. Hibernate permanece en `ddl-auto=validate`.
3. Pedido declarado, recepción real, compatibilidad y producción son snapshots/responsabilidades separadas.
4. `COMPAT-1` no cambia de significado.
5. La excepción conserva `compatible`, razones y recomendación; producción solo deriva `separationRequired`.
6. `separationRequired` no prueba separación física.
7. La creación de ciclos usa clave idempotente, advisory lock, bloqueo pesimista y orden UUID.
8. Capacidad planificada y real no puede superar la máquina.
9. Una carga compartida exige perfiles vigentes, evaluación vigente, compatibilidad efectiva y no exclusividad.
10. Los parámetros técnicos de programas usados están protegidos por `V10`.
11. La UI no es autoridad de permisos, estado, precio, capacidad ni compatibilidad.
12. Access token en memoria; refresh token en cookie `HttpOnly`.

## Puntos de entrada

```text
RopaListaApplication.main
AuthController → AuthService
OrderController → OrderService
ReceptionController → ReceptionService
CompatibilityController → CompatibilityService / CompatibilityEngine
ProductionController → ProductionService / ProductionProgramPolicy
PaymentController → PaymentService
frontend/src/App.tsx → rutas SPA
frontend/src/pages/ProductionPage.tsx → operación productiva
scripts/Start-Local.ps1 → inicio
scripts/Verify-Local.ps1 → verificación
```

## Flujo productivo

```text
CLASSIFIED / REWASH_REQUIRED
→ WAITING_WASH
→ WASHING
→ WAITING_DRY o QUALITY_CONTROL
→ DRYING
→ QUALITY_CONTROL
→ FOLDING o REWASH_REQUIRED
```

## Comandos canónicos

```bash
cd backend && mvn clean verify
```

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

```powershell
.\scripts\tests\Local.Common.Tests.ps1
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

```bash
docker compose config --quiet
docker compose build
```

## Hecho

- administración, pagos y auditoría;
- recepción idempotente;
- compatibilidad `COMPAT-1`;
- máquinas, programas, ciclos, capacidad y calidad;
- Flyway `V1`–`V10`;
- UI y pruebas de las verticales anteriores;
- hardening de entorno local y CI.

## Pendiente

- trazabilidad física de separación;
- optimización automática de cargas;
- almacenamiento binario de evidencias y correcciones de recepción;
- logística/rutas;
- caja, costos y rentabilidad;
- inventario, mantenimiento completo y reclamos;
- E2E de navegador, accesibilidad, carga y DAST;
- TLS, secretos administrados, backup/restore, observabilidad y rollback.

## Riesgos de modificación

- editar migraciones publicadas;
- relajar perfiles o exclusividad;
- cambiar orden de bloqueos;
- tratar excepción como compatibilidad original;
- alterar programas usados sin nueva estrategia histórica;
- codificar puertos host;
- usar estados administrativos para fingir ejecución física;
- declarar Compose como producción.
