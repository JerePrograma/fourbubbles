# Arquitectura

Versión de referencia: `0.1.2`.

## Decisión principal

Monolito modular. Cada módulo contiene las capas que necesita: API, aplicación, dominio, persistencia e infraestructura. No se distribuye prematuramente una operación todavía pequeña, pero se mantienen límites explícitos para poder evolucionar por módulo.

## Módulos implementados

```text
auth        identidad, roles, sesiones y login throttling
audit       eventos sensibles y consulta administrativa
catalog     servicios y equivalencias
customer    clientes, preferencias y domicilios versionados
location    zonas
pricing     precios, promociones y revalidación concurrente
order       pedidos, ítems, límites, cotización y estados
payment     medios, cobros, saldo e historial
common      respuestas, excepciones, correlación y base auditable
config      seguridad, OpenAPI, auditoría JPA e infraestructura transversal
```

## Dependencias

- controladores dependen de DTO y servicios de aplicación;
- servicios de aplicación orquestan entidades y repositorios;
- dominio no depende de HTTP;
- persistencia pertenece al módulo dueño de los datos;
- los cruces entre módulos se realizan desde aplicación, no desde controladores;
- no se exponen entidades JPA como contratos API;
- no se permite lógica comercial duplicada en React.

## Transacciones críticas

### Cliente/domicilio

- alta de domicilio, auditoría y respuesta se ejecutan en una transacción;
- el domicilio se persiste antes de auditar su UUID;
- cambiar principal hace flush de la despromoción antes de promover otro registro;
- la baja es lógica y preserva pedidos históricos.

### Pedido/precio

- creación de pedido, ítems, historial y auditoría es atómica;
- cotización manual conserva el precio automático;
- confirmación congela `confirmed_price`;
- planificación solo es editable en estados tempranos.

### Promoción

La confirmación adquiere un bloqueo pesimista sobre la promoción, revalida reglas y registra el uso dentro de la misma transacción. El cálculo preliminar no reserva cupo.

### Pago

El cobro adquiere un bloqueo pesimista sobre el pedido antes de consultar pagos previos, calcular saldo, guardar el nuevo pago y actualizar el estado financiero.

## Seguridad

- Spring Security stateless;
- jerarquía de roles centralizada;
- autorización específica por método;
- JWT de acceso y refresh opaco;
- access token solo en memoria del frontend;
- errores de filtro y controlador usan el mismo contrato JSON;
- `X-Request-ID` atraviesa la solicitud y los logs.

## Persistencia

- UUID internos;
- secuencia para número humano de pedido;
- `NUMERIC(15,2)` para dinero;
- gramos enteros;
- `TIMESTAMPTZ` para instantes;
- `DATE` para vigencias comerciales por día;
- JSONB para snapshots de preferencias, precio y auditoría;
- baja lógica y vigencias donde importa trazabilidad;
- constraints e índices definidos por Flyway;
- `ddl-auto=validate`.

## Frontend

- SPA React 18;
- TypeScript estricto;
- cliente HTTP centralizado;
- sesión renovable;
- rutas protegidas;
- formularios con React Hook Form/Zod;
- cálculos de vista previa aislados en funciones puras probadas;
- backend como autoridad de precio, permisos, transiciones y saldo.

## Runtime

```text
browser
  ↓
Nginx / frontend :8080
  ├── archivos SPA
  └── proxy /api
        ↓
Spring Boot :8081
        ↓
PostgreSQL :5432
```

Compose espera salud de PostgreSQL y backend. El smoke runtime valida el circuito completo.

## Evolución prevista

### Evidencias

Fotografías y archivos deben ir a almacenamiento de objetos o filesystem administrado. PostgreSQL guardará:

- URL/clave;
- hash;
- MIME;
- tamaño;
- actor;
- fecha;
- relación con recepción/reclamo.

### Mensajería

WhatsApp debe comenzar con plantillas, enlaces e historial. Una API externa requiere adaptador, idempotencia, reintentos y observabilidad; no debe contaminar el dominio.

### Escalado

Antes de separar servicios deben existir presión real y límites claros. Candidatos futuros:

- evidencias;
- notificaciones;
- optimización logística;
- procesamiento de pagos externos.

La base transaccional de pedidos, recepción y producción debe permanecer coherente; dividirla sin necesidad aumentaría el costo de consistencia.
