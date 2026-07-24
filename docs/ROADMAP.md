# Hoja de ruta

Versión base: `0.4.0`.

## Completado

### 0.1 — Plataforma y administración

Seguridad, catálogo, clientes, pedidos, pagos, React, Docker, CI, operación y auditoría.

### 0.2 — Recepción física

Flyway `V7`, idempotencia, peso/conteo real, diferencias, decisión y metadata de evidencia.

### 0.3 — Compatibilidad explicable

Flyway `V8`, perfiles, `COMPAT-1`, evaluaciones históricas, recomendación y excepción.

### 0.4 — Producción base

Flyway `V9`/`V10`, máquinas, programas, ciclos, capacidad, lavado, secado, calidad, UI y concurrencia.

## Endurecimiento inmediato de 0.4

1. E2E de navegador para el flujo productivo.
2. Trazabilidad física de separación para ciclos exceptuados.
3. Administración UI completa de máquinas/programas.
4. Métricas de duración, capacidad y fallos.
5. Cierre de versiones de artefactos `0.4.0`.

## 0.5 — Logística y agenda

- franjas, rutas y paradas;
- conductor y orden de visita;
- retiro/entrega real;
- kilómetros, combustible y tiempo;
- incidencias y mensajes;
- capacidad diaria.

## 0.6 — Caja, costos y rentabilidad

- caja y arqueo;
- ingresos/egresos/reembolsos;
- conciliación;
- insumos, energía, agua, transporte y mano de obra;
- costo por ciclo/pedido;
- amortización, margen y rentabilidad.

## 0.7 — Crecimiento

- abonos y SLA;
- inventario/lotes;
- compras/consumo;
- mantenimiento completo;
- reclamos, compensaciones y políticas;
- tableros y alertas.

## Transversal antes de producción real

- TLS/dominio;
- secretos administrados;
- backup/restore probado;
- object storage;
- observabilidad;
- rate limit distribuido;
- privacidad/retención;
- recursos y rollback;
- E2E, accesibilidad, carga y DAST.

## Criterio de avance

Un corte se considera integrado cuando migraciones, backend, frontend, PowerShell, contenedores, runtime smoke y documentación pasan en `main`.
