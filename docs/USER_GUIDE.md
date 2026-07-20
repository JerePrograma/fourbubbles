# Guía de uso funcional

Esta guía corresponde a la versión `0.1.1`. Describe únicamente funciones realmente disponibles.

## 1. Alcance utilizable desde la interfaz

- iniciar y cerrar sesión;
- renovar la sesión mediante cookie `HttpOnly`;
- buscar clientes por apellido;
- crear clientes con domicilio y preferencias;
- editar perfil, estado y preferencias de cliente;
- consultar servicios y equivalencias vigentes durante el alta de pedido;
- crear y cotizar pedidos;
- buscar pedidos por número y estado;
- consultar detalle, prendas, límites y desglose de precio;
- confirmar precios que no requieren ajuste manual;
- avanzar por las transiciones válidas;
- registrar pagos parciales o totales.

Swagger continúa disponible para inspección técnica, pero ya no es necesario para el recorrido operativo normal.

## 2. Iniciar sesión

1. Abrir `http://localhost:8080`.
2. Ingresar el valor de `APP_DEV_ADMIN_USERNAME`, inicialmente `admin`.
3. Ingresar la contraseña generada o configurada en `APP_DEV_ADMIN_PASSWORD`.
4. La UI guarda el access token solo en memoria.
5. La renovación usa la cookie de refresh.

Después de varios fallos consecutivos, el sistema puede devolver `LOGIN_RATE_LIMITED`. Los valores iniciales son cinco fallos y quince minutos de bloqueo.

## 3. Clientes

### Crear

1. Entrar en **Clientes**.
2. Seleccionar **Nuevo**.
3. Completar nombre, apellido, teléfono y WhatsApp.
4. Informar correo y origen de adquisición cuando existan.
5. Cargar el domicilio principal.
6. Seleccionar Marcos Paz o Mariano Acosta.
7. Registrar preferencias:
   - fragancia;
   - uso de suavizante;
   - uso de secadora;
   - tratamiento hipoalergénico;
   - separación de colores;
   - instrucciones especiales.
8. Guardar.

Reglas:

- WhatsApp único entre clientes activos;
- al menos un domicilio;
- exactamente un domicilio principal;
- zona activa obligatoria.

### Buscar

El listado permite filtrar por apellido. El backend limita el tamaño máximo de página a 100 registros.

### Editar

1. Seleccionar **Editar**.
2. Modificar perfil, estado o preferencias.
3. Guardar.

Los domicilios aparecen en modo consulta. La versión 0.1.1 no los sobrescribe porque todavía no existe un modelo explícito de vigencia e historial domiciliario.

## 4. Crear un pedido

1. Entrar en **Pedidos**.
2. Seleccionar **Nuevo pedido**.
3. Elegir cliente.
4. Elegir uno de sus domicilios.
5. Elegir un servicio vigente.
6. Informar una promoción solo cuando se conozca su código.
7. Agregar uno o más ítems.
8. Para cada ítem seleccionar la equivalencia e informar piezas físicas.
9. Revisar la vista previa:
   - piezas físicas;
   - grupos comerciales;
   - unidades equivalentes;
   - peso estimado;
   - necesidad de cotización;
   - ciclo exclusivo requerido.
10. Informar peso declarado cuando exista.
11. Programar retiro y promesa cuando correspondan.
12. Guardar con **Crear y cotizar pedido**.

### Ejemplo de equivalencias

| Código | Prenda | Conversión |
|---|---|---:|
| `TSHIRT` | Remera | 1 pieza = 1 unidad |
| `PANTS` | Pantalón | 1 pieza = 1 unidad |
| `SOCKS_3_PAIRS` | Tres pares de medias | 6 piezas = 1 unidad |
| `UNDERWEAR_3` | Tres prendas interiores | 3 piezas = 1 unidad |
| `HEAVY_SWEATSHIRT` | Buzo grueso | 1 pieza = 2 unidades |
| `BATH_SHEET` | Toallón | 1 pieza = 3 unidades |
| `COMFORTER` | Acolchado | presupuesto y ciclo separado |

La cantidad física nunca se reemplaza por la cantidad comercial.

