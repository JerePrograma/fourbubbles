# Alcance funcional

Versión: `0.4.0`.

Este documento describe funciones realmente utilizables.

## Estado por módulo

| Módulo | Estado | Alcance disponible | Pendiente principal |
|---|---|---|---|
| Autenticación | Implementado base | login, refresh, logout y bloqueo local | usuarios UI, MFA y rate limit distribuido |
| RBAC | Implementado | jerarquía y permisos por método | permisos finos por instalación |
| Clientes/domicilios | Avanzado | alta, preferencias, múltiples domicilios e historial | timeline y geocodificación |
| Catálogo/precios | Implementado base | servicios, equivalencias, precios y promociones versionados | CRUD UI completo |
| Pedidos | Avanzado | declaración, cotización, planificación, estados y búsqueda | correcciones físicas versionadas |
| Recepción | Implementado base | idempotencia, peso/conteo real, inspección y decisión | binarios y correcciones |
| Compatibilidad | Implementado base | perfil, evaluación, razones, recomendación e excepción | matriz administrable y lotes múltiples |
| Producción | Implementado base | máquinas, programas, ciclos, capacidad, ejecución y calidad | insumos, mantenimiento y optimización |
| Pagos | Implementado robusto base | parciales, totales, saldo e historial | caja, reembolsos y webhooks |
| Auditoría | Implementado base | eventos sensibles y consulta | exportación y retención |
| Logística | Pendiente | retiro/promesa en pedido | rutas, paradas, kilómetros y agenda |
| Finanzas | Parcial mínimo | cobros | caja, costos, margen y conciliación |
| Crecimiento | Pendiente | catálogo/promociones | abonos, inventario y reclamos |

## Flujo disponible

```text
cliente
→ pedido declarado y precio
→ retiro
→ recepción real
→ CLASSIFIED
→ perfil de tratamiento
→ compatibilidad cuando se comparte
→ ciclo de lavado
→ ciclo de secado o calidad directa
→ control de calidad
→ FOLDING o REWASH_REQUIRED
→ pago / entrega administrativa
```

## Producción

### Máquinas

Tipos:

- `WASHER`;
- `DRYER`.

Estados:

- `ACTIVE`;
- `MAINTENANCE`;
- `OUT_OF_SERVICE`.

Una máquina debe estar activa, disponible y sin otro ciclo `PLANNED/RUNNING`.

### Programas

Etapas:

- `WASH`: temperatura y fragancia obligatorias; suavizante opcional;
- `DRY`: sin parámetros de lavado.

El programa se valida contra cada perfil:

- temperatura no superior al máximo;
- suavizante solo si todos lo permiten;
- fragancia coincidente;
- delicado/lana exige programa gentle;
- secado exige permiso de secadora.

Tras el primer ciclo, sus parámetros técnicos son inmutables. Nombre, notas y activación pueden cambiar.

### Planificación de ciclo

Precondiciones:

- `ADMIN` u `OPERATOR`;
- `Idempotency-Key` de 8 a 120 caracteres;
- máquina/programa compatibles;
- uno o dos pedidos distintos;
- perfil vigente en cada pedido;
- peso real disponible;
- capacidad suficiente;
- ninguna asignación activa de la misma etapa.

Para dos pedidos:

- ambos perfiles no exclusivos;
- evaluación `COMPAT-1` con versiones vigentes;
- `effectivelyCompatible=true`;
- si `compatible=false` y existe excepción, `separationRequired=true`.

La excepción no permite exceder capacidad ni compartir exclusividad.

### Estados de ciclo

```text
PLANNED → RUNNING → COMPLETED
PLANNED → CANCELLED
```

No se cancela un ciclo iniciado. La cancelación deja los pedidos en estado de espera para replanificación.

### Estados de pedido

```text
CLASSIFIED / REWASH_REQUIRED
→ WAITING_WASH
→ WASHING
→ WAITING_DRY o QUALITY_CONTROL
→ DRYING
→ QUALITY_CONTROL
→ FOLDING o REWASH_REQUIRED
```

Si el perfil no permite secadora, lavado completo avanza directamente a calidad.

### Control de calidad

- `PASS` → `FOLDING`;
- `REWASH` → `REWASH_REQUIRED`.

Requiere observación y queda auditado.

## Idempotencia y concurrencia

- La misma clave y plan esencial devuelve el mismo ciclo.
- Reusar la clave con máquina, programa o pedidos diferentes devuelve conflicto.
- Un advisory lock serializa la misma clave.
- Un bloqueo de máquina evita dos ciclos activos concurrentes.
- Los pedidos se bloquean en orden UUID.
- Constraints parciales/únicos son última defensa.

Las notas no forman parte de la identidad idempotente actual; la identidad se basa en máquina, programa y conjunto de pedidos.

## Roles

| Operación | ADMIN | OPERATOR | DRIVER | REPORT_VIEWER |
|---|---:|---:|---:|---:|
| crear/editar máquina/programa | Sí | No | No | No |
| consultar configuración/ciclos | Sí | Sí | Sí | Sí |
| planificar/iniciar/completar/cancelar | Sí | Sí | No | No |
| control de calidad | Sí | Sí | No | No |
| recepción | Sí | Sí | Sí | No |
| compatibilidad | Sí | Sí | lectura | lectura |
| auditoría | Sí | No | No | No |

## Límites conscientes

- La UI ofrece alta básica; edición avanzada está disponible por API.
- No hay asignación automática óptima de pedidos.
- La separación requerida es una marca operativa, no tracking físico interno.
- No hay consumo de detergente/suavizante ni costo del ciclo.
- Mantenimiento es un estado, no un módulo completo.
- No hay secado natural modelado como ciclo.
- No hay rutas ni agenda logística real.
