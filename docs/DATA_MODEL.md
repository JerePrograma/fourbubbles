# Modelo de datos

Versión: `0.2.0`.

## Principios

- PostgreSQL 16.
- Flyway como única autoridad.
- Hibernate `ddl-auto=validate`.
- UUID de negocio.
- `NUMERIC`/`BigDecimal` para dinero.
- gramos enteros.
- `TIMESTAMPTZ`/`OffsetDateTime`.
- JSONB para snapshots controlados.
- baja lógica y vigencia para preservar historia.
- archivos pesados fuera de PostgreSQL.

## Relaciones principales

```text
clients 1---N addresses
clients 1---N laundry_orders
laundry_orders 1---N order_items
laundry_orders 1---N payments
laundry_orders 1---1 order_receptions
order_receptions 1---N reception_items
order_receptions 1---N reception_evidences
```

## Migraciones

- V1: identidad, refresh y auditoría.
- V2: zonas, clientes y domicilios.
- V3: catálogo, precios y promociones.
- V4: pedidos, estados y pagos.
- V5: datos comerciales iniciales.
- V6: vigencia de domicilios y cotización manual.
- V7: recepción física.

## V7 — Recepción

### Secuencia

`reception_label_seq` genera etiquetas legibles:

```text
RCV-000001
```

### `laundry_orders`

Agrega:

- `actual_physical_pieces`.

Ya existía `actual_weight_grams`; ambos se completan una sola vez al recibir.

### `order_receptions`

Campos principales:

- `order_id` único;
- `idempotency_key` único;
- `received_at`;
- piezas declaradas/reales/diferencia;
- peso declarado/real/diferencia;
- condición, daño y mancha;
- necesidad y estado de aprobación;
- actor/fecha/notas de decisión;
- etiqueta única;
- bolsa;
- auditoría estándar.

Constraints:

- una recepción por pedido;
- una clave globalmente única;
- etiqueta única;
- conteos y peso válidos;
- estados de aprobación restringidos;
- actor/fecha obligatorios para decisión final;
- `requires_customer_approval` coherente con `approval_status`.

### `reception_items`

Snapshot real por equivalencia:

- FK opcional a equivalencia;
- código/nombre copiados;
- piezas declaradas;
- piezas reales;
- diferencia;
- daño/mancha;
- observaciones.

El snapshot de código/nombre evita que una versión futura del catálogo cambie retrospectivamente la recepción.

Unique:

```text
(reception_id, equivalence_code_snapshot)
```

### `reception_evidences`

No almacena el archivo. Conserva:

- `object_key` único;
- nombre;
- MIME;
- tamaño;
- SHA-256;
- descripción;
- auditoría.

El `object_key` debe apuntar a un almacenamiento externo administrado por la operación.

## Invariantes transaccionales

### Idempotencia

- el servicio consulta la clave;
- bloquea el pedido;
- vuelve a comprobar clave y pedido;
- crea el agregado una sola vez;
- constraints de DB actúan como última barrera.

### Declarado versus real

No se modifica `order_items` ni `physical_pieces`.

Se conservan:

```text
order_items / physical_pieces   = declaración
reception_items / actual_*      = recepción real
```

### Estado

El agregado de recepción y las transiciones de pedido se guardan en la misma transacción.

Sin diferencias:

```text
PICKED_UP → RECEIVED → PENDING_INSPECTION → CLASSIFIED
```

Con diferencias:

```text
... → WAITING_PRICE_APPROVAL
```

La decisión final actualiza recepción, pedido, historial y auditoría juntos.

## Invariantes anteriores

### Domicilio

- principal activo único;
- `valid_to` coherente con `active`;
- pedidos conservan FK histórica.

### Precio

- `automatic_quoted_price` original;
- `quoted_price` vigente;
- `confirmed_price` congelado.

### Promoción

- uso al confirmar bajo bloqueo pesimista.

### Pago

- saldo calculado bajo bloqueo del pedido.

## Próximas migraciones

Compatibilidad deberá agregarse en V8 o posterior. No debe editar V7.

Posibles entidades:

- atributos de tratamiento normalizados;
- versiones de reglas;
- evaluación por pedido;
- explicación de conflictos;
- excepción autorizada.

## Prohibiciones

- no editar V1–V7 aplicadas;
- no `ddl-auto=update`;
- no reemplazar declaración con recepción;
- no almacenar binarios grandes en tablas transaccionales;
- no guardar secretos/tokens en claro;
- no usar `double` para dinero.
