# Modelo de datos

Versión: `0.4.2`.

## Principios

- PostgreSQL 16;
- Flyway `V1`–`V11`;
- Hibernate `ddl-auto=validate`;
- UUID internos, gramos enteros y `TIMESTAMPTZ`;
- snapshots históricos para no reinterpretar operaciones.

## Núcleos

- identidad/auditoría;
- clientes, domicilios y zonas;
- catálogo, precios y promociones;
- pedidos, pagos e historial;
- recepción y evidencias metadata;
- perfiles/evaluaciones/excepciones de compatibilidad;
- máquinas, programas, ciclos, asignaciones e historial productivo.

## Producción V9/V10

`production_machines`, `production_programs`, `production_cycles`, `production_cycle_orders` y `production_cycle_history` modelan capacidad, programas, ejecución y pedidos. `V10` protege parámetros técnicos de programas usados.

## Separación V11

`production_cycle_orders` agrega:

- `separation_container_code VARCHAR(80)`;
- `separation_confirmed_at TIMESTAMPTZ`;
- `separation_confirmed_by VARCHAR(100)`.

El constraint `ck_production_separation_confirmation` exige que los tres campos sean nulos o estén completos sobre una asignación con `separation_required=true`.

El índice `uk_production_cycle_separation_container` garantiza unicidad case-insensitive del contenedor dentro del ciclo cuando el código existe.

Las asignaciones históricas previas a V11 permanecen válidas con campos nulos. Un ciclo planificado exceptuado deberá confirmarse antes de iniciar por regla de dominio.

## Relaciones

```text
ProductionMachine 1---N ProductionCycle
ProductionProgram 1---N ProductionCycle
ProductionCycle 1---N ProductionCycleOrder
LaundryOrder 1---N ProductionCycleOrder
ProductionCycle 1---N ProductionCycleHistory
```

## Concurrencia

- advisory lock por clave idempotente;
- bloqueos pesimistas de máquina, programa, pedidos y ciclo;
- UUID canónico para pares;
- unique parcial de máquina activa;
- unique de clave idempotente;
- unique de contenedor por ciclo.

## Migraciones

| Versión | Alcance |
|---|---|
| V1–V6 | plataforma y administración |
| V7 | recepción |
| V8 | compatibilidad |
| V9 | producción |
| V10 | protección de programas usados |
| V11 | separación física trazable |

Las migraciones publicadas no se editan. Cambios futuros usan `V12+`. El corte 0.4.2 no altera esquema: agrega consultas agregadas sobre ciclos y asignaciones existentes.
