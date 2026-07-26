# Alcance funcional

Versión: `0.4.2`. Solo se describe comportamiento utilizable.

## Estado por módulo

| Módulo | Estado | Alcance disponible | Pendiente |
|---|---|---|---|
| autenticación/RBAC | base | sesión y jerarquía | MFA, gestión UI, rate limit distribuido |
| clientes/domicilios | avanzado | alta, preferencias, historial | timeline y geocodificación |
| catálogo/precios | base | servicios, equivalencias, precios, promociones | CRUD UI completo |
| pedidos | avanzado | declaración, cotización, planificación, estados | correcciones físicas versionadas |
| recepción | base | idempotencia, realidad física, decisión | binarios y enmiendas |
| compatibilidad | base | perfil, evaluación, explicación, excepción | matriz administrable |
| producción | base | máquinas, programas, ciclos, capacidad, calidad | optimización, insumos, métricas |
| separación | base | contenedor, confirmación, bloqueo de inicio | evidencia automatizada |
| métricas | base | estados, etapas, pesos, duración, separación | costos/capacidad histórica |
| logística | pendiente | retiro/promesa | rutas, paradas, kilómetros, agenda |
| finanzas | parcial | cobros | caja, costos, margen |

## Flujo disponible

```text
cliente → pedido → recepción → CLASSIFIED
→ perfil → compatibilidad opcional
→ ciclo planificado
→ confirmación de separación cuando corresponda
→ lavado → secado o calidad
→ FOLDING o REWASH_REQUIRED
```

## Separación

Cuando una carga compartida solo es efectivamente compatible por excepción:

- todas las asignaciones quedan con `separationRequired=true`;
- `ADMIN` u `OPERATOR` identifica un contenedor distinto por pedido;
- la confirmación registra actor y fecha;
- el mismo código sobre la misma asignación es idempotente;
- un código usado por otro pedido del ciclo es rechazado;
- el ciclo no inicia con confirmaciones pendientes;
- `DRIVER` y `REPORT_VIEWER` solo consultan.

La aplicación prueba que un operador confirmó la separación; no prueba mediante hardware que se mantuvo físicamente durante el ciclo.

## Métricas

Los cuatro roles consultan una ventana de hasta 366 días. Se informan conteos, pesos, duración, cargas compartidas y preparación de separación. Solo ciclos completados aportan duración/peso real. No se infieren costos ni utilización histórica.

## Roles

| Operación | ADMIN | OPERATOR | DRIVER | REPORT_VIEWER |
|---|---:|---:|---:|---:|
| configurar máquinas/programas | Sí | No | No | No |
| operar ciclos/calidad | Sí | Sí | No | No |
| confirmar separación | Sí | Sí | No | No |
| consultar producción/separación/métricas | Sí | Sí | Sí | Sí |
| registrar/decidir recepción | Sí | Sí | No | No |
| excepción de compatibilidad | Sí | No | No | No |

## Límites

- sin asignación automática ni fraccionamiento;
- sin consumos/costos por ciclo;
- mantenimiento básico por estado;
- sin secado natural modelado;
- sin rutas ni agenda real;
- sin evidencia física automatizada de separación;
- métricas sin costos, consumos ni snapshot de capacidad.
