# Estrategia y estado de pruebas

Última actualización: 2026-07-20.

## Evidencia automatizada

El pipeline definitivo `.github/workflows/ci.yml` ejecuta tres jobs independientes:

1. **Backend**
   - Java 21 Temurin;
   - `mvn clean verify`;
   - pruebas unitarias JUnit 5/Mockito;
   - prueba de integración `*IT` mediante Maven Failsafe;
   - PostgreSQL 16 real mediante Testcontainers;
   - migraciones Flyway completas;
   - validación Hibernate/JPA contra el esquema.
2. **Frontend**
   - Node.js 22;
   - instalación reproducible con `npm ci`;
   - validación TypeScript mediante `npm run lint`;
   - construcción Vite mediante `npm run build`.
3. **Contenedores**
   - validación del modelo Compose;
   - construcción de imágenes backend y frontend.

Comandos equivalentes:

```bash
cd backend
mvn clean verify

cd ../frontend
npm ci
npm run lint
npm run build

cd ..
docker compose config --quiet
docker compose build
```

## Pruebas unitarias implementadas

### Equivalencias

- agrupación de medias;
- agrupación incompleta de ropa interior;
- redondeo hacia arriba de grupos incompletos;
- preservación de piezas físicas;
- cálculo de unidades equivalentes.

### Límites del pedido

- límite exacto de 12 unidades;
- límite exacto de 2.500 gramos;
- exceso por equivalencias;
- exceso por peso;
- identificación del primer límite;
- bloqueo de capacidad segura.

### Estados

- transiciones permitidas;
- bloqueo de saltos inválidos.

### Precios y promociones

- precio base explicable;
- precio fijo promocional;
- primera compra;
- bloqueo de promoción que exige validación manual;
- cupos simulados de manera explícita en las pruebas.

El corte actual ejecuta 13 casos unitarios sin errores.

## Prueba de integración implementada

`ApplicationContextIT`:

1. inicia PostgreSQL 16 mediante Testcontainers;
2. configura Spring con el perfil `test`;
3. aplica V1 a V5 con Flyway;
4. construye el contexto completo;
5. obliga a Hibernate a ejecutar `ddl-auto=validate`;
6. detecta divergencias reales entre migraciones y entidades.

Esta prueba detectó durante la estabilización:

- uso de una palabra reservada como columna SQL;
- divergencia `CHAR(3)`/`VARCHAR(3)` en moneda;
- ambigüedad de mapeo UUID;
- errores de configuración criptográfica del JWT.

## Matriz de las 30 reglas originales

| # | Regla | Estado actual | Próxima evidencia requerida |
|---:|---|---|---|
| 1 | cálculo de unidades equivalentes | cubierta | ampliar casos configurables |
| 2 | agrupación de medias | cubierta | — |
| 3 | agrupación de ropa interior | cubierta | — |
| 4 | límite de 12 unidades | cubierta | prueba API |
| 5 | límite de 2,5 kg | cubierta | peso real en recepción |
| 6 | primer límite alcanzado | cubierta en dominio | prueba API/UI |
| 7 | dos pedidos compatibles | pendiente Fase 2 | matriz de compatibilidad |
| 8 | dos pedidos incompatibles | pendiente Fase 2 | matriz de compatibilidad |
| 9 | máximo 5 kg compartido | pendiente Fase 2 | entidad y política de ciclo |
| 10 | ciclo exclusivo | parcial en pedido | ciclo de producción |
| 11 | cálculo de precios | cubierta parcialmente | adicionales/impuestos/redondeo |
| 12 | vigencia de precios | cubierta por repositorio | más pruebas de borde temporal |
| 13 | primera compra | cubierta | prueba integrada con pedidos previos |
| 14 | una promoción por domicilio | implementada | concurrencia e integración |
| 15 | no acumulación | implementada por diseño | promociones múltiples futuras |
| 16 | cupos | parcial | bloqueo transaccional/concurrencia |
| 17 | abonos | pendiente Fase 5 | módulo de suscripciones |
| 18 | créditos | pendiente Fase 4/5 | libro de movimientos |
| 19 | pagos parciales | implementado | prueba de servicio/API |
| 20 | cambio de estado | cubierta | permisos por transición |
| 21 | auditoría | implementada | consultas y prueba integrada |
| 22 | margen | pendiente Fase 4 | motor de costos |
| 23 | costos | pendiente Fase 4 | motor de costos |
| 24 | punto de equilibrio | pendiente Fase 4 | simulador financiero |
| 25 | capacidad diaria | pendiente Fase 2 | calendario de máquinas |
| 26 | sobrecarga | pedido cubierto | ciclo y máquina pendientes |
| 27 | relavado | estado modelado | flujo productivo |
| 28 | reclamos | estado modelado | módulo de reclamos |
| 29 | eliminación lógica | esquema implementado | prueba de visibilidad/historial |
| 30 | permisos | configuración base | MockMvc por rol |

## Próximas pruebas prioritarias

1. MockMvc de login, refresh y logout.
2. Contrato uniforme de errores y validaciones.
3. Permisos por rol y operación.
4. Alta y consulta completa de cliente.
5. Creación de pedido y persistencia del precio histórico.
6. Confirmación de precio y consumo promocional.
7. Pago parcial, pago total y exceso rechazado.
8. Eliminación lógica y exclusión de registros inactivos.
9. Carrera de cupos promocionales con concurrencia real.
10. Pruebas React de autenticación y alta de cliente.
11. Smoke test HTTP del Compose iniciado.
12. E2E del recorrido cliente → pedido → pago.

## Criterio para cerrar una fase

Una fase no se considera finalizada hasta que:

- compila;
- todas sus migraciones aplican sobre PostgreSQL vacío;
- Hibernate valida el esquema;
- las reglas críticas poseen pruebas;
- frontend compila con lockfile;
- imágenes Docker se construyen;
- documentación y backlog reflejan el estado real.
