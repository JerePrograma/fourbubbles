# Arquitectura

Versión: `0.3.0`.

## Decisión

Monolito modular con límites por dominio. Se prioriza consistencia transaccional antes que distribución prematura.

## Módulos

```text
auth           identidad, sesiones, roles y login throttling
audit          eventos sensibles y consulta
catalog        servicios y equivalencias
customer       clientes y domicilios
location       zonas
pricing        precios, promociones y usos
order          pedido declarado, estados y planificación
payment        medios, cobros e historial
reception      snapshot físico real, diferencias y decisión
compatibility  perfiles, motor, evaluaciones y excepciones
common         API, errores y entidad auditable
config         seguridad e infraestructura transversal
```

## Dependencias principales

```text
customer ─┐
catalog  ─┼─> order ─> reception ─> compatibility
pricing  ─┘      └────────> payment
                    └──────> audit
```

- `order` conserva la declaración y resumen real.
- `reception` es dueño del agregado físico recibido.
- `compatibility` consume pedido y recepción, pero no modifica su composición.
- `compatibility` no depende de futuros ciclos/máquinas.

## Capas

Cada módulo contiene solo las capas que necesita:

- API: controladores y DTO;
- aplicación: orquestación y transacciones;
- dominio: entidades y reglas locales;
- persistencia: repositorios JPA;
- infraestructura: adaptadores cuando corresponda.

Los controladores no acceden directamente a repositorios.

## Transacciones críticas

### Confirmación de precio

- bloquea promoción;
- revalida vigencia/cupos;
- registra uso;
- confirma precio;
- audita.

### Pago

- bloquea pedido;
- recalcula saldo;
- impide sobrepago;
- registra cobro;
- actualiza estado de pago;
- audita.

### Recepción

- bloquea pedido;
- verifica idempotencia;
- crea snapshot real;
- calcula diferencias;
- avanza estados;
- audita.

### Perfil de compatibilidad

- bloquea pedido;
- exige `CLASSIFIED` y recepción;
- calcula restricciones efectivas;
- crea/actualiza perfil versionado;
- audita.

### Evaluación

- normaliza el par por UUID;
- bloquea ambos pedidos en ese orden;
- relee perfiles/versiones;
- reutiliza snapshot existente o crea uno nuevo;
- persiste razones/recomendación JSONB;
- audita.

### Excepción

- bloquea evaluación;
- valida incompatibilidad original;
- crea excepción única;
- conserva resultado original;
- audita.

## Compatibilidad explicable

`CompatibilityEngine` es puro y determinista. Entrada:

- dos `ProfileData`.

Salida:

- `compatible`;
- razones estructuradas;
- recomendación estructurada.

La versión `COMPAT-1` forma parte de la identidad histórica. Cambiar semántica requiere una nueva versión de reglas, no reinterpretar filas existentes.

## Perfil efectivo

La capa de aplicación combina:

- request operativo;
- preferencias del cliente;
- exclusividad del pedido.

La combinación es monotónica respecto de restricciones: nunca hace el tratamiento más permisivo que sus fuentes.

## Persistencia

- PostgreSQL 16.
- Flyway V1-V8.
- Hibernate solo valida.
- UUID internos.
- `NUMERIC(15,2)` para dinero.
- gramos enteros.
- JSONB para snapshots explicables.
- constraints únicos como última defensa.

## Frontend

SPA React con:

- contexto de autenticación;
- access token en memoria;
- refresh por cookie;
- rutas protegidas;
- clientes/pedidos/recepción/compatibilidad/pagos/auditoría;
- TypeScript estricto.

La UI no replica reglas críticas; muestra la respuesta efectiva del backend.

## Integraciones futuras

### Object storage

La base conservará metadata y referencias. Los binarios vivirán fuera de PostgreSQL.

### Producción

El siguiente módulo debe consumir `compatibility`, no incorporarse dentro de él. Ciclo, máquina, programa, capacidad y asignación serán agregados propios.

### Logística

Rutas y paradas serán independientes de pedidos para permitir replanificación sin reescribir la historia del pedido.

## Límites actuales

- despliegue Compose orientado a desarrollo;
- sin event bus;
- sin caché distribuida;
- sin object storage;
- sin motor de reglas administrable;
- sin ciclos, máquinas, rutas ni caja.

Estos límites son conscientes: distribuir antes de estabilizar el dominio agregaría complejidad sin resolver el riesgo operativo principal.
