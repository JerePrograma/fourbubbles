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

- **24 pruebas unitarias**;
- **18 pruebas de integración**;
- **42 casos backend totales**;
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
- `CompatibilityFlowIT`.

### Frontend

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

El gate comprueba:

- TypeScript estricto;
- pruebas Vitest;
- build Vite;
- importación de rutas/páginas nuevas;
- contrato de modelos de recepción y compatibilidad.

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
- persistencia del perfil efectivo.

### Integración

- flujo pedido → recepción → `CLASSIFIED` → perfil;
- evaluación compatible/incompatible;
- reuso del snapshot con mismas versiones;
- creación de evaluación nueva al cambiar perfil;
- autorización de excepción por `ADMIN`;
- rechazo de excepción para roles inferiores;
- precondiciones de estado y perfil.

## Concurrencia

Ya cubierto:

- consumo concurrente de promoción;
- pagos concurrentes sin sobrecobro;
- recepción concurrente con misma clave idempotente.

La implementación de compatibilidad añade bloqueo ordenado de ambos pedidos y constraint único del snapshot. Debe mantenerse una prueba concurrente explícita al ampliar el módulo o antes de crear ciclos.

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

Los workflows de diagnóstico son temporales. Se crean solo para aislar una falla y deben eliminarse antes del merge.

El gate válido es el de workflows permanentes:

- `CI`;
- `Runtime smoke`.

No se considera estable un PR por el solo hecho de que un diagnóstico pase.

## Casos aún faltantes

- evaluación concurrente duplicada explícita;
- property-based testing de combinaciones del motor;
- pruebas E2E de navegador;
- accesibilidad automatizada;
- pruebas de carga;
- restauración desde backup;
- seguridad dinámica;
- ciclos/máquinas y capacidad;
- logística y costos.