## 5. Buscar pedidos

El listado permite:

- buscar por número completo o parcial;
- filtrar por estado;
- paginar resultados;
- consultar cliente, servicio, cantidades, importe, pago y retiro.

Seleccionar el número de pedido abre el detalle operativo.

## 6. Detalle del pedido

El detalle muestra:

- cliente y WhatsApp;
- servicio;
- piezas físicas y unidades equivalentes;
- peso declarado;
- ciclo exclusivo;
- límite alcanzado;
- retiro y promesa;
- precio cotizado y confirmado;
- desglose persistido;
- prendas, grupos, unidades y peso estimado;
- estado del pedido;
- estado de pago.

## 7. Confirmar el precio

El botón aparece cuando:

- el usuario posee rol `ADMIN` u `OPERATOR`;
- el precio todavía no fue confirmado;
- `requiresQuote` es falso.

Confirmar:

- congela el precio histórico;
- audita la operación;
- consume la promoción si corresponde;
- mueve un pedido `QUOTED` a `WAITING_CONFIRMATION`.

Un pedido con `requiresQuote=true` todavía necesita un mecanismo de ajuste manual no implementado. No debe forzarse su confirmación.

## 8. Cambiar el estado

El selector muestra únicamente las transiciones permitidas desde el estado actual. La UI no inventa estados posibles.

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

Existen bifurcaciones para cancelación, aprobación de precio, relavado y reclamo. La API devuelve solo las transiciones legales del pedido consultado.

Roles:

- `ADMIN`, `OPERATOR` y `DRIVER` pueden realizar las transiciones autorizadas por método;
- `REPORT_VIEWER` solo consulta.

## 9. Registrar pagos

Requisitos:

- precio confirmado;
- rol `ADMIN` u `OPERATOR`;
- importe positivo;
- importe no superior al saldo.

Métodos:

- `CASH`;
- `TRANSFER`;
- `MERCADO_PAGO`;
- `OTHER`.

El resultado informa total pagado, saldo y estado consolidado. Cuando el saldo llega a cero, el pedido queda `PAID`.

La pantalla todavía no lista el historial completo de pagos. Para conciliación real se debe implementar ese endpoint y su vista antes de operar caja formal.

## 10. Flujo recomendado de operación

1. Crear o localizar el cliente.
2. Revisar sus preferencias.
3. Crear el pedido con todas las piezas físicas.
4. Comparar la vista previa con el conteo recibido.
5. Crear y cotizar.
6. Abrir el detalle.
7. Revisar límites y precio.
8. Confirmar el precio cuando corresponda.
9. Registrar la aceptación mediante el cambio de estado.
10. Programar y registrar retiro.
11. Continuar estados operativos solo con evidencia real.
12. Registrar pagos.
13. Entregar y cerrar únicamente cuando las condiciones operativas se cumplan.

## 11. Qué no debe hacerse todavía

- No utilizar la aplicación para planificar ciclos compartidos: compatibilidad y máquinas no existen.
- No registrar `RECEIVED` como sustituto de un proceso real de recepción, peso y evidencias.
- No confirmar automáticamente pedidos con presupuesto manual.
- No usar Agenda o Dashboard como fuente contable u operativa completa.
- No interpretar el bloqueo local de login como protección distribuida.
- No usar el perfil `dev` en producción.
- No borrar volúmenes si existen datos que deban conservarse.

## 12. Swagger

Abrir `http://localhost:8081/api/swagger-ui.html`.

Uso técnico:

1. ejecutar `POST /api/auth/login`;
2. copiar `data.accessToken`;
3. pulsar **Authorize**;
4. ingresar `Bearer <token>`;
5. ejecutar los endpoints requeridos.

Cada respuesta expone `X-Request-ID`, útil para correlacionar errores con logs.

## 13. Reinicio del entorno

Sin borrar datos:

```powershell
docker compose down
docker compose up -d
```

Reinicio destructivo:

```powershell
docker compose down -v --remove-orphans
docker compose up --build -d
```

El segundo bloque elimina usuarios, clientes, pedidos, pagos y toda la base local.
