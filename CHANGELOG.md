# Changelog

## 0.4.1 — Separación física trazable

Fecha: 2026-07-26.

### Agregado

- migración Flyway `V11`;
- código de contenedor por pedido dentro de un ciclo compartido exceptuado;
- confirmación con actor y fecha;
- unicidad de contenedor dentro del ciclo;
- API de consulta y confirmación de separación;
- pantalla **Separación** para ciclos planificados;
- auditoría `CONFIRM_SEPARATION`;
- pruebas unitarias, integración, permisos, replay y duplicados;
- runtime smoke y verificación local actualizados a once migraciones.

### Endurecido

- un ciclo con `separationRequired=true` no puede iniciar hasta confirmar todos los contenedores;
- la misma confirmación/código es idempotente;
- cambiar el contenedor después de confirmar devuelve conflicto;
- confirmar después de iniciar el ciclo devuelve conflicto;
- `DRIVER` y `REPORT_VIEWER` conservan solo lectura.

### Límite consciente

La confirmación acredita una acción del operador y queda auditada. No existe sensor, fotografía obligatoria ni verificación independiente de que las prendas permanezcan separadas durante todo el ciclo.

## 0.4.0 — Producción base

Fecha: 2026-07-24.

- máquinas `WASHER`/`DRYER`, programas y ciclos;
- asignación idempotente de uno o dos pedidos;
- capacidad, bloqueos y compatibilidad vigente;
- lavado, secado, calidad y relavado;
- Flyway `V9`/`V10`;
- UI, pruebas y documentación.

## 0.3.0 — Compatibilidad explicable

- Flyway `V8`, perfiles, `COMPAT-1`, evaluaciones históricas y excepción `ADMIN`.

## 0.2.0 — Recepción física

- Flyway `V7`, recepción idempotente, realidad física, diferencias y decisión.

## 0.1.0–0.1.2 — Plataforma y administración

- seguridad, PostgreSQL, catálogo, clientes, pedidos, pagos, auditoría, React, Docker y CI.
