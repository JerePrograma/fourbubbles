# Contexto técnico para agentes

## 1. Identidad

**Four Bubbles / Ropa Lista** es un sistema de gestión para una lavandería doméstica con retiro y entrega, orientado inicialmente a Marcos Paz y Mariano Acosta.

Actores modelados: `ADMIN`, `OPERATOR`, `DRIVER` y `REPORT_VIEWER`.

Estado funcional de `main`: versión funcional documentada `0.3.0`. Están implementados administración, recepción física y compatibilidad explicable. No existen todavía ciclos físicos, máquinas, rutas, caja/costos completos ni almacenamiento binario de evidencias.

Stack confirmado:

- backend Java 21, Spring Boot, Maven, JPA/Hibernate, Flyway y PostgreSQL 16;
- frontend React 18, TypeScript, Vite, React Router, React Hook Form, Zod y Vitest;
- Docker Compose, Nginx, PowerShell 7 y GitHub Actions.

Repositorio canónico: `https://github.com/JerePrograma/fourbubbles`.

Rama y fuente de verdad: `main` / `origin/main`.

Base funcional inspeccionada: `6f6d3cd8256408bc574e5b3d4568bf1b2866b0d8`.

## 2. Mapa de lectura

- localizar código: [`REPOSITORY_MAP.md`](REPOSITORY_MAP.md);
- comprender límites y flujos: [`ARCHITECTURE.md`](ARCHITECTURE.md);
- contratos HTTP: [`API.md`](API.md);
- esquema y migraciones: [`DATA_MODEL.md`](DATA_MODEL.md);
- autenticación y permisos: [`SECURITY.md`](SECURITY.md);
- variables y perfiles: [`CONFIGURATION.md`](CONFIGURATION.md);
- iniciar/operar: [`OPERATIONS.md`](OPERATIONS.md) y [`WINDOWS_SETUP.md`](WINDOWS_SETUP.md);
- validar: [`TESTING.md`](TESTING.md);
- estado vigente: [`PROJECT_STATUS.md`](PROJECT_STATUS.md);
- incidencias verificadas: [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md);
- próximos cortes: [`ROADMAP.md`](ROADMAP.md);
- decisiones: [`DECISIONS.md`](DECISIONS.md);
- términos: [`GLOSSARY.md`](GLOSSARY.md).

## 3. Reglas inviolables

1. Trabajar contra `origin/main`; no asumir que ramas o PR abiertos están integrados.
2. Mantener módulos funcionales; no crear una estructura global por tipo técnico.
3. Controladores sin reglas de negocio ni acceso nuevo a repositorios.
4. Flyway es la autoridad del esquema y `ddl-auto=validate` debe seguir activo.
5. Nunca editar `V1`–`V8`; cualquier evolución comienza en `V9`.
6. Separar declaración del pedido, recepción real y evaluación histórica.
7. Conservar importes con `BigDecimal`/`NUMERIC`, peso en gramos enteros y eventos con zona temporal.
8. Revalidar promociones y saldo bajo bloqueo; no confiar en cálculos del frontend.
9. La recepción es única por pedido y usa `Idempotency-Key`.
10. `COMPAT-1` no puede cambiar de semántica sin una nueva versión.
11. Bloquear pares de pedidos según UUID canónico para evitar interbloqueos.
12. La excepción administrativa conserva intacto el resultado original.
13. El access token no se persiste en navegador; el refresh token sigue en cookie `HttpOnly`.
14. No exponer ni versionar secretos.
15. No declarar producción lista: Compose usa `dev` y publica únicamente en loopback.

## 4. Puntos de entrada

