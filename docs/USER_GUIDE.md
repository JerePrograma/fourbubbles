# Guía de uso funcional

Versión: `0.1.2`.

Describe únicamente funciones disponibles. Los estados de recepción/producción no sustituyen los módulos físicos todavía pendientes.

## 1. Iniciar sesión

Abrir:

```text
http://localhost:8080
```

En desarrollo, usar el usuario y la contraseña mostrados por `Start-Local.ps1`.

El access token permanece en memoria. La renovación utiliza una cookie `HttpOnly`.

## 2. Roles

| Rol | Uso |
|---|---|
| `ADMIN` | administración completa, cotización manual y auditoría |
| `OPERATOR` | clientes, domicilios, pedidos, planificación, confirmación y pagos |
| `DRIVER` | lectura y transiciones operativas habilitadas |
| `REPORT_VIEWER` | consulta |

## 3. Crear cliente

1. Entrar en **Clientes**.
2. Seleccionar **Nuevo cliente**.
3. Completar nombre, apellido, teléfono y WhatsApp.
4. Agregar correo y origen cuando correspondan.
5. Configurar preferencias.
6. Cargar al menos un domicilio.
7. Marcar exactamente uno como principal.
8. Guardar.

Preferencias:

- fragancia;
- suavizante permitido;
- secadora permitida;
- tratamiento hipoalergénico;
- separación de colores;
- instrucciones especiales.

## 4. Buscar y editar cliente

Desde **Clientes**:

- buscar por apellido;
- abrir el perfil;
- modificar datos;
- cambiar estado;
- actualizar preferencias;
- administrar domicilios.

WhatsApp no puede repetirse en otro cliente activo.

## 5. Administrar domicilios

### Agregar

1. Abrir **Editar cliente**.
2. Ir a domicilios.
3. Informar zona, calle, número, localidad y referencias.
4. Elegir si será principal.
5. Guardar.

### Cambiar principal

1. Seleccionar un domicilio activo alternativo.
2. Elegir **Hacer principal**.
3. El anterior continúa activo, pero deja de ser principal.

### Desactivar

1. Verificar que no sea el principal.
2. Elegir **Desactivar**.
3. El registro pasa al historial.

Reglas:

- siempre debe existir un domicilio activo;
- el principal no se desactiva directamente;
- solo existe un principal activo;
- los pedidos anteriores mantienen su domicilio histórico.

## 6. Crear pedido

1. Entrar en **Pedidos**.
2. Seleccionar **Nuevo pedido**.
3. Elegir cliente.
4. Elegir domicilio activo.
5. Elegir servicio.
6. Informar promoción cuando corresponda.
7. Agregar prendas y cantidades físicas.
8. Revisar la vista previa.
9. Informar peso declarado opcional.
10. Informar retiro, promesa y notas opcionales.
11. Crear y cotizar.

La vista previa muestra:

- piezas físicas;
- grupos comerciales;
- unidades equivalentes;
- peso estimado;
- necesidad de presupuesto;
- necesidad de ciclo exclusivo;
- límite operativo alcanzado.

## 7. Interpretar los precios

El detalle diferencia:

- **precio automático**: cálculo original;
- **precio cotizado**: propuesta vigente;
- **precio confirmado**: valor congelado aceptado.

El desglose muestra las líneas que explican el total.

## 8. Cotización manual

Solo `ADMIN`.

Se utiliza cuando `requiresQuote` es verdadero o cuando el tratamiento exige revisión individual.

1. Abrir el pedido.
2. Ir a **Cotización manual**.
3. Ingresar importe.
4. Escribir un motivo concreto.
5. Guardar.

El sistema registra:

- importe automático original;
- importe manual;
- diferencia;
- motivo;
- actor;
- fecha.

No se permite cotizar manualmente después de confirmar el precio ni fuera de `INQUIRY`/`QUOTED`.

## 9. Editar planificación temprana

`ADMIN` y `OPERATOR` pueden modificar:

