# Estrategia y estado de pruebas

Última actualización: 2026-07-20.

Versión: `0.3.0`.

## Gate de pull request

### Backend

```bash
cd backend
mvn clean verify
```

El gate usa Java 21, PostgreSQL 16 mediante Testcontainers, Flyway V1-V8 y validación JPA.

Resultado verificado para 0.3.0:

- **25 pruebas unitarias**;
- **19 pruebas de integración**;
- **44 casos backend totales**;
- 0 fallos en el gate aceptado.

Clases unitarias principales:

- `PricingServiceTest`;
- `GarmentEquivalenceCalculatorTest`;
- `OrderTransitionPolicyTest`;
- `OrderLimitPolicyTest`;
- `LoginAttemptServiceTest`;
- `ReceptionDifferencePolicyTest`;
- `CompatibilityEngineTest`;
- `CompatibilityServiceTest`.

Clases de integración principales:

- `ApplicationContextIT`;
- `ApiContractIT`;
- `OperationalFlowIT`;
- `AdministrativeFlowIT`;
- `AdministrativeAuthorizationIT`;
- `ConcurrentPaymentIT`;
- `ReceptionFlowIT`;
- `CompatibilityFlowIT`;
- `ConcurrentCompatibilityIT`.

### Frontend

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

El gate comprueba TypeScript estricto, Vitest, build Vite, rutas nuevas y modelos de recepción/compatibilidad.

### Contenedores

```bash
docker compose config --quiet
docker compose build
```

Valida el modelo Compose y construye backend/frontend.

### Runtime smoke

El workflow permanente levanta el stack completo y verifica:

1. PostgreSQL saludable;
2. backend listo;
3. Flyway aplicado;
4. SPA accesible;
5. login con credenciales del entorno;
6. consulta autenticada de catálogo.

## Cobertura funcional de compatibilidad

### Motor

- carga compatible sin razones duras;
- exclusividad bloqueante;
- color desconocido/distinto;
- materiales compatibles e incompatibles;
- aislamiento hipoalergénico;
- cruce bebé/mascotas;
- suciedad pesada contra carga sensible;
- fragancia incompatible;
- reducción de temperatura;
- deshabilitación de secadora y suavizante.

### Servicio

- restricciones del cliente no relajables;
- exclusividad del pedido no relajable;
- fragancia `NONE` para perfil hipoalergénico;
- persistencia del perfil efectivo;
- orden UUID canónico alineado con el constraint de PostgreSQL;
- caso determinista `7fff…/8000…`, donde `UUID.compareTo` no sirve como orden canónico.

### Integración

- flujo pedido → recepción → `CLASSIFIED` → perfil;
- evaluación compatible/incompatible;
- reuso del snapshot con mismas versiones;
- creación de evaluación nueva al cambiar perfil;
- autorización de excepción por `ADMIN`;
- rechazo de excepción para roles inferiores;
- precondiciones de estado y perfil;
- dos solicitudes concurrentes A/B y B/A reutilizan el mismo ID de evaluación.

## Concurrencia cubierta

- consumo concurrente de promoción;
- pagos concurrentes sin sobrecobro;
- recepción concurrente con la misma clave idempotente;
- compatibilidad concurrente con orden de entrada inverso.

`ConcurrentCompatibilityIT` verifica que el bloqueo ordenado y la identidad canónica eviten snapshots duplicados.

## Contratos de seguridad

Se prueban:

- 401 sin autenticación;
- 403 por rol insuficiente;
- cotización manual `ADMIN`;
- auditoría `ADMIN`;
- recepción permitida a `DRIVER`;
- decisión de recepción no permitida a `DRIVER`;
- excepción de compatibilidad exclusiva de `ADMIN`.

## Diagnóstico de fallos

Los workflows de diagnóstico son temporales. Se crean solo para aislar una falla y se eliminan antes del merge.

El gate válido es:

- `CI`;
- `Runtime smoke`.

No se considera estable un PR porque un diagnóstico aislado pase.

## Casos aún faltantes

- property-based testing de combinaciones del motor;
- pruebas E2E de navegador;
- accesibilidad automatizada;
- pruebas de carga;
- restauración desde backup;
- seguridad dinámica;
- ciclos/máquinas y capacidad;
- logística y costos.
