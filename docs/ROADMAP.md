# Hoja de ruta

Versión base: `0.2.0`.

## Completado

### 0.1.0 — Plataforma/núcleo

Arquitectura, seguridad, PostgreSQL/Flyway, catálogo, clientes, pedidos, pagos, React, Docker y CI.

### 0.1.1 — Hardening/operación

Contratos de seguridad, correlación, protección de login, preferencias tipadas y UI operativa.

### 0.1.2 — Cierre administrativo

Domicilios versionados, cotización manual, planificación, historial financiero, auditoría, RBAC y concurrencia.

### 0.2.0 — Recepción física

- Flyway V7;
- recepción única e idempotente;
- peso/conteo real;
- composición real por prenda;
- diferencias;
- daño/mancha;
- aprobación/rechazo;
- etiqueta/bolsa;
- evidencia metadata;
- UI;
- pruebas concurrentes.

## Próximo corte: 0.3.0 — Compatibilidad

Objetivo: decidir de forma explicable qué pedidos/prendas pueden compartir tratamiento usando datos reales de recepción.

### Modelo

- atributos normalizados de tratamiento;
- versiones de reglas;
- restricciones duras/blandas;
- evaluación por pedido;
- conflictos explicados;
- excepciones autorizadas.

### Variables

- color y transferencia;
- material;
- temperatura;
- secadora;
- fragancia/suavizante;
- hipoalergénico;
- bebé;
- mascotas;
- suciedad/contaminación;
- daño/mancha;
- ciclo exclusivo.

### Reglas

- incompatibles no se proponen juntos;
- resultado explicable;
- datos reales prevalecen sobre declaración;
- excepción solo `ADMIN`, con motivo;
- versiones históricas conservadas.

### UI/API

- perfil de tratamiento de recepción;
- evaluación individual;
- comparar pedidos;
- propuestas compatibles;
- conflictos;
- excepción auditada.

### Pruebas

- matrices críticas;
- simetría/transitividad cuando aplique;
- versionado;
- autorización;
- combinaciones concurrentes;
- explicación consistente.

## 0.4.0 — Producción

- máquinas/capacidad;
- programas;
- ciclos compartidos/exclusivos;
- asignación segura;
- sobrepeso;
- bolsas de red;
- lavado/secado;
- calidad/relavado;
- doblado/embolsado;
- mantenimiento.

## 0.5.0 — Logística

- franjas;
- rutas/paradas;
- orden de visita;
- retiro/entrega;
- kilometraje/combustible;
- incidencias;
- agenda;
- WhatsApp.

## 0.6.0 — Finanzas

- caja/arqueo;
- ingresos/egresos;
- reembolsos;
- comprobantes;
- conciliación;
- costos por pedido/ciclo;
- mano de obra/amortización;
- margen/rentabilidad.

## 0.7.0 — Crecimiento

- abonos;
- comercios/SLA;
- inventario;
- equipamiento;
- reclamos/compensaciones;
- políticas;
- tableros/alertas.

## Línea productiva transversal

- TLS;
- secretos gestionados;
- PostgreSQL gestionado;
- backups/restauración;
- object storage;
- URLs firmadas y escaneo;
- observabilidad;
- rate limiting compartido;
- SAST/dependencias/imágenes;
- privacidad/retención;
- rollback;
- versionado API.

## Priorización

```text
recepción ✓
→ compatibilidad
→ producción
→ logística
→ finanzas
→ crecimiento
```

Ahora existen peso/composición reales. Compatibilidad es el siguiente paso; crear ciclos antes repetiría el error de optimizar sin reglas de mezcla.

## Definición de terminado

Cada corte exige:

1. migración aditiva;
2. dominio/transacciones;
3. API/permisos;
4. UI cuando corresponda;
5. pruebas de riesgo y concurrencia;
6. runtime smoke;
7. documentación;
8. CI verde;
9. squash limpio en `main`.
