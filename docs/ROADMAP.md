# Hoja de ruta

Versión base: `0.1.2`.

## Completado

### 0.1.0 — Plataforma y núcleo inicial

- arquitectura;
- seguridad base;
- PostgreSQL/Flyway;
- catálogo;
- clientes;
- pedidos;
- estados;
- pagos;
- frontend inicial;
- Docker y CI.

### 0.1.1 — Hardening y flujo operativo

- 401/403 uniformes;
- correlación;
- protección de login;
- preferencias tipadas;
- búsqueda y detalle de pedidos;
- UI operativa;
- pruebas de contrato e integración.

### 0.1.2 — Cierre administrativo

- domicilios versionados;
- cotización manual;
- planificación temprana;
- historial de pagos;
- auditoría consultable;
- jerarquía RBAC;
- concurrencia promocional;
- concurrencia financiera;
- smoke runtime;
- verificación local autenticada.

## Próximo corte: 0.2.0 — Recepción

Objetivo: convertir la transición `RECEIVED` en una operación física trazable.

### Datos

- registro de recepción por pedido;
- clave idempotente;
- fecha y operador;
- peso declarado y real;
- conteo declarado y real;
- diferencias;
- observaciones;
- daños y manchas;
- estado de aprobación;
- etiqueta y bolsa;
- metadatos de evidencias.

### Reglas

- una misma clave no duplica recepción;
- la recepción exige un estado compatible;
- diferencias relevantes requieren aprobación;
- el precio puede recalcularse sin perder snapshots anteriores;
- una recepción confirmada no se sobrescribe silenciosamente;
- las evidencias se almacenan fuera de PostgreSQL y se referencian por metadatos.

### API/UI

- formulario de recepción;
- comparación declarado/real;
- carga de inspección;
- decisión de aprobación;
- historial de recepciones/correcciones;
- timeline agregado del cliente.

### Pruebas

- idempotencia;
- concurrencia;
- diferencias;
- autorización;
- recalculo;
- rollback transaccional;
- smoke UI/API.

## 0.3.0 — Compatibilidad

- atributos de tratamiento normalizados;
- matriz de compatibilidad;
- motor explicable;
- propuestas de combinación;
- bloqueo de incompatibles;
- excepción de administrador con motivo;
- pruebas de combinaciones críticas.

## 0.4.0 — Producción

- máquinas;
- programas;
- ciclos;
- capacidad real;
- asignación de hasta dos pedidos cuando sea segura;
- ciclos exclusivos;
- bolsas de red;
- lavado/secado;
- control de calidad;
- relavado;
- doblado y embolsado;
- mantenimiento.

## 0.5.0 — Logística

- franjas;
- rutas;
- paradas;
- orden de visita;
- retiro y entrega;
- kilometraje;
- combustible;
- incidencias;
- agenda real;
- WhatsApp.

## 0.6.0 — Finanzas

- caja;
- arqueo;
- ingresos/egresos;
- reembolsos;
- comprobantes;
- cuentas por cobrar;
- costos por pedido/ciclo;
- mano de obra;
- amortización;
- margen y rentabilidad;
- escenarios.

## 0.7.0 — Crecimiento

- abonos;
- comercios;
- SLA;
- inventario;
- lotes y consumo;
- equipamiento;
- reclamos;
- compensaciones;
- políticas y aceptación;
- tableros y alertas.

## Línea transversal productiva

Debe avanzar en paralelo antes de operar comercialmente:

- TLS;
- secretos administrados;
- PostgreSQL gestionado;
- backups y restauración;
- almacenamiento de objetos;
- observabilidad;
- alertas;
- rate limiting compartido;
- escaneo de dependencias e imágenes;
- política de datos personales;
- rollback;
- versionado de API.

## Priorización

Orden obligatorio recomendado:

```text
recepción
→ compatibilidad
→ producción
→ logística
→ costos/finanzas
→ crecimiento
```

Razón: compatibilidad, ciclos y rutas necesitan composición y peso reales. Implementarlos antes de recepción produciría optimizaciones sobre datos declarados no confiables.

## Definición de terminado por corte

Un corte no está terminado por tener tablas. Requiere:

1. migración aditiva;
2. dominio y transacciones;
3. API y permisos;
4. interfaz cuando sea operativa;
5. pruebas unitarias/integración/concurrencia según riesgo;
6. runtime smoke;
7. documentación;
8. CI verde;
9. integración limpia en `main`.
