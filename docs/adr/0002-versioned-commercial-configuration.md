# ADR 0002 — Configuración comercial versionada

## Estado

Aceptado.

## Decisión

Precios, promociones, equivalencias, políticas y capacidades se representan como registros con vigencia. Los pedidos guardan snapshots del cálculo aplicado.

No se actualiza destructivamente una tarifa ya utilizada. Una modificación crea una nueva versión y finaliza, cuando corresponde, la anterior.

## Razón

La trazabilidad comercial y contable exige poder reconstruir por qué un pedido histórico tuvo determinado importe.
