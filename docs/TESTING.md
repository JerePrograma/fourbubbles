# Estrategia de pruebas

## Ejecutadas por diseño

### Unitarias

- agrupación de medias;
- agrupación incompleta de ropa interior;
- preservación de piezas físicas;
- límites exactos de 12 unidades y 2,5 kg;
- exceso por equivalencias;
- exceso por peso;
- bloqueo de capacidad segura;
- transiciones permitidas y saltos inválidos;
- precio base explicable;
- precio fijo promocional;
- primera compra;
- promoción que exige validación manual.

### Integración

`ApplicationContextIT` levanta PostgreSQL real con Testcontainers, aplica Flyway y obliga a Hibernate a validar el esquema.

## Pendientes prioritarios

- MockMvc de login, refresh, logout y errores uniformes;
- permisos por rol;
- alta completa de cliente;
- pedido y persistencia de precio histórico;
- pago parcial y pago total;
- carrera de cupos promocionales;
- eliminación lógica;
- recorridos E2E del frontend.

## Matriz original de 30 pruebas

| Regla | Estado |
|---|---|
| equivalencias, medias, ropa interior | cubierta |
| límites 12/2,5 y primer límite | cubierta parcialmente |
| compatibilidad y ciclo 5 kg | Fase 2 |
| ciclo exclusivo | modelado parcial, sin ciclo |
| precios y vigencia | prueba parcial; integración pendiente |
| promociones primera compra/domicilio/cupos | parcial |
| abonos/créditos | pendiente |
| pagos parciales | código; prueba pendiente |
| estados | cubierta |
| auditoría | integración pendiente |
| margen/costos/punto equilibrio | Fase 4 |
| capacidad/sobrecarga | pedido cubierto; ciclos pendientes |
| relavado/reclamos | Fase 2/5 |
| eliminación lógica | esquema; prueba pendiente |
| permisos | configuración; prueba pendiente |
