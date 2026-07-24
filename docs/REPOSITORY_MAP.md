# Mapa del repositorio

Versión funcional: `0.4.0`.

## Raíz

| Ruta | Responsabilidad |
|---|---|
| `README.md` | presentación y arranque |
| `AGENTS.md` | reglas de agentes |
| `CHANGELOG.md` | evolución |
| `.env.example` | plantilla segura |
| `docker-compose.yml` | topología local |
| `.github/workflows/ci.yml` | backend, frontend, PowerShell y contenedores |
| `.github/workflows/runtime-smoke.yml` | stack real y mínimo de diez migraciones |

## Backend

Raíz: `backend/src/main/java/ar/com/ropalista`.

| Módulo | Símbolos principales | Responsabilidad |
|---|---|---|
| `auth` | `AuthController`, `AuthService`, `SecurityConfig` | identidad y sesiones |
| `audit` | `AuditService`, `AuditController` | trazabilidad |
| `catalog` | `CatalogController` | catálogo vigente |
| `customer` | `ClientService`, `ClientController` | clientes/domicilios |
| `pricing` | `PricingService` | precios/promociones |
| `order` | `OrderService`, `LaundryOrder`, `OrderTransitionPolicy` | pedido y estados |
| `payment` | `PaymentService` | cobros |
| `reception` | `ReceptionService`, `OrderReception` | realidad física |
| `compatibility` | `CompatibilityService`, `CompatibilityEngine` | perfiles/evaluaciones |
| `production` | `ProductionController`, `ProductionService`, `ProductionProgramPolicy` | máquinas, programas, ciclos y calidad |
| `common/config` | contratos y configuración | transversal |

### Producción

```text
production/api/ProductionController.java
production/api/ProductionDtos.java
production/application/ProductionService.java
production/application/ProductionProgramPolicy.java
production/domain/*
production/persistence/*
```

### Migraciones

- `V1`–`V6`: plataforma/administración;
- `V7`: recepción;
- `V8`: compatibilidad;
- `V9`: máquinas, programas y ciclos;
- `V10`: protección de programas usados.

### Pruebas representativas

- `ProductionProgramPolicyTest`;
- `ProductionFlowIT`;
- `ProductionAuthorizationIT`;
- `ConcurrentProductionIT`;
- pruebas históricas de administración, recepción y compatibilidad.

## Frontend

| Ruta | Responsabilidad |
|---|---|
| `App.tsx` | rutas protegidas |
| `components/AppShell.tsx` | navegación |
| `pages/ProductionPage.tsx` | operación productiva |
| `models/production.ts` | contratos TypeScript |
| `production.css` | estilos de producción |
| `api/httpClient.ts` | cliente HTTP y refresh |
| páginas existentes | clientes, pedidos, recepción, compatibilidad, auditoría |

## Operación

| Ruta | Responsabilidad |
|---|---|
| `scripts/Start-Local.ps1` | inicio y health |
| `scripts/Verify-Local.ps1` | smoke autenticado y Flyway ≥10 |
| `scripts/Local.Common.ps1` | `.env`, puertos, Compose |
| `infra/nginx/default.conf` | SPA y proxy diferido |