```text
backend/src/main/java/ar/com/ropalista/RopaListaApplication.java
→ RopaListaApplication.main
→ arranque Spring Boot y auditoría JPA

backend/src/main/java/ar/com/ropalista/auth/api/AuthController.java
→ login / refresh / logout
→ sesiones y cookie de renovación

backend/src/main/java/ar/com/ropalista/order/api/OrderController.java
→ creación, búsqueda, planificación, precio y estados
→ OrderService

backend/src/main/java/ar/com/ropalista/reception/api/ReceptionController.java
→ registro, consulta y decisión de recepción
→ ReceptionService.receive / get / decide

backend/src/main/java/ar/com/ropalista/compatibility/api/CompatibilityController.java
→ perfil, evaluación y excepción
→ CompatibilityService

backend/src/main/java/ar/com/ropalista/compatibility/application/CompatibilityEngine.java
→ CompatibilityEngine.evaluate
→ reglas puras COMPAT-1

backend/src/main/resources/db/migration/
→ V1–V8
→ esquema canónico

frontend/src/main.tsx
→ BrowserRouter + AuthProvider + App

frontend/src/App.tsx
→ rutas SPA protegidas

frontend/src/api/httpClient.ts
→ autenticación HTTP, refresh único en vuelo y contrato de error

scripts/Start-Local.ps1
→ configuración idempotente, preflight, Compose y health

scripts/Verify-Local.ps1
→ smoke local autenticado

.github/workflows/ci.yml
→ backend, frontend, PowerShell y contenedores

.github/workflows/runtime-smoke.yml
→ stack real, Nginx, login, catálogo y Flyway
```

## 5. Comandos canónicos

Desde la raíz:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

Backend:

```bash
cd backend
mvn clean verify
```

Frontend:

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

PowerShell:

```powershell
.\scripts\tests\Local.Common.Tests.ps1
```

Contenedores:

```bash
docker compose config --quiet
docker compose build
```

Detención conservando PostgreSQL:

```bash
docker compose down --remove-orphans
```

Eliminación explícita de datos locales:

```bash
docker compose down -v --remove-orphans
```

## 6. Estado actual

### HECHO y VERIFICADO en la base inspeccionada

- clientes, preferencias y domicilios versionados;
- catálogo, equivalencias, precios y promociones versionados;
- pedidos, cotización, planificación, estados y pagos;
- recepción idempotente con snapshot físico, diferencias y decisión;
- perfil efectivo y compatibilidad `COMPAT-1`;
- evaluaciones históricas y excepción administrativa;
- UI para las verticales anteriores;
- Flyway `V1`–`V8`, CI y runtime smoke;
- estados agregados `validation/ci-summary` y `validation/runtime-smoke` en `success` para `6f6d3cd`.

### PENDIENTE

- máquinas, programas, ciclos y capacidad;
- logística y rutas;
- caja, costos, margen y conciliación;
- object storage y carga/descarga de evidencias;
- navegador E2E, accesibilidad, carga y seguridad dinámica;
- endurecimiento productivo.

### NO VERIFICADO en esta actualización documental

- ejecución local de Docker, Maven, npm y PowerShell: el entorno de documentación no dispuso de checkout ni salida DNS;
- cualquier contenido del PR abierto `#7`: no pertenece a `origin/main`.

## 7. Riesgos de modificación

- Cambiar una migración publicada rompe instalaciones existentes.
- Relajar preferencias en compatibilidad puede mezclar cargas restringidas.
- Cambiar el orden UUID puede crear duplicados o interbloqueos.
- Recalcular snapshots históricos altera auditoría y explicabilidad.
- Cambiar una contraseña de `.env` no actualiza el administrador ya persistido.
- Codificar puertos host rompe el flujo configurable.
- Mover el access token a almacenamiento persistente amplía el impacto de XSS.
- Tratar una excepción como compatibilidad original oculta el riesgo operativo.
- Avanzar estados posteriores a `CLASSIFIED` como si fueran producción física crea trazabilidad falsa.
- `CatalogController` ya accede a repositorios; copiar esa excepción degradaría los límites de capa.

## 8. Última verificación

Fecha: `2026-07-24`.

Commit funcional inspeccionado: `6f6d3cd8256408bc574e5b3d4568bf1b2866b0d8`.

Evidencia consultada: árbol reconstruido mediante PR integrados `#2`–`#6`, archivos actuales de `main`, migraciones `V1`–`V8`, controladores, DTO, servicios críticos, frontend, scripts, Compose, workflows, historial y estados agregados de commit.

Validación remota de la base: `validation/ci-summary=success`; `validation/runtime-smoke=success`.
