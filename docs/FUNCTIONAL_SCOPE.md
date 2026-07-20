# Alcance funcional

Este documento describe lo que existe realmente en `0.1.2` y separa implementación administrativa de operación física.

| Módulo | Estado | Alcance disponible | Pendiente principal |
|---|---|---|---|
| Autenticación | Implementado base | login, refresh, logout, JWT, cookie segura, bloqueo local | administración de usuarios, MFA, limitación distribuida |
| RBAC | Implementado | jerarquía de cuatro roles y permisos por método | permisos finos por transición |
| Clientes | Avanzado | alta, búsqueda, actualización, estado y preferencias | historial agregado y métricas |
| Domicilios | Implementado administrativo | múltiples, principal único, vigencia, baja e historial | coordenadas, geocodificación y validación logística |
| Zonas | Implementado inicial | Marcos Paz y Mariano Acosta | radios, restricciones y adicionales |
| Catálogo | Implementado inicial | servicios y equivalencias versionados | interfaz administrativa completa |
| Precios | Implementado | vigencia, zona, histórico, automático y manual | reglas compuestas y simulación |
| Promociones | Implementado para reglas soportadas | vigencia, servicio, cupos, primera compra, domicilio y concurrencia | créditos, autorización y reglas compuestas |
| Pedidos | Avanzado | alta, cotización, planificación temprana, estados y búsqueda | recepción y edición física controlada |
| Recepción | Pendiente | solo existe transición de estado | peso/conteo real, daños, fotos, diferencias, aceptación |
| Pagos | Implementado base robusto | parciales, totales, saldo, historial y concurrencia | caja, reembolso, comprobantes, webhooks |
| Auditoría | Implementado administrativo | persistencia, filtros e interfaz | exportación, retención y comparación visual avanzada |
| Compatibilidad | Pendiente | preferencias capturadas como insumo | matriz y motor de reglas |
| Producción | Pendiente | estados preparatorios | ciclos, máquinas, lavado, secado y calidad |
| Logística | Pendiente | retiro/promesa en pedido | rutas, paradas, agenda y entrega |
| Finanzas | Parcial mínimo | cobros por pedido | caja, costos, margen y proyecciones |
| Inventario | Pendiente | ninguno | stock, lotes, consumo y alertas |
| Reclamos | Pendiente | estado `CLAIM` solamente | circuito, evidencias y compensación |
| Crecimiento | Pendiente | semillas promocionales | abonos, comercios, SLA y tableros |

## Reglas funcionales finalizadas

### Cliente y domicilio

- un WhatsApp activo no puede pertenecer a dos clientes;
- cada cliente debe conservar al menos un domicilio activo;
- existe exactamente un principal activo;
- el principal no puede darse de baja hasta elegir otro;
- la baja no elimina el domicilio;
- pedidos anteriores conservan su referencia histórica;
- el cambio de principal respeta el índice único incluso durante el flush de JPA.

### Pedido

- conserva piezas físicas y equivalencias calculadas;
- aplica el primer límite operativo alcanzado;
- guarda precio automático, desglose y definición de precio;
- marca si necesita presupuesto o ciclo exclusivo;
- una cotización manual no borra el cálculo automático;
- retiro, promesa y notas solo pueden editarse en `INQUIRY` o `QUOTED`;
- el precio confirmado no cambia retrospectivamente;
- los cambios de estado deben respetar la política de transiciones.

### Promoción

- la cotización evalúa si la regla es potencialmente aplicable;
- la confirmación bloquea la promoción;
- vuelve a validar estado, vigencia, servicio, primera compra, domicilio y cupos;
- registra el uso dentro de la misma transacción;
- dos confirmaciones concurrentes no pueden consumir el mismo beneficio restringido.

### Pago

- exige precio confirmado;
- exige importe positivo;
- no permite superar el saldo;
- bloquea el pedido durante el cálculo y persistencia;
- dos pagos concurrentes no pueden sobrecobrar;
- conserva medio, fecha, actor, referencia, notas y estado.

## Datos iniciales

- 2 zonas;
- 11 servicios;
- 21 equivalencias;
- 11 precios iniciales;
- 9 promociones;
- 4 medios de pago.

Los datos comerciales se cargan por Flyway, no mediante constantes de negocio Java.

## Regla de honestidad de alcance

Una tabla o estado no equivale a un módulo terminado. Por ejemplo:

- `RECEIVED` no implica que exista recepción real;
- `WASHING` no implica que existan ciclos o máquinas;
- `DELIVERY_SCHEDULED` no implica que exista una ruta;
- `CLAIM` no implica que exista gestión de reclamos.

La funcionalidad se considera implementada únicamente cuando existen reglas, persistencia, API, permisos, interfaz cuando corresponde y pruebas representativas.
