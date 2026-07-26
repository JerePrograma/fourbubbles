# Estado integral del proyecto

Última actualización: 2026-07-26. Versión funcional: `0.4.3`.

## Veredicto ejecutivo

Four Bubbles dispone de administración, recepción física, compatibilidad explicable y producción controlada, separación trazable y métricas operativas. El corte 0.4.3 elimina el acceso directo del catálogo a persistencia desde la capa HTTP. El corte 0.4.1 cierra el principal hueco operativo de las excepciones: un ciclo exceptuado no inicia hasta identificar y confirmar un contenedor distinto para cada pedido.

No está listo para producción comercial: faltan despliegue seguro, evidencias binarias, logística, costos y pruebas no funcionales.

## Estado por fase

| Fase | Estado | Entregado | Pendiente |
|---|---|---|---|
| plataforma | `HECHO` para desarrollo | seguridad, PostgreSQL, React, Docker, CI | hardening productivo |
| administración | `HECHO` base | clientes, catálogo, pedidos, pagos, auditoría | timeline/CRUD completos |
| recepción 0.2 | `HECHO` base | idempotencia, realidad física, diferencias | binarios y correcciones |
| compatibilidad 0.3 | `HECHO` base | perfiles, `COMPAT-1`, historial, excepción | matriz administrable |
| producción 0.4 | `HECHO` base | máquinas, programas, ciclos, capacidad, calidad | optimización e insumos |
| separación 0.4.1 | `HECHO` base | contenedor, confirmación, bloqueo de inicio, UI y auditoría | evidencia automatizada |
| métricas 0.4.2 | `HECHO` base | estados, etapas, pesos, duración y separación | costos/capacidad histórica |
| logística | `PENDIENTE` | fechas en pedido | rutas y agenda |
| finanzas | parcial | cobros | caja, costos, margen |
| crecimiento | `PENDIENTE` | catálogo/promociones | abonos, inventario, reclamos |

## Separación 0.4.1 entregada

- [x] Flyway `V11`;
- [x] `separationContainerCode`, actor y fecha;
- [x] contenedor único por ciclo;
- [x] confirmación idempotente;
- [x] prohibición de iniciar con confirmaciones pendientes;
- [x] prohibición de confirmar después de iniciar;
- [x] API de lectura/escritura con RBAC;
- [x] auditoría de cada confirmación;
- [x] UI de pendientes;
- [x] pruebas de dominio, integración y permisos;
- [x] verificación operativa elevada a once migraciones.

## Métricas 0.4.2 entregadas

- [x] rango predeterminado de 30 días y máximo de 366;
- [x] estados y etapas de ciclo;
- [x] pedidos asignados y ciclos compartidos;
- [x] pesos planificado/real;
- [x] duración media y tasa de finalización;
- [x] separación requerida/pendiente;
- [x] acceso de lectura para cuatro roles;
- [x] UI y pruebas de integración/frontend.

## Arquitectura 0.4.3 entregada

- [x] `CatalogQueryService` como capa de aplicación;
- [x] `CatalogController` exclusivamente delegante;
- [x] contratos HTTP conservados;
- [x] proyecciones y deduplicación cubiertas por pruebas;
- [x] `KI-001` cerrado.

## Parcial

- la confirmación depende de la declaración del operador;
- UI de configuración productiva no cubre administración avanzada;
- mantenimiento es estado de máquina, no módulo;
- no hay fraccionamiento ni optimizador;
- evidencias no incluyen archivos;
- pagos no constituyen caja/contabilidad.

## Riesgos abiertos

1. Compose no es producción.
2. Sin backup/restore ni object storage.
3. La separación no tiene evidencia automática ni sensor.
4. Sin E2E de navegador, accesibilidad, carga ni DAST.
5. Rate limit local.
6. Sin corrección versionada de recepción.
7. Manifiestos todavía identifican `0.1.0`.
8. Métricas sin costos, consumos ni snapshot histórico de capacidad.

## Próximo orden

1. cerrar E2E de navegador y versiones de artefactos;
2. completar UI administrativa y capacidad histórica;
3. logística 0.5;
4. caja/costos 0.6;
5. crecimiento 0.7;
6. hardening productivo transversal.
