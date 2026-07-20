# Arquitectura

Versión: `0.2.0`.

## Decisión

Monolito modular con límites por dominio. Se prioriza consistencia transaccional antes que distribución prematura.

## Módulos

```text
auth        identidad, sesiones, roles y login throttling
audit       eventos y consulta administrativa
catalog     servicios y equivalencias
customer    clientes, preferencias y domicilios
location    zonas
pricing     precios y promociones
order       pedido declarado, precio, planificación y estados
reception   snapshot real, inspección, diferencia, evidencia y decisión
payment     cobros, saldo e historial
common      contratos, errores, correlación y auditoría base
config      seguridad/OpenAPI/infraestructura
```

## Límite `order` / `reception`

### `order`

Es dueño de:

- declaración inicial;
- composición cotizada;
- precio;
- planificación;
- estado;
- resumen de totales reales (`actualPhysicalPieces`, `actualWeightGrams`).

### `reception`

Es dueño de:

- agregado único;
- idempotencia;
- composición real detallada;
- diferencias;
- inspección;
- aprobación;
- etiqueta/bolsa;
- evidencia metadata.

La recepción referencia el pedido y actualiza su resumen dentro de la misma transacción, pero no muta `order_items`.

## Flujo transaccional de recepción

```text
HTTP POST + Idempotency-Key
        ↓
ReceptionService
        ↓
consulta clave previa
        ↓
bloqueo PESSIMISTIC_WRITE de LaundryOrder
        ↓
revalidación clave/pedido/estado
        ↓
construcción OrderReception + items + evidences
        ↓
actualización resumen real del pedido
        ↓
historial de estados
        ↓
auditoría
        ↓
commit único
```

La doble consulta alrededor del bloqueo evita respuestas inconsistentes bajo carreras. Los constraints únicos son la última barrera.

## Política de diferencias

`ReceptionDifferencePolicy` es un componente puro y probado.

Entrada:

- piezas declaradas/reales;
- peso declarado/real;
- daño.

Salida:

- diferencias;
- peso material;
- necesidad de aprobación.

Umbral:

```text
abs(delta peso) > max(250 g, ceil(10 % declarado))
```

## Evidencias

`ReceptionEvidence` almacena metadata, no bytes. La arquitectura prevista es:

```text
browser/operador
   ↓ carga firmada futura
object storage privado
   ↓ objectKey/hash/metadata
API reception
   ↓
PostgreSQL
```

Hasta implementar esa carga, la UI solo permite registrar metadata de un objeto preexistente.

## Estados

La recepción usa la política de transiciones existente y genera historia por cada paso:

```text
PICKED_UP → RECEIVED → PENDING_INSPECTION
```

Luego:

- `CLASSIFIED`; o
- `WAITING_PRICE_APPROVAL`.

La decisión transaccional mueve a `CLASSIFIED` o `CANCELLED`.

## Persistencia

- UUID internos.
- secuencias para número de pedido y etiqueta.
- PostgreSQL/Flyway V1–V7.
- JSONB solo en snapshots flexibles.
- TIMESTAMPTZ para instantes.
- constraints e índices de idempotencia.
- `ddl-auto=validate`.

## Frontend

- SPA React/TypeScript.
- pantalla separada `/orders/:id/reception`.
- estado del formulario genera una clave estable con `crypto.randomUUID()`.
- backend decide umbrales, transiciones y permisos.
- frontend no recalcula reglas críticas.

## Runtime

```text
browser → Nginx :8080 → Spring :8081 → PostgreSQL :5432
```

CI y runtime smoke validan el conjunto.

## Próxima evolución

Compatibilidad debe consumir `reception_items` y atributos reales. No debe mezclar directamente pedidos sobre `order_items` declarados cuando existe recepción.

Futuros candidatos a adaptadores separados:

- object storage;
- notificaciones;
- logística;
- pagos externos.

No separar microservicios sin presión real: recepción, pedido y estados requieren atomicidad local.
