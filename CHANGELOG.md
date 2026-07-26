# Changelog

## 0.4.2 — Métricas productivas

Fecha: 2026-07-26.

### Agregado

- endpoint autenticado `GET /production/metrics`;
- ventana predeterminada de 30 días y máximo de 366 días;
- conteos por estado y etapa;
- pedidos asignados, cargas compartidas y separación pendiente;
- pesos planificado/real y duración media de ciclos completados;
- porcentajes de finalización y preparación de separación;
- pantalla **Métricas** para los cuatro roles;
- pruebas de integración, autorización y lógica frontend;
- artefactos de diagnóstico backend en CI cuando Maven falla.

### Corregido

- las pruebas MockMvc envían `from` y `to` como parámetros reales, sin codificar una URL completa;
- los reportes Surefire/Failsafe y el log Maven quedan disponibles durante siete días ante fallos.

### Límite consciente

Las métricas usan snapshots ya persistidos. No calculan costos, capacidad histórica ni rendimiento económico, y no reinterpretan la capacidad actual de una máquina sobre ciclos pasados.

## 0.4.1 — Separación física trazable

- Flyway `V11`;
- contenedores por pedido, confirmación auditada, unicidad y bloqueo de inicio;
- API, UI y pruebas.

## 0.4.0 — Producción base

- máquinas, programas, ciclos, capacidad, lavado, secado, calidad, UI y pruebas.

## 0.3.0 — Compatibilidad explicable

- Flyway `V8`, perfiles, `COMPAT-1`, evaluaciones históricas y excepción `ADMIN`.

## 0.2.0 — Recepción física

- Flyway `V7`, recepción idempotente, realidad física, diferencias y decisión.

## 0.1.0–0.1.2 — Plataforma y administración

- seguridad, PostgreSQL, catálogo, clientes, pedidos, pagos, auditoría, React, Docker y CI.
