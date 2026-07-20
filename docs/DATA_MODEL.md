# Modelo de datos

## Núcleo actual

```text
app_users 1---N refresh_tokens
app_users 1---N app_user_roles

zones 1---N addresses N---1 clients
clients 1---N laundry_orders
addresses 1---N laundry_orders

service_offerings 1---N price_definitions
zones 1---N price_definitions
service_offerings 1---N laundry_orders
price_definitions 1---N laundry_orders
promotions 1---N promotion_usages
promotions 1---N laundry_orders

garment_equivalences 1---N order_items N---1 laundry_orders
laundry_orders 1---N order_state_history
laundry_orders 1---N payments
payment_methods 1---N payments

audit_events referencia entidades por tipo/id para conservar independencia modular
```

## Invariantes implementadas

- usuario único sin distinguir mayúsculas;
- un domicilio principal activo por cliente;
- WhatsApp único entre clientes no eliminados;
- códigos comerciales versionables por fecha de vigencia;
- vigencias con fin posterior al inicio;
- dinero no negativo y pagos positivos;
- piezas, grupos y equivalencias positivas;
- pesos positivos cuando están presentes;
- número de pedido único;
- índices parciales para datos activos.

## Evolución prevista

Las tablas siguientes se agregarán solo junto con sus reglas:

- `compatibility_profiles`, `compatibility_rules`;
- `wash_cycles`, `dry_cycles`, `cycle_orders`, `cycle_products`;
- `equipment`, `maintenance_events`;
- `bags`, `bag_movements`;
- `routes`, `route_stops`;
- `subscriptions`, `subscription_usages`;
- `cash_sessions`, `cash_movements`;
- `cost_definitions`, `order_costs`, `fixed_expenses`, `work_logs`;
- `inventory_items`, `stock_movements`;
- `claims`, `claim_resolutions`;
- `policy_versions`, `policy_acceptances`;
- `communication_templates`, `communications`;
- `market_references`.
