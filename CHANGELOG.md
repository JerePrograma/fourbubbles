# Changelog

## 0.4.0 — Producción base

Fecha: 2026-07-24.

### Agregado

- módulo backend `production` con API, aplicación, dominio y persistencia;
- máquinas `WASHER`/`DRYER`, capacidad y estado operativo;
- programas `WASH`/`DRY` con validación técnica;
- ciclos `PLANNED`, `RUNNING`, `COMPLETED` y `CANCELLED`;
- asignación idempotente de uno o dos pedidos;
- bloqueo de máquina, programas, pedidos y ciclos;
- capacidad planificada y real en gramos;
- integración con perfiles y evaluación `COMPAT-1` vigente;
- marca `separationRequired` cuando una excepción habilita una carga compartida;
- estados `WAITING_WASH`, `WASHING`, `WAITING_DRY`, `DRYING`, `QUALITY_CONTROL`, `FOLDING` y `REWASH_REQUIRED`;
- control de calidad `PASS`/`REWASH`;
- historial y auditoría de ciclos;
- migraciones Flyway `V9` y `V10`;
- protección de parámetros técnicos de programas ya utilizados;
- UI de producción y modelos TypeScript;
- pruebas unitarias, de autorización, flujo y concurrencia;
- documentación técnica y operativa reconciliada.

### Endurecido

- runtime smoke y verificación local exigen al menos diez migraciones;
- se preservó el hardening de puertos, Compose, Nginx y `.env`;
- no se incorporó el workflow diagnóstico temporal usado durante el desarrollo.

### Límites conscientes

- `separationRequired` no rastrea bolsas o compartimentos;
- no hay optimizador automático de cargas;
- no hay consumo de insumos, costos, mantenimiento completo ni secado natural modelado;
- la UI ofrece operación base, no administración avanzada completa.

## 0.3.0 — Compatibilidad explicable

- Flyway `V8`;
- perfiles efectivos;
- motor `COMPAT-1`;
- evaluaciones históricas, razones, recomendación y excepción `ADMIN`;
- UI y pruebas concurrentes.

## 0.2.0 — Recepción física

- Flyway `V7`;
- recepción idempotente;
- peso/conteo real, inspección, diferencias, decisión y metadata de evidencia.

## 0.1.2 — Cierre administrativo

- domicilios versionados, cotización manual, planificación, promociones/pagos concurrentes y auditoría.

## 0.1.0–0.1.1 — Plataforma y operación

- seguridad, PostgreSQL, catálogo, clientes, pedidos, pagos, React, Docker, CI y hardening operativo.
