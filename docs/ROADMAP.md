# Hoja de ruta

Versión base: `0.3.0`.

## Completado

### 0.1.0 — Plataforma y núcleo

Arquitectura, seguridad, PostgreSQL/Flyway, catálogo, clientes, pedidos, pagos, React, Docker y CI.

### 0.1.1 — Hardening y operación

Contratos de seguridad, correlación, preferencias tipadas y UI operativa.

### 0.1.2 — Cierre administrativo

Domicilios versionados, cotización manual, planificación, promociones/pagos concurrentes, auditoría y documentación integral.

### 0.2.0 — Recepción física

- Flyway V7;
- recepción idempotente;
- peso/conteo real;
- inspección y diferencias;
- aprobación/rechazo;
- etiqueta, bolsa y evidencia metadata;
- UI y pruebas concurrentes.

### 0.3.0 — Compatibilidad explicable

- Flyway V8;
- perfil efectivo por pedido/recepción;
- color, material, temperatura y productos;
- condiciones hipoalergénicas, bebé, mascotas, suciedad y exclusividad;
- motor `COMPAT-1` con razones y recomendación;
- snapshots por versión;
- bloqueos transaccionales;
- excepción exclusiva de `ADMIN`;
- UI y pruebas.

## Próximo corte — 0.4.0 Producción base

Objetivo: convertir compatibilidad en una asignación física controlada.

### Máquinas

- entidad máquina;
- tipo lavadora/secadora;
- capacidad en gramos;
- estado operativo;
- fechas de alta/baja;
- mantenimiento básico;
- CRUD administrativo mínimo.

### Programas

- nombre y tipo;
- temperatura;
- duración estimada;
- material compatible;
- secadora/suavizante/fragancia admitidos;
- vigencia.

### Ciclos

- identificador legible;
- máquina;
- programa;
- capacidad planificada y real;
- estado del ciclo;
- inicio/fin;
- operador;
- uno o dos pedidos;
- modo compartido/exclusivo.

### Asignación

- solo pedidos `CLASSIFIED`;
- perfil existente;
- evaluación compatible efectiva para dos pedidos;
- exclusividad respetada;
- peso total menor o igual a capacidad;
- bloqueo de máquina/pedidos;
- idempotencia o constraint para evitar doble asignación;
- historial/auditoría.

### Ejecución mínima

- planificar ciclo;
- iniciar lavado;
- finalizar lavado;
- iniciar/finalizar secado si aplica;
- control de calidad;
- relavado o avance a doblado.

### Pruebas obligatorias

- capacidad exacta permitida;
- sobrepeso rechazado;
- incompatibles rechazados;
- excepción efectiva admitida;
- pedido exclusivo no compartido;
- máquina fuera de servicio rechazada;
- asignación concurrente sin duplicados;
- transiciones y auditoría.

## 0.5.0 — Logística y agenda

- franjas horarias;
- rutas y paradas;
- asignación de conductor;
- orden de visita;
- retiro/entrega reales;
- kilómetros, combustible y tiempo;
- incidencias;
- mensajes/plantillas de WhatsApp;
- agenda diaria con capacidad.

## 0.6.0 — Caja, costos y rentabilidad

- caja diaria y arqueo;
- ingresos/egresos;
- reembolsos;
- comprobantes;
- conciliación;
- costos de insumos, energía, agua, transporte y mano de obra;
- costo por ciclo/pedido;
- amortización;
- margen y rentabilidad.

## 0.7.0 — Crecimiento

- abonos;
- comercios y SLA;
- inventario y lotes;
- compras/consumo;
- equipamiento/mantenimiento;
- reclamos, evidencias y compensaciones;
- políticas y aceptación;
- tableros y alertas.

## Transversal antes de producción

- TLS y dominio;
- secretos administrados;
- backup/restore probado;
- object storage privado;
- observabilidad central;
- rate limit distribuido;
- política de privacidad;
- límites de recursos;
- rollback ensayado;
- pruebas E2E, carga y seguridad.

## Criterio de avance

Un corte solo se fusiona a `main` cuando:

1. migración aditiva y validación JPA pasan;
2. pruebas unitarias/integración pasan;
3. TypeScript, Vitest y build pasan;
4. imágenes se construyen;
5. runtime smoke pasa;
6. workflows diagnósticos fueron eliminados;
7. documentación refleja exactamente el alcance y los límites.
