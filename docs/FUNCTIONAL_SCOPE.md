# Alcance funcional

Este documento traduce el negocio a módulos y marca qué existe realmente en `0.1.1`.

| Módulo | Entidades/reglas principales | Estado actual |
|---|---|---|
| Autenticación y usuarios | usuarios, roles, sesiones, refresh token, limitación de login | Implementado base; falta administración y limitación distribuida |
| Clientes | datos básicos, captación, preferencias, estado, historial | Avanzado; falta historial agregado |
| Domicilios | principal/alternativos, zona, referencias, vigencia | Parcial; alta inicial sin edición/versionado |
| Zonas | Marcos Paz, Mariano Acosta, vigencia | Implementado inicial |
| Pedidos | cotización, búsqueda, detalle, estados, recepción, entrega | Avanzado hasta operación administrativa; recepción pendiente |
| Prendas/equivalencias | piezas, grupos, unidades, vigencia y restricciones | Implementado inicial |
| Precios | vigencia, zona, servicio, histórico y desglose | Implementado automático; ajuste manual pendiente |
| Promociones | cupos, domicilio, primera compra, descuentos/créditos | Parcial; concurrencia y reglas complejas pendientes |
| Abonos | planes, usos, vigencia, pausa, renovación | Pendiente |
| Compatibilidad | colores, alergias, productos, temperatura, exclusividad | Pendiente |
| Lavado/secado | ciclos, máquinas, carga, productos, tiempos e incidentes | Pendiente |
| Logística | rutas, paradas, franjas, kilómetros e incidencias | Pendiente |
| Bolsas | reutilizables, red, ubicación, depósito e historial | Pendiente |
| Pagos | métodos, parciales, saldo, registro desde UI | Implementado básico; historial/caja pendientes |
| Caja | apertura, movimientos, arqueo y diferencias | Pendiente |
| Costos | insumos, servicios, transporte, mano de obra y amortización | Pendiente |
| Equipamiento | máquinas, capacidad, mantenimiento, fallas | Pendiente |
| Inventario | stock, lotes, consumo y alertas | Pendiente |
| Reclamos | evidencia, análisis, compensación y autorización | Pendiente |
| Políticas | versiones y aceptación | Pendiente |
| WhatsApp | plantillas, enlaces, historial y recordatorios | Pendiente |
| Comercios | tarifas, frecuencia, SLA y cuenta corriente | Pendiente |
| Métricas | tablero, alertas y rentabilidad | Pendiente real; pantalla estructural solamente |
| Proyecciones | escenarios editables y recuperación | Pendiente |
| Competencia | relevamientos comparables | Pendiente |
| Configuración | datos comerciales versionados | Parcial; sin interfaz administrativa |
| Auditoría | cambios sensibles con actor y valores | Implementado en operaciones actuales; consulta pendiente |

## Recorrido funcional disponible

1. autenticación;
2. alta, búsqueda y actualización de cliente;
3. registro de domicilio principal;
4. captura de preferencias tipadas;
5. consulta de servicios y equivalencias vigentes;
6. alta y cotización de pedido;
7. búsqueda y detalle;
8. confirmación de precio automático;
9. avance por transiciones permitidas;
10. pago parcial o total.

## Regla de alcance

Una tabla o pantalla sin su regla crítica no cuenta como módulo implementado. Por ejemplo:

- un estado `RECEIVED` no sustituye recepción con peso y evidencias;
- una pantalla Agenda vacía no constituye logística;
- un pago aislado no constituye caja;
- un campo de preferencia no constituye compatibilidad productiva;
- una tabla de ciclo sin matriz de compatibilidad no completa Fase 2.

## Datos iniciales

- dos zonas;
- once servicios;
- veintiuna equivalencias;
- once precios;
- nueve promociones;
- cuatro medios de pago.

Los datos comerciales residen en Flyway, no en constantes Java.

## Límites de 0.1.1

- sin peso real;
- sin evidencias;
- sin edición/versionado de domicilios;
- sin ajuste manual de cotización;
- sin administración de catálogo;
- sin compatibilidad ni ciclos;
- sin rutas;
- sin caja, costos o margen;
- sin abonos, inventario o reclamos.
