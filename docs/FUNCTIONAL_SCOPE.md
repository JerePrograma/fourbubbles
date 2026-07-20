# Alcance funcional

Versión: `0.2.0`.

Este documento describe funciones utilizables, no solamente tablas o estados.

| Módulo | Estado | Alcance disponible | Pendiente principal |
|---|---|---|---|
| Autenticación | Implementado base | login, refresh, logout, JWT, cookie segura, bloqueo local | usuarios UI, MFA, limitación distribuida |
| RBAC | Implementado | jerarquía y permisos por método | permisos finos por transición |
| Clientes | Avanzado | alta, búsqueda, actualización, estado y preferencias | timeline agregado |
| Domicilios | Implementado administrativo | múltiples, principal, vigencia, baja e historial | coordenadas y geocodificación |
| Zonas | Inicial | Marcos Paz y Mariano Acosta | radios y restricciones avanzadas |
| Catálogo | Inicial versionado | servicios y equivalencias | CRUD administrativo UI |
| Precios | Implementado | automático/manual, vigencia, histórico y desglose | simulación y reglas compuestas |
| Promociones | Implementado soportado | cupos, primera compra, domicilio y concurrencia | créditos y reglas compuestas |
| Pedidos | Avanzado | alta, cotización, planificación, estados y búsqueda | edición física versionada posterior |
| Recepción | Implementado base | idempotencia, peso/conteo real, inspección, diferencias y aprobación | almacenamiento binario y correcciones versionadas |
| Evidencias | Parcial | metadata y SHA-256 | upload/download y object storage |
| Pagos | Implementado base robusto | parciales, totales, saldo, historial y concurrencia | caja, reembolsos y webhooks |
| Auditoría | Implementado administrativo | persistencia, filtros e interfaz | exportación y retención |
| Compatibilidad | Pendiente | datos reales disponibles como insumo | matriz y motor explicable |
| Producción | Pendiente | estados preparatorios | ciclos, máquinas, lavado/secado/calidad |
| Logística | Pendiente | fechas en pedido | rutas, agenda, kilómetros y entrega |
| Finanzas | Parcial mínimo | cobros | caja, costos, margen y rentabilidad |
| Inventario | Pendiente | ninguno | stock, lotes y consumo |
| Reclamos | Pendiente | estado `CLAIM` | circuito, evidencia y compensación |
| Crecimiento | Pendiente | catálogo/promociones base | abonos, comercios, SLA y tableros |

## Tres snapshots diferentes

### Declaración

Se registra al crear el pedido:

- prendas declaradas;
- cantidades físicas;
- grupos;
- unidades equivalentes;
- peso declarado opcional;
- precio inicial.

### Recepción real

Se registra una única vez de forma idempotente:

- peso real;
- conteo real total;
- conteo real por equivalencia;
- diferencias;
- daños/manchas;
- observaciones;
- etiqueta/bolsa;
- evidencia metadata;
- aprobación.

No sobrescribe la declaración.

### Producción

Todavía pendiente:

- compatibilidad;
- composición de ciclos;
- máquina/programa;
- consumos;
- tiempos;
- calidad y relavado.

## Reglas de cliente/domicilio

- WhatsApp activo único.
- Al menos un domicilio activo.
- Un único principal activo.
- El principal no se desactiva directamente.
- Baja lógica e historial.
- Pedidos conservan el domicilio histórico.

## Reglas de precio/promoción

- precio automático original separado del cotizado vigente;
- cotización manual requiere `ADMIN` y motivo;
- confirmación congela el precio;
- promoción se consume al confirmar;
- la promoción se bloquea y revalida bajo transacción;
- una carrera no consume dos veces el mismo beneficio restringido.

## Reglas de recepción

### Estado de origen

Solo `PICKED_UP`.

### Idempotencia

- cabecera obligatoria `Idempotency-Key`;
- 16–120 caracteres `[A-Za-z0-9._:-]`;
- misma clave y pedido: mismo resultado;
- misma clave en otro pedido: conflicto;
- segunda clave en pedido ya recibido: conflicto;
- constraint único por pedido y por clave.

### Composición

- todos los códigos declarados deben aparecer;
- códigos duplicados se rechazan;
- cantidades reales son enteros no negativos;
- total real debe ser positivo;
- prenda adicional exige equivalencia vigente;
- diferencias se calculan por código y total.

### Peso

- peso real positivo;
- fecha no puede superar cinco minutos en el futuro;
- diferencia se conserva en gramos;
- aprobación requerida cuando el desvío supera 250 g o 10 %.

### Inspección

- daño obliga a aprobación;
- diferencia de piezas obliga a aprobación;
- mancha se registra, pero no obliga por sí sola;
- observaciones generales y por ítem;
- etiqueta única `RCV-xxxxxx`;
- bolsa opcional.

### Evidencia

Se registran solamente metadatos:

- object key;
- nombre;
- MIME;
- tamaño;
- SHA-256;
- descripción.

El binario debe existir en un almacenamiento externo. El sistema no afirma que haya subido un archivo solo porque guardó metadata.

### Estados resultantes

Sin aprobación:

```text
PICKED_UP → RECEIVED → PENDING_INSPECTION → CLASSIFIED
```

Con aprobación:

```text
PICKED_UP → RECEIVED → PENDING_INSPECTION → WAITING_PRICE_APPROVAL
```

Decisión:

```text
APPROVED → CLASSIFIED
REJECTED → CANCELLED
```

## Reglas de pago

- precio confirmado obligatorio;
- importe positivo;
- no superar saldo;
- pedido bloqueado durante el cobro;
- pagos concurrentes no sobrecobran;
- historial por fecha, medio, actor y referencia.

## Datos iniciales

- 2 zonas;
- 11 servicios;
- 21 equivalencias;
- 11 precios;
- 9 promociones;
- 4 medios de pago.

## Regla de honestidad

- `RECEIVED` ahora sí tiene agregado físico.
- `WASHING` todavía no implica un ciclo real.
- `DELIVERY_SCHEDULED` todavía no implica una ruta.
- `CLAIM` todavía no implica un módulo de reclamos.

Un módulo se considera finalizado cuando existen datos, reglas, transacción, API, permisos, UI cuando corresponde y pruebas de los riesgos críticos.
