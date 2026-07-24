# Mapa del repositorio

Referencia: `origin/main` en `6f6d3cd8256408bc574e5b3d4568bf1b2866b0d8`.

## Raíz

| Ruta | Responsabilidad | Consumidores |
|---|---|---|
| `README.md` | presentación, estado resumido y arranque | personas y agentes |
| `CHANGELOG.md` | evolución funcional | releases y diagnóstico histórico |
| `.env.example` | plantilla segura de entorno | scripts y Compose |
| `docker-compose.yml` | topología local | Docker Compose y workflows |
| `AGENTS.md` | reglas operativas para agentes | herramientas automatizadas |
| `.github/workflows/ci.yml` | gates estáticos y de build | pushes y PR |
| `.github/workflows/runtime-smoke.yml` | smoke del stack real | pushes y PR |

## Backend

Raíz de paquetes: `backend/src/main/java/ar/com/ropalista`.

| Ruta / módulo | Símbolos principales | Responsabilidad | Consumidores |
|---|---|---|---|
| `RopaListaApplication.java` | `RopaListaApplication.main` | arranque y JPA auditing | runtime |
| `auth/` | `AuthController`, `AuthService`, `JwtService`, `SecurityConfig`, `LoginAttemptService` | identidad, JWT, refresh, roles y throttling | frontend y todos los endpoints |
| `audit/` | `AuditService`, `AuditQueryService`, `AuditController` | persistir y consultar eventos | servicios transaccionales, ADMIN |
| `catalog/` | `CatalogController`, `ServiceOffering`, `GarmentEquivalence` | catálogo vigente | pedidos, recepción y frontend |
| `customer/` | `ClientController`, `ClientService`, `Client`, `Address` | clientes, preferencias y domicilios | pedidos y compatibilidad |
| `location/` | `Zone`, `ZoneRepository` | zonas operativas | domicilios y precio |
| `pricing/` | `PricingService`, `PriceDefinition`, `Promotion` | cotización y promociones | pedidos |
| `order/` | `OrderController`, `OrderService`, `LaundryOrder`, `OrderTransitionPolicy` | pedido declarado, planificación, precio y estados | recepción, pagos y compatibilidad |
| `payment/` | `PaymentController`, `PaymentService` | cobros e historial | pedidos y frontend |
| `reception/` | `ReceptionController`, `ReceptionService`, `ReceptionDifferencePolicy`, `OrderReception` | snapshot físico, diferencias y decisión | compatibilidad |
| `compatibility/` | `CompatibilityController`, `CompatibilityService`, `CompatibilityEngine` | perfil efectivo, evaluación y excepción | frontend y futura producción |
| `common/` | `ApiResponse`, `ApiErrorResponse`, `BusinessException`, `GlobalExceptionHandler`, `AuditableEntity` | contratos y comportamiento transversal | todos los módulos |
| `config/` | `AuditingConfig`, `OpenApiConfig` | infraestructura Spring | runtime |

### Persistencia y configuración

| Ruta | Responsabilidad |
|---|---|
| `backend/src/main/resources/application.yml` | configuración común, `/api`, JPA, Flyway, Actuator y OpenAPI |
| `application-dev.yml` | administrador inicial y logging de desarrollo |
| `application-test.yml` | perfil de integración |
| `application-prod.yml` | cookie segura y logs JSON |
| `db/migration/V1__identity_and_audit.sql` | identidad y auditoría |
| `db/migration/V2__locations_and_clients.sql` | zonas, clientes y domicilios |
| `db/migration/V3__catalog_and_pricing.sql` | catálogo y precio |
| `db/migration/V4__orders_and_payments.sql` | pedidos y pagos |
| `db/migration/V5__initial_configuration.sql` | datos iniciales |
| `db/migration/V6__administrative_closure.sql` | cierre administrativo |
| `db/migration/V7__order_reception.sql` | recepción física |
| `db/migration/V8__compatibility_engine.sql` | compatibilidad |

### Pruebas backend

| Ruta | Cobertura representativa |
|---|---|
| `backend/src/test/java/ar/com/ropalista/integration/PostgresIntegrationTestSupport.java` | PostgreSQL Testcontainers compartido |
| `integration/OperationalFlowIT.java` | flujo administrativo |
| `integration/AdministrativeFlowIT.java` | cierre administrativo |
| `integration/ReceptionFlowIT.java` | recepción, idempotencia, concurrencia y permisos |
| `integration/CompatibilityFlowIT.java` | perfil, evaluación y excepción |
| `integration/ConcurrentCompatibilityIT.java` | A/B y B/A |
| pruebas `*Test.java` por módulo | reglas puras y servicios |

## Frontend

Raíz: `frontend/src`.

| Ruta | Símbolo | Responsabilidad |
|---|---|---|
| `main.tsx` | montaje React | router, autenticación y estilos |
| `App.tsx` | `App` | definición de rutas |
| `auth/AuthContext.tsx` | `AuthProvider`, `useAuth` | sesión y access token en memoria |
| `api/httpClient.ts` | `apiRequest`, `refreshAccessToken` | cliente HTTP y renovación |
| `components/ProtectedRoute.tsx` | `ProtectedRoute` | proteger SPA |
| `components/AppShell.tsx` | `AppShell` | navegación |
| `pages/ClientsPage.tsx`, `NewClientPage.tsx`, `EditClientPage.tsx` | páginas de clientes | CRUD operativo |
| `pages/OrdersPage.tsx`, `NewOrderPage.tsx`, `OrderDetailPage.tsx` | páginas de pedidos | alta, búsqueda y operación |
| `pages/ReceptionPage.tsx` | `ReceptionPage` | registro/decisión |
| `pages/CompatibilityPage.tsx` | `CompatibilityPage` | perfil y evaluación |
| `pages/AuditPage.tsx` | `AuditPage` | auditoría ADMIN |
| `pages/AgendaPage.tsx`, `DashboardPage.tsx` | estructura parcial | agenda/tablero sin módulos completos |
| `models/` | tipos TypeScript | contrato cliente |
| `order/orderDraft.ts` y test | cálculo de borrador | previsualización no autoritativa |

## Infraestructura y scripts

| Ruta | Símbolo o función | Responsabilidad |
|---|---|---|
| `backend/Dockerfile` | build multi-stage | imagen Spring Boot |
| `frontend/Dockerfile` | build Vite + Nginx | imagen SPA |
| `infra/nginx/default.conf` | proxy `/api` | SPA y backend con DNS diferido |
| `scripts/Start-Local.ps1` | flujo principal | crear/completar `.env`, preflight, start y health |
| `scripts/Verify-Local.ps1` | smoke local | contenedores, Flyway, SPA, login y catálogo |
| `scripts/Local.Common.ps1` | helpers | entorno, puertos, Compose y diagnósticos |
| `scripts/Local.ContainerIdentity.ps1` | identidad de recursos | evitar detener proyectos ajenos |
| `scripts/tests/Local.Common.Tests.ps1` | pruebas PowerShell | parsers, variables y puertos |

## Documentación

`docs/` contiene fuentes estables, operativas, dinámicas e históricas. El índice canónico es [`README.md`](README.md); la entrada automatizada es [`AI_CONTEXT.md`](AI_CONTEXT.md).
