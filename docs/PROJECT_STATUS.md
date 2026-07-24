# Estado integral del proyecto

Última actualización: 2026-07-24. Versión funcional: `0.4.0`.

## Veredicto ejecutivo

Four Bubbles dispone de administración, recepción física, compatibilidad explicable y producción base. La versión 0.4.0 convierte perfiles/evaluaciones en ciclos reales controlados por máquina, programa, capacidad, idempotencia y bloqueos.

No está listo para producción comercial: faltan despliegue seguro, evidencias binarias, separación física rastreable, logística, costos y pruebas no funcionales.

## Estado por fase

| Fase | Estado | Entregado | Pendiente |
|---|---|---|---|
| plataforma | `HECHO` para desarrollo | seguridad, PostgreSQL, React, Docker, CI | hardening productivo |
| administración | `HECHO` base | clientes, catálogo, pedidos, pagos, auditoría | timeline/CRUD completos |
| recepción 0.2 | `HECHO` base | idempotencia, realidad física, diferencias | binarios y correcciones |
| compatibilidad 0.3 | `HECHO` base | perfiles, `COMPAT-1`, historial, excepción | matriz administrable |
| producción 0.4 | `HECHO` base | máquinas, programas, ciclos, capacidad, calidad | optimización, insumos, separación física |
| logística | `PENDIENTE` | fechas en pedido | rutas y agenda |
| finanzas | parcial | cobros | caja, costos, margen |
| crecimiento | `PENDIENTE` | catálogo/promociones | abonos, inventario, reclamos |

## Producción 0.4.0 entregada

- [x] `production` como módulo funcional;
- [x] máquinas `WASHER`/`DRYER`;
- [x] capacidad y estado operativo;
- [x] programas `WASH`/`DRY`;
- [x] parámetros técnicos protegidos tras uso;
- [x] ciclos con número legible e historial;
- [x] uno o dos pedidos;
- [x] clave idempotente y advisory lock;
- [x] bloqueo de máquina, programa, pedidos y ciclo;
- [x] capacidad planificada/real;
- [x] evaluación vigente para compartir;
- [x] exclusividad respetada;
- [x] `separationRequired` para excepción;
- [x] lavado, secado, calidad y relavado;
- [x] auditoría;
- [x] Flyway `V9`/`V10`;
- [x] UI productiva;
- [x] pruebas de policy, flujo, autorización y concurrencia;
- [x] smoke/verificación actualizados a diez migraciones.

## Parcial

- la separación es un booleano operativo;
- UI de configuración productiva no cubre administración avanzada;
- mantenimiento es estado de máquina, no módulo;
- no hay fraccionamiento ni optimizador;
- evidencias no incluyen archivos;
- pagos no constituyen caja/contabilidad.

## Riesgos abiertos

1. Compose no es producción.
2. Sin backup/restore ni object storage.
3. Excepciones productivas sin tracking físico de separación.
4. Sin E2E de navegador, accesibilidad, carga ni DAST.
5. Rate limit local.
6. Sin corrección versionada de recepción.
7. Manifiestos todavía identifican `0.1.0`.
8. Sin costos/consumos por ciclo.

## Próximo orden

1. validar continuamente CI/runtime smoke de `main`;
2. cerrar trazabilidad de separación y E2E productivo;
3. logística 0.5;
4. caja/costos 0.6;
5. crecimiento 0.7;
6. hardening productivo transversal.
