# Alcance funcional

Este documento traduce el alcance completo del negocio a módulos y marca qué existe realmente.

| Módulo | Entidades/reglas principales | Estado actual |
|---|---|---|
| Autenticación y usuarios | usuarios, roles, sesiones, refresh token | Implementado inicial |
| Clientes | datos básicos, captación, preferencias, saldos, historial | Parcial |
| Domicilios | principal/alternativos, zona, referencias, coordenadas | Parcial; sin coordenadas/rutas |
| Zonas | Marcos Paz, Mariano Acosta, vigencia | Implementado inicial |
| Pedidos | cotización, recepción, estados, evidencias, entrega | Parcial |
| Prendas/equivalencias | piezas, grupos, unidades, vigencia y restricciones | Implementado inicial |
| Precios | vigencia, zona, servicio, histórico y desglose | Implementado inicial; faltan dimensiones completas |
| Promociones | cupos, domicilio, primera compra, descuentos/créditos | Parcial |
| Abonos | planes, usos, vigencia, pausa, renovación | Pendiente |
| Compatibilidad | colores, alergias, productos, temperatura, exclusividad | Pendiente |
| Lavado/secado | ciclos, máquinas, carga, productos, tiempos e incidentes | Pendiente |
| Logística | rutas, paradas, franjas, kilómetros e incidencias | Pendiente |
| Bolsas | reutilizables, red, ubicación, depósito e historial | Pendiente |
| Pagos | métodos, parciales, saldo | Implementado básico |
| Caja | apertura, movimientos, arqueo y diferencias | Pendiente |
| Costos | insumos, servicios, transporte, mano de obra y amortización | Pendiente |
| Equipamiento | máquinas, capacidad, mantenimiento, fallas | Pendiente |
| Inventario | stock, lotes, consumo y alertas | Pendiente |
| Reclamos | evidencia, análisis, compensación y autorización | Pendiente |
| Políticas | versiones y aceptación | Pendiente |
| WhatsApp | plantillas, enlaces, historial y recordatorios | Pendiente |
| Comercios | tarifas, frecuencia, SLA y cuenta corriente | Pendiente |
| Métricas | tablero, alertas y rentabilidad | Pendiente |
| Proyecciones | escenarios editables y recuperación | Pendiente |
| Competencia | relevamientos comparables | Pendiente |
| Configuración | datos comerciales versionados | Parcial |
| Auditoría | cambios sensibles con actor y valores | Implementado en operaciones actuales |

## Regla de alcance

Una entidad creada sin su regla crítica no se considera módulo implementado. Por ejemplo, una tabla `wash_cycle` sin matriz de compatibilidad no cumpliría Fase 2.

## Datos iniciales cargados

- dos zonas;
- once servicios;
- veintiuna equivalencias;
- once precios iniciales;
- nueve promociones;
- cuatro medios de pago.

Los datos comerciales están en Flyway y no en constantes Java.
