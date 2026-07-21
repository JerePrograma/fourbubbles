# Alcance funcional

Versión: `0.3.0`.

Este documento describe funciones utilizables, no solamente tablas o estados.

## Estado por módulo

| Módulo | Estado | Alcance disponible | Pendiente principal |
|---|---|---|---|
| Autenticación | Implementado base | login, refresh, logout, JWT, cookie segura y bloqueo local | usuarios UI, MFA y limitación distribuida |
| RBAC | Implementado | jerarquía y permisos por método | permisos finos por transición |
| Clientes | Avanzado | alta, búsqueda, actualización, estado y preferencias | timeline agregado |
| Domicilios | Implementado administrativo | múltiples, principal, vigencia, baja e historial | coordenadas/geocodificación |
| Zonas | Inicial | Marcos Paz y Mariano Acosta | radios y restricciones avanzadas |
| Catálogo | Inicial versionado | servicios y equivalencias | CRUD administrativo UI |
| Precios | Implementado | automático/manual, vigencia, histórico y desglose | simulación y reglas compuestas |
| Promociones | Implementado soportado | cupos, primera compra, domicilio y concurrencia | créditos y reglas compuestas |
| Pedidos | Avanzado | alta, cotización, planificación, estados y búsqueda | edición física versionada posterior |
| Recepción | Implementado base | idempotencia, peso/conteo real, inspección, diferencias y aprobación | binarios y correcciones versionadas |
| Evidencias | Parcial | metadata, tamaño, MIME y SHA-256 | upload/download y object storage |
| Compatibilidad | Implementado base | perfil, evaluación por pares, razones, recomendación, historial y excepción | matriz administrable y comparación múltiple |
| Pagos | Implementado base robusto | parciales, totales, saldo, historial y concurrencia | caja, reembolsos y webhooks |
| Auditoría | Implementado base | eventos sensibles y consulta paginada | exportación/retención |
| Producción | Pendiente | compatibilidad como insumo | ciclos, máquinas, capacidad y calidad |
| Logística | Pendiente | fechas de retiro/promesa | rutas, paradas y kilómetros |
| Finanzas | Parcial mínimo | cobros | caja, costos, margen y conciliación |
| Crecimiento | Pendiente | base comercial | abonos, inventario, mantenimiento y reclamos |

## Flujo implementado

```text
cliente
  -> domicilio activo
  -> pedido declarado
  -> precio automático/manual
  -> confirmación y retiro
  -> PICKED_UP
  -> recepción real idempotente
  -> CLASSIFIED o WAITING_PRICE_APPROVAL
  -> perfil de tratamiento
  -> evaluación explicable contra otro CLASSIFIED
  -> compatible / bloqueado / excepción ADMIN
```

La evaluación de compatibilidad no crea un ciclo ni cambia el estado del pedido.

## Perfil de tratamiento

Solo puede crearse o modificarse mientras el pedido está `CLASSIFIED` y posee recepción.

Atributos:

- grupo de color;
- grupo de material;
- temperatura máxima;
- secadora;
- política de fragancia;
- suavizante;
- hipoalergénico;
- ropa de bebé;
- contacto con mascotas;
- suciedad pesada;
- ciclo exclusivo;
- notas.

El perfil guardado es efectivo, no una copia ciega del formulario. Se aplican reglas no relajables:

- `dryerAllowed=false` del cliente prevalece;
- `softenerAllowed=false` del cliente prevalece;
- `hypoallergenic=true` prevalece y fuerza `fragrancePolicy=NONE`;
- `exclusiveCycle=true` del pedido o cliente prevalece.

## Evaluación de compatibilidad

Requiere dos pedidos diferentes, ambos `CLASSIFIED` y con perfil.

El motor `COMPAT-1` genera:

- `compatible` original;
- lista de razones con severidad `HARD` o `WARNING`;
- temperatura máxima común;
- uso de secadora y suavizante;
- política de fragancia;
- programa recomendado;
- modo `SHARED` o `BLOCKED`.

Reglas duras iniciales:

- ciclo exclusivo;
- color desconocido o distinto;
- materiales incompatibles;
- aislamiento hipoalergénico;
- cruce bebé/mascotas;
- suciedad pesada contra carga sensible;
- política de fragancia distinta.

Advertencias iniciales:

- reducción de temperatura;
- deshabilitación de secadora;
- deshabilitación de suavizante.

La evaluación se reutiliza si coinciden el par ordenado, las versiones de ambos perfiles y la versión de reglas. Si cambia un perfil, se crea un nuevo snapshot histórico.

## Excepción administrativa

Solo `ADMIN` puede autorizarla y debe informar un motivo.

La excepción:

- no cambia `compatible`;
- establece `overridden=true`;
- establece `effectivelyCompatible=true`;
- conserva actor y fecha;
- queda auditada;
- no puede duplicarse.

## Roles

| Operación | ADMIN | OPERATOR | DRIVER | REPORT_VIEWER |
|---|---:|---:|---:|---:|
| crear/editar cliente y domicilio | Sí | Sí | No | No |
| crear/editar pedido temprano | Sí | Sí | No | No |
| cotización manual | Sí | No | No | No |
| registrar recepción | Sí | Sí | Sí | No |
| decidir diferencia de recepción | Sí | Sí | No | No |
| guardar perfil | Sí | Sí | No | No |
| evaluar compatibilidad | Sí | Sí | No | No |
| consultar perfil/evaluación | Sí | Sí | Sí | Sí |
| autorizar excepción | Sí | No | No | No |
| registrar pago | Sí | Sí | No | No |
| consultar auditoría | Sí | No | No | No |

## Límites conscientes

- La compatibilidad es por pares, no un optimizador de lotes.
- Una excepción no asigna pedidos a una máquina.
- No se valida capacidad porque todavía no existen ciclos/máquinas.
- La evidencia de recepción es metadata; no existe almacenamiento binario integrado.
- Los estados posteriores a `CLASSIFIED` no representan ejecución física completa.
