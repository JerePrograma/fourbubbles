# Estrategia y estado de pruebas

Última actualización: 2026-07-20.

Versión: `0.1.2`.

## Gate de pull request

### Backend

```bash
cd backend
mvn clean verify
```

Ejecuta:

- compilación Java 21;
- 16 pruebas unitarias;
- 12 casos de integración;
- PostgreSQL 16 mediante Testcontainers;
- Flyway V1–V6;
- validación Hibernate/JPA;
- Maven Surefire y Failsafe.

### Frontend

```bash
cd frontend
npm ci
npm run lint
npm test
npm run build
```

Valida:

- lockfile reproducible;
- TypeScript estricto;
- lógica pura de borrador de pedido;
- build Vite.

### Contenedores

```bash
docker compose config --quiet
docker compose build
```

### Runtime

El workflow `runtime-smoke.yml`:

1. construye e inicia PostgreSQL, backend y frontend;
2. espera readiness;
3. valida la SPA;
4. inicia sesión con un administrador efímero;
5. obtiene un JWT;
6. consulta `/api/catalog/services` autenticado;
7. elimina volúmenes y contenedores.

## Cobertura backend

### Unitarias

- `PricingServiceTest`: base, precio fijo, primera compra y promociones manuales.
- `GarmentEquivalenceCalculatorTest`: agrupación y unidades.
- `OrderLimitPolicyTest`: límites por unidades/peso/capacidad.
- `OrderTransitionPolicyTest`: transiciones válidas e inválidas.
- `LoginAttemptServiceTest`: intentos, bloqueo y limpieza.

### Integración

#### `ApplicationContextIT`

- aplicación completa;
- PostgreSQL real;
- Flyway V1–V6;
- mapeos JPA válidos.

#### `ApiContractIT`

- 401 uniforme;
- 403 uniforme;
- validaciones de campos;
- enum/parámetro inválido;
- propagación de `X-Request-ID`.

#### `OperationalFlowIT`

- alta de cliente;
- actualización de preferencias;
- creación de pedido;
- búsqueda;
- confirmación;
- pago parcial;
- pago total.

#### `AdministrativeFlowIT`

- domicilio alternativo;
- cambio de principal;
- baja lógica;
- historial de domicilios;
- cotización manual;
- edición de planificación;
- bloqueo posterior a confirmación;
- historial de pagos;
- consulta de auditoría;
- carrera de promoción restringida.

#### `AdministrativeAuthorizationIT`

- `DRIVER` hereda lectura;
- `DRIVER` no crea clientes;
- `OPERATOR` no aplica cotización manual;
- contratos 403 evaluados con payloads válidos.

#### `ConcurrentPaymentIT`

- dos pagos simultáneos compiten por el mismo saldo;
- uno se confirma;
- el segundo recibe 422;
- solo un pago queda persistido;
- no existe sobrecobro.

## Defectos detectados por las pruebas

Durante 0.1.2 se encontraron y corrigieron:

1. promociones mock sin estado `ACTIVE`, que impedían probar la regla objetivo;
2. test de autorización con cuerpo inválido que medía 400 en lugar de 403;
3. auditoría de un domicilio antes de que JPA generara su UUID;
4. orden de flush inseguro al cambiar el principal;
5. planificación editable fuera de los estados de cotización;
6. posible sobrecobro por pagos concurrentes.

No se relajaron reglas productivas para hacer pasar tests; se corrigieron fixtures o implementación según correspondía.

## Verificación local

```powershell
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

`Verify-Local.ps1` comprueba contenedores, health, Flyway, SPA, login y una API protegida. Utiliza `.env` sin imprimir credenciales.

## Matriz pendiente

### Recepción

- idempotencia;
- conteo y peso real;
- diferencias;
- daños/manchas;
- aprobación;
- recalculo;
- evidencias.

### Producción

- compatibilidad;
- capacidad de ciclo;
- asignación concurrente;
- fallas de máquina;
- relavado;
- trazabilidad física.

### Logística

- solapamiento de franjas;
- rutas;
- orden de paradas;
- retiro/entrega idempotentes;
- kilómetros y costo.

### Finanzas externas

- idempotencia de webhook;
- reembolsos;
- caja;
- arqueo;
- conciliación.

## Criterio de release

No se integra un corte cuando:

- algún job obligatorio falla;
- quedan workflows diagnósticos temporales;
- las migraciones no validan sobre PostgreSQL real;
- la documentación afirma funciones inexistentes;
- el runtime no puede iniciar, autenticar y consultar una API protegida.
