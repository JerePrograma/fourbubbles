# Modelo de datos

Versión: `0.3.0`.

## Principios

- PostgreSQL 16.
- Flyway V1-V8 como única autoridad del esquema.
- Hibernate `ddl-auto=validate`.
- UUID para identificadores internos.
- `NUMERIC`/`BigDecimal` para dinero.
- gramos enteros para peso.
- `TIMESTAMPTZ` para eventos.
- eliminación lógica donde existe historial operativo.
- snapshots históricos para valores que no deben reinterpretarse.

## Núcleos principales

### Identidad y auditoría

- `users`
- `roles`
- `user_roles`
- `refresh_tokens`
- `audit_events`

### Clientes y ubicación

- `clients`
- `addresses`
- `zones`

`addresses` conserva `valid_from`, `valid_to`, estado activo y principal. El índice parcial garantiza un principal activo por cliente.

### Catálogo y precio

- `service_offerings`
- `garment_equivalences`
- `price_definitions`
- `promotions`
- `promotion_usages`

Las reglas conservan vigencia. Los pedidos referencian la definición de precio y promoción históricas.

### Pedidos y pagos

- `laundry_orders`
- `order_items`
- `order_state_history`
- `payments`
- `payment_methods`

`laundry_orders` separa:

- piezas físicas;
- unidades equivalentes;
- peso declarado;
- peso real;
- precio automático;
- precio cotizado;
- precio confirmado;
- estado operativo;
- estado de pago.

### Recepción V7

- `order_receptions`
- `reception_items`
- `reception_evidences`

Restricciones principales:

- una recepción por pedido;
- una clave idempotente global;
- etiqueta única;
- un ítem por equivalencia dentro de la recepción;
- decisión y actor separados del registro físico.

La composición real se guarda como snapshot. No sobrescribe `order_items`.

### Compatibilidad V8

#### `order_treatment_profiles`

Un perfil por pedido y por recepción.

Campos relevantes:

- `order_id` único;
- `reception_id` único;
- `color_group`;
- `material_group`;
- `max_temperature_c`;
- `dryer_allowed`;
- `fragrance_policy`;
- `softener_allowed`;
- `hypoallergenic`;
- `baby_clothes`;
- `pet_contact`;
- `heavy_soil`;
- `exclusive_cycle`;
- `notes`;
- versión optimista heredada de la entidad auditable.

El perfil pertenece al pedido clasificado y a su recepción. No es una preferencia genérica del cliente.

#### `compatibility_evaluations`

Snapshot inmutable del resultado entre dos perfiles.

Campos relevantes:

- `order_a_id`;
- `order_b_id`;
- `profile_a_version`;
- `profile_b_version`;
- `rule_version`;
- `compatible`;
- `reasons` JSONB;
- `recommendation` JSONB.

Restricción única:

```text
(order_a_id, order_b_id, profile_a_version, profile_b_version, rule_version)
```

El servicio normaliza el par por UUID, por lo que `A/B` y `B/A` son la misma evaluación lógica.

`reasons` conserva códigos, severidad y mensaje. `recommendation` conserva el tratamiento compartido propuesto. Ambos son snapshots para evitar reinterpretar una evaluación histórica con reglas futuras.

#### `compatibility_exceptions`

Una excepción como máximo por evaluación.

Campos:

- `evaluation_id` único;
- `reason`;
- `authorized_by`;
- `authorized_at`.

La excepción no modifica `compatible`. La compatibilidad efectiva se deriva de:

```text
compatible OR exception exists
```

## Relaciones relevantes

```text
Client 1---N Address
Client 1---N LaundryOrder
LaundryOrder 1---N OrderItem
LaundryOrder 1---1 OrderReception
OrderReception 1---N ReceptionItem
OrderReception 1---N ReceptionEvidence
LaundryOrder 1---1 OrderTreatmentProfile
OrderReception 1---1 OrderTreatmentProfile
LaundryOrder A/B --- N CompatibilityEvaluation
CompatibilityEvaluation 1---0..1 CompatibilityException
LaundryOrder 1---N Payment
LaundryOrder 1---N OrderStateHistory
```

## Concurrencia

- Promociones: bloqueo pesimista al confirmar.
- Pagos: bloqueo pesimista del pedido.
- Recepción: bloqueo pesimista del pedido más constraints únicos.
- Perfil: bloqueo pesimista del pedido antes de crear/actualizar.
- Evaluación: bloqueo de ambos pedidos en orden UUID antes de buscar/crear el snapshot.
- Excepción: bloqueo pesimista de la evaluación.

Los constraints son última defensa; el flujo normal evita llegar a una violación.

## Migraciones

| Versión | Alcance |
|---|---|
| V1-V5 | plataforma, catálogo, clientes, pedidos, seguridad y flujo inicial |
| V6 | cierre administrativo, domicilios, cotización, pagos y auditoría |
| V7 | recepción física idempotente |
| V8 | perfiles, evaluaciones y excepciones de compatibilidad |

Las migraciones publicadas no deben editarse. Cualquier cambio posterior debe agregarse como V9 o superior.
