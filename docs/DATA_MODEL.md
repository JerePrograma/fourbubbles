# Modelo de datos

Versión: `0.4.0`.

## Principios

- PostgreSQL 16.
- Flyway V1-V10.
- Hibernate `ddl-auto=validate`.
- UUID internos, números humanos separados.
- gramos enteros y dinero `NUMERIC`.
- `TIMESTAMPTZ` para eventos.
- snapshots históricos para no reinterpretar operaciones.

## Núcleos existentes

- identidad/auditoría: `users`, `roles`, `refresh_tokens`, `audit_events`;
- clientes/ubicación: `clients`, `addresses`, `zones`;
- catálogo/precio: `service_offerings`, `garment_equivalences`, `price_definitions`, `promotions`;
- pedidos/pagos: `laundry_orders`, `order_items`, `order_state_history`, `payments`;
- recepción: `order_receptions`, `reception_items`, `reception_evidences`;
- compatibilidad: `order_treatment_profiles`, `compatibility_evaluations`, `compatibility_exceptions`.

## Producción V9

### `production_machines`

- `code` único;
- `machine_type`: `WASHER`/`DRYER`;
- `capacity_grams > 0`;
- `status`: `ACTIVE`, `MAINTENANCE`, `OUT_OF_SERVICE`;
- `active` y notas;
- auditoría/versión.

Índice parcial: una máquina solo puede tener un ciclo `PLANNED` o `RUNNING`.

### `production_programs`

- código/nombre;
- etapa `WASH`/`DRY`;
- tipo de máquina requerido;
- duración;
- temperatura para lavado;
- modo gentle;
- suavizante y fragancia;
- vigencia.

Constraints distinguen estructura de lavado y secado.

### `production_cycles`

- `cycle_number` único, formato `PC-000001`;
- `idempotency_key` única;
- máquina/programa;
- estado `PLANNED`, `RUNNING`, `COMPLETED`, `CANCELLED`;
- peso planificado/real;
- notas y fechas;
- auditoría/versión.

Los checks relacionan estado y fechas: un ciclo completado posee inicio/fin; uno cancelado solo fecha de cancelación.

### `production_cycle_orders`

- ciclo/pedido;
- posición 1 o 2;
- peso asignado;
- separación requerida.

Constraints:

- pedido único dentro del ciclo;
- posición única;
- máximo estructural de dos posiciones.

La prevención de asignación activa del mismo pedido/etapa se aplica transaccionalmente en servicio.

### `production_cycle_history`

Snapshot de cada transición del ciclo con estado anterior/nuevo, observación, actor y fecha.

## Protección histórica V10

El trigger `trg_protect_used_production_program_parameters` impide cambiar parámetros técnicos de un programa referenciado por cualquier ciclo:

- etapa/tipo de máquina;
- duración;
- temperatura;
- gentle;
- suavizante;
- fragancia.

Nombre, notas y activación pueden cambiar. Esto evita reinterpretar un ciclo histórico.

## Relaciones

```text
ProductionMachine 1---N ProductionCycle
ProductionProgram 1---N ProductionCycle
ProductionCycle 1---N ProductionCycleOrder
LaundryOrder 1---N ProductionCycleOrder
ProductionCycle 1---N ProductionCycleHistory
```

Un pedido puede participar en distintos ciclos históricos o etapas, pero no en dos ciclos activos de la misma etapa.

## Estado del pedido

Se agregó `WAITING_DRY` para separar lavado finalizado de secado iniciado.

Flujo productivo:

```text
CLASSIFIED/REWASH_REQUIRED
→ WAITING_WASH
→ WASHING
→ WAITING_DRY o QUALITY_CONTROL
→ DRYING
→ QUALITY_CONTROL
→ FOLDING o REWASH_REQUIRED
```

## Concurrencia

- idempotencia de ciclo: advisory lock por hash de clave;
- máquina/programa/pedidos: bloqueos pesimistas;
- pedidos: orden UUID estable;
- ciclo/quality: bloqueo antes de transición;
- unique parcial de máquina activa;
- unique global de clave idempotente.

## Seeds

V9 crea:

- lavadora principal 10 kg;
- secadora principal 10 kg;
- programas de lavado 30° sin fragancia, 40° estándar y delicado;
- programas de secado normal/delicado.

Son configuración inicial de desarrollo y pueden desactivarse o ampliarse.

## Migraciones

| Versión | Alcance |
|---|---|
| V1-V6 | plataforma y administración |
| V7 | recepción |
| V8 | compatibilidad |
| V9 | producción, máquinas, programas y ciclos |
| V10 | protección histórica de programas usados |

Las migraciones publicadas no se editan. Cambios futuros usan V11 o superior.
