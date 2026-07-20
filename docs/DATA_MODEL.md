# Modelo de datos

Versión: `0.1.2`.

## Principios

- PostgreSQL 16 es la base principal.
- Flyway es la única autoridad del esquema.
- Hibernate usa `ddl-auto=validate`.
- UUID para entidades de negocio.
- `NUMERIC(15,2)`/`BigDecimal` para dinero.
- gramos enteros para peso.
- `TIMESTAMPTZ`/`OffsetDateTime` para instantes.
- JSONB únicamente para snapshots o estructuras flexibles controladas.
- baja lógica cuando debe conservarse historial.
- configuración comercial versionada por vigencia.

## Relaciones principales

```text
app_users 1---N refresh_tokens
app_users N---N roles

audit_events

clients 1---N addresses
zones   1---N addresses

clients   1---N laundry_orders
addresses 1---N laundry_orders
services  1---N laundry_orders
prices    1---N laundry_orders
promotions 1---N laundry_orders

laundry_orders 1---N order_items
laundry_orders 1---N order_state_history
laundry_orders 1---N payments
promotions     1---N promotion_usages
```

## Migraciones

### V1 — Identidad y auditoría

- `app_users`;
- `app_user_roles`;
- `refresh_tokens`;
- `audit_events`.

### V2 — Zonas, clientes y domicilios

- `zones`;
- `clients`;
- `addresses`;
- índice único de WhatsApp activo;
- índice único parcial de domicilio principal activo por cliente.

### V3 — Catálogo y precios

- servicios;
- equivalencias;
- definiciones de precio;
- promociones;
- usos promocionales.

### V4 — Pedidos y pagos

- `laundry_orders`;
- `order_items`;
- `order_state_history`;
- `payment_methods`;
- `payments`;
- secuencia de números legibles.

### V5 — Configuración inicial

- zonas iniciales;
- servicios;
- equivalencias;
- precios;
- promociones;
- medios de pago.

### V6 — Cierre administrativo

En `addresses`:

- `valid_from TIMESTAMPTZ NOT NULL`;
- `valid_to TIMESTAMPTZ`;
- constraint de orden temporal;
- constraint de coherencia entre `active` y `valid_to`;
- índice por cliente y vigencia.

En `laundry_orders`:

- `automatic_quoted_price`;
- `manual_quote_reason`;
- `manual_quote_at`;
- `manual_quote_by`;
- constraints de importe y completitud de cotización manual.

## Invariantes

### Cliente

- `whatsapp` es único mientras `deleted_at IS NULL`.
- un cliente eliminado lógicamente no libera ni modifica registros históricos.

### Domicilio

- existe como máximo un principal activo por cliente;
- un domicilio activo tiene `valid_to IS NULL`;
- uno inactivo tiene `valid_to IS NOT NULL`;
- `valid_to >= valid_from`;
- el cambio de principal hace flush de la despromoción antes de promover el nuevo;
- los pedidos conservan el `address_id` histórico.

### Pedido

El precio se divide en tres conceptos:

- `automatic_quoted_price`: resultado automático original;
- `quoted_price`: propuesta vigente, automática o manual;
- `confirmed_price`: valor congelado aceptado.

Una cotización manual exige que `manual_quote_reason`, `manual_quote_at` y `manual_quote_by` estén todos presentes o todos ausentes.

El `price_breakdown` conserva las líneas automáticas y agrega la diferencia manual.

### Promoción

- la referencia seleccionada queda en el pedido;
- el consumo efectivo se registra en `promotion_usages` al confirmar;
- una promoción se bloquea antes de revalidar cupos;
- las restricciones por domicilio y pedido se respaldan con consultas y constraints existentes.

### Pago

- cada pago referencia pedido, cliente y medio;
- el total pagado se calcula sobre pagos `PAID`;
- el pedido se bloquea antes de calcular saldo y guardar un pago;
- `payment_status` del pedido consolida `PENDING`, `PARTIAL` o `PAID`.

### Auditoría

`audit_events` conserva:

- tipo de entidad;
- identificador textual;
- acción;
- valor anterior JSONB;
- valor nuevo JSONB;
- motivo;
- actor y fecha heredados de auditoría JPA.

## Evolución pendiente

La recepción requerirá una migración nueva; no debe agregarse a V6 ni editar migraciones aplicadas. El diseño esperado debe contemplar:

- registro de recepción único por pedido o versión controlada;
- clave de idempotencia;
- peso/conteo reales;
- diferencias estructuradas;
- inspecciones, daños y manchas;
- evidencias externas con metadatos, no binarios grandes dentro de PostgreSQL;
- decisión/aprobación del cliente;
- etiquetas y bolsas.

## Prohibiciones

- no usar `ddl-auto=update`;
- no editar V1–V6 después de desplegarlas;
- no usar `double` para importes;
- no reemplazar domicilios o precios históricos;
- no guardar contraseñas, JWT ni refresh tokens en texto claro;
- no almacenar archivos pesados directamente en tablas transaccionales sin una decisión explícita.