- retiro programado;
- fecha prometida;
- notas.

Solo durante:

```text
INQUIRY
QUOTED
```

Después de confirmar o avanzar el estado, la edición se rechaza.

## 10. Confirmar precio

1. Revisar precio y desglose.
2. Verificar que no quede presupuesto pendiente.
3. Seleccionar **Confirmar precio**.

Al confirmar:

- el precio se congela;
- la promoción se bloquea;
- se revalidan vigencia y cupos;
- se registra el consumo promocional;
- se audita la operación.

Dos pedidos concurrentes no pueden consumir el mismo beneficio restringido.

## 11. Cambiar estado

El detalle muestra únicamente `allowedTransitions`.

Recorrido orientativo:

```text
QUOTED
→ WAITING_CONFIRMATION
→ RESERVED
→ PICKUP_SCHEDULED
→ PICKED_UP
→ RECEIVED
→ PENDING_INSPECTION
→ CLASSIFIED
→ WAITING_WASH
→ WASHING
→ DRYING
→ QUALITY_CONTROL
→ FOLDING
→ PACKAGED
→ READY_FOR_DELIVERY
→ DELIVERY_SCHEDULED
→ DELIVERED
→ CLOSED
```

También existen cancelación, reclamo y reembolso como estados, pero sus módulos completos todavía no están implementados.

`RECEIVED`, `WASHING` o `DELIVERED` no crean por sí mismos peso real, ciclos o rutas.

## 12. Registrar pago

Requisitos:

- precio confirmado;
- saldo disponible;
- rol `ADMIN` u `OPERATOR`.

Pasos:

1. Elegir medio.
2. Ingresar importe.
3. Agregar referencia opcional.
4. Registrar.

Medios iniciales:

- efectivo;
- transferencia;
- Mercado Pago;
- otro.

El sistema impide:

- cero o negativos;
- cobrar sin precio confirmado;
- superar el saldo;
- sobrecobrar mediante pagos concurrentes.

## 13. Consultar historial de pagos

En el detalle se muestran:

- fecha;
- medio;
- importe;
- referencia;
- notas;
- estado;
- usuario que registró.

No existen todavía reembolsos, comprobantes adjuntos ni caja diaria.

## 14. Consultar auditoría

Solo `ADMIN`.

1. Abrir **Auditoría**.
2. Filtrar opcionalmente por entidad, ID o acción.
3. Revisar actor, fecha, motivo y valores.

Entidades frecuentes:

```text
CLIENT
CLIENT_ADDRESS
ORDER
PAYMENT
```

Acciones frecuentes:

```text
CREATE
UPDATE
MAKE_PRIMARY
DEACTIVATE
MANUAL_QUOTE
UPDATE_PLANNING
CONFIRM_PRICE
STATUS_CHANGE
REGISTER
```

## 15. Qué hacer ante un error

- copiar el `X-Request-ID` de la respuesta;
- no compartir tokens ni cookies;
- revisar el mensaje funcional;
- consultar logs del backend con ese request ID;
- verificar que el rol sea correcto;
- comprobar que el pedido esté en un estado editable.

## 16. Flujo completo de demostración

1. Crear cliente.
2. Agregar segundo domicilio.
3. Convertirlo en principal.
4. Crear pedido.
5. Revisar equivalencias.
6. Ajustar planificación.
7. Aplicar cotización manual si corresponde.
8. Confirmar precio.
9. Registrar pago parcial.
10. Registrar pago final.
11. Revisar historial.
12. Cambiar estado por una transición permitida.
13. Consultar auditoría.
14. Desactivar el domicilio anterior y verificar historial.

## 17. Límites que deben comunicarse al usuario operativo

- no cargar todavía recepción física como si estuviera documentada;
- no usar estados de lavado como sustituto de ciclos;
- no prometer rutas optimizadas;
- no considerar pagos equivalentes a caja contable;
- no adjuntar evidencias porque aún no existe almacenamiento;
- no inferir rentabilidad a partir del precio cobrado.
