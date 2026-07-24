# Alcance funcional

Versión: `0.4.0`. Solo se describe comportamiento utilizable.

## Estado por módulo

| Módulo | Estado | Alcance disponible | Pendiente |
|---|---|---|---|
| autenticación/RBAC | base implementada | login, refresh, logout, jerarquía | MFA, gestión UI, rate limit distribuido |
| clientes/domicilios | avanzado | alta, preferencias, historial | timeline y geocodificación |
| catálogo/precios | base implementada | servicios, equivalencias, precios, promociones | CRUD UI completo |
| pedidos | avanzado | declaración, cotización, planificación, estados | correcciones físicas versionadas |
| recepción | base implementada | idempotencia, peso/conteo real, inspección, decisión | binarios y enmiendas |
| compatibilidad | base implementada | perfil, evaluación, razones, recomendación, excepción | matriz administrable, lotes múltiples |
| producción | base implementada | máquinas, programas, ciclos, capacidad, ejecución, calidad | optimización, insumos, mantenimiento completo |
| pagos | base robusta | parciales, totales, saldo, historial | caja, reembolsos, conciliación |
| auditoría | base implementada | eventos y consulta | exportación y retención |
| logística | pendiente | retiro/promesa en pedido | rutas, paradas, kilómetros, agenda |
| finanzas | parcial | cobros | costos, margen, caja |
| crecimiento | pendiente | catálogo/promociones | abonos, inventario, reclamos |

## Flujo disponible

```text
cliente → pedido → retiro → recepción → CLASSIFIED
→ perfil → compatibilidad opcional
→ lavado → secado o calidad
→ control de calidad → FOLDING o REWASH_REQUIRED
→ pago / entrega administrativa
```

## Producción

### Máquinas

- tipos `WASHER` y `DRYER`;
- capacidad en gramos;
- estados `ACTIVE`, `MAINTENANCE`, `OUT_OF_SERVICE`;
- alta/edición solo `ADMIN`;
- código y tipo inmutables;
- no se modifica con ciclo activo.

### Programas

- etapas `WASH` y `DRY`;
- duración, gentle, vigencia y notas;
- lavado: temperatura, suavizante y fragancia;
- secado: sin parámetros de lavado;
- parámetros técnicos de programas usados protegidos por `V10`.

### Planificación

- `ADMIN` u `OPERATOR`;
- `Idempotency-Key` de 8–120 caracteres;
- uno o dos pedidos distintos;
- peso real obligatorio;
- máquina/programa compatibles;
- capacidad suficiente;
- sin asignación activa de la misma etapa;
- perfil vigente y programa permitido.

Dos pedidos además exigen:

- no exclusividad;
- evaluación vigente `COMPAT-1`;
- `effectivelyCompatible=true`;
- `separationRequired=true` si solo una excepción habilita la combinación.

### Estados

```text
PLANNED → RUNNING → COMPLETED
PLANNED → CANCELLED
```

```text
CLASSIFIED / REWASH_REQUIRED
→ WAITING_WASH
→ WASHING
→ WAITING_DRY o QUALITY_CONTROL
→ DRYING
→ QUALITY_CONTROL
→ FOLDING o REWASH_REQUIRED
```

### Calidad

- `PASS` → `FOLDING`;
- `REWASH` → `REWASH_REQUIRED`;
- observación obligatoria;
- auditoría de decisión.

## Roles

| Operación | ADMIN | OPERATOR | DRIVER | REPORT_VIEWER |
|---|---:|---:|---:|---:|
| crear/editar máquina o programa | Sí | No | No | No |
| consultar configuración/ciclos | Sí | Sí | Sí | Sí |
| planificar/iniciar/completar/cancelar ciclo | Sí | Sí | No | No |
| control de calidad | Sí | Sí | No | No |
| registrar/decidir recepción | Sí | Sí | No | No |
| consultar recepción | Sí | Sí | Sí | Sí |
| guardar/evaluar compatibilidad | Sí | Sí | No | No |
| consultar compatibilidad | Sí | Sí | Sí | Sí |
| autorizar excepción | Sí | No | No | No |
| auditoría | Sí | No | No | No |

## Límites

- UI productiva base; no administración avanzada completa;
- sin asignación automática;
- separación solo marcada, no rastreada físicamente;
- sin fraccionamiento de pedidos;
- sin consumos ni costo de ciclo;
- mantenimiento es estado, no módulo;
- sin secado natural modelado;
- sin rutas ni agenda real.
