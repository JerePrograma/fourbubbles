# Modelo de dominio inicial

## Agregados implementados

### Identidad y acceso

`UserAccount` controla autenticación y rol. `RefreshToken` permite rotación y revocación.

### Cliente

`Customer` conserva datos mínimos, estado, fuente y observaciones. `Address` pertenece al cliente y se vincula a una `Zone`. `CustomerPreference` registra restricciones operativas relevantes.

### Catálogo y equivalencias

`GarmentEquivalence` es versionada. Distingue piezas físicas, tamaño del grupo y unidades comerciales. El pedido guarda el snapshot usado para impedir que un cambio posterior altere el historial.

### Comercial

`ServicePlan` define límites configurables. `PriceVersion` determina precio por vigencia y contexto. `Promotion` expresa restricciones iniciales y `PromotionUsage` impide reutilizaciones.

### Pedido

`LaundryOrder` es la raíz operativa. Contiene `OrderItem`, peso, equivalencias, precio confirmado y estado. `OrderStatusHistory` registra cada transición. La secuencia PostgreSQL genera números `RL-000001`.

### Pago

`Payment` registra movimientos parciales y su método. La conciliación de caja y reembolsos completos se implementará en Fase 4.

## Próximos agregados

- producción: compatibilidad, ciclos, máquinas, bolsas y control de calidad;
- logística: rutas, paradas y agenda;
- finanzas: costos, gastos, caja, margen y recuperación;
- crecimiento: abonos, inventario, reclamos, competencia y métricas avanzadas.

## Reglas ya explicitadas

- cálculo agrupado de unidades equivalentes;
- límites por unidades y gramos;
- selección de tarifa vigente;
- promociones no acumulables;
- primera compra y una promoción por domicilio;
- snapshots de precios y equivalencias;
- transiciones auditadas.
