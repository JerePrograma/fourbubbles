# Hoja de ruta

Versión base: `0.4.1`.

## Completado

### 0.1 — Plataforma y administración

Seguridad, catálogo, clientes, pedidos, pagos, React, Docker, CI y auditoría.

### 0.2 — Recepción física

Flyway `V7`, idempotencia, realidad física, diferencias y decisión.

### 0.3 — Compatibilidad explicable

Flyway `V8`, perfiles, `COMPAT-1`, evaluaciones y excepción.

### 0.4 — Producción base

Flyway `V9`/`V10`, máquinas, programas, ciclos, capacidad, lavado, secado y calidad.

### 0.4.1 — Separación trazable

Flyway `V11`, contenedores por pedido, confirmación auditada, bloqueo de inicio, UI y pruebas.

## Endurecimiento siguiente

1. E2E de navegador para producción/separación.
2. Alinear versiones de `pom.xml` y `package.json` con la versión funcional.
3. Administración UI completa de máquinas/programas.
4. Métricas de duración, capacidad y fallos.
5. Evidencia opcional de separación sin almacenar binarios en PostgreSQL.

## 0.5 — Logística y agenda

- franjas, rutas y paradas;
- conductor y orden de visita;
- retiro/entrega real;
- kilómetros, combustible y tiempo;
- incidencias, mensajes y capacidad diaria.

## 0.6 — Caja, costos y rentabilidad

- caja/arqueo;
- ingresos, egresos y reembolsos;
- conciliación;
- costos por ciclo/pedido;
- margen y rentabilidad.

## 0.7 — Crecimiento

- abonos/SLA;
- inventario/lotes;
- mantenimiento;
- reclamos/compensaciones;
- tableros y alertas.

## Transversal antes de producción real

TLS, secretos administrados, backup/restore, object storage, observabilidad, rate limit distribuido, privacidad, recursos, rollback, E2E, accesibilidad, carga y DAST.

## Criterio de avance

Un corte se considera integrado cuando migraciones, backend, frontend, PowerShell, contenedores, runtime smoke y documentación pasan en `main`.
