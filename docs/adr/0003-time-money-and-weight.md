# ADR 0003 — Tiempo, dinero y peso

## Estado

Aceptado.

## Decisión

- `Instant` para eventos auditables.
- `LocalDate` para vigencias comerciales.
- zona de negocio `America/Argentina/Buenos_Aires`.
- `BigDecimal` / `NUMERIC(19,2)` para dinero.
- gramos enteros para peso.
- código ISO 4217 para moneda, inicialmente `ARS`.

## Consecuencia

Se evita el error de punto flotante y se mantiene una semántica consistente entre API, Java y PostgreSQL.
