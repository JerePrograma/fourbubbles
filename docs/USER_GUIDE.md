# Guía de uso funcional

Esta guía describe el alcance realmente utilizable del corte actual. No presenta como disponible aquello que todavía pertenece a fases posteriores.

## 1. Alcance disponible hoy

Desde la interfaz web se puede:

- iniciar y cerrar sesión;
- renovar la sesión de forma transparente;
- consultar clientes;
- crear clientes con domicilio principal;
- navegar por el tablero, agenda y pedidos, aunque estas últimas vistas todavía son estructurales.

Desde Swagger o un cliente HTTP autenticado se puede además:

- consultar equivalencias vigentes;
- crear y cotizar pedidos;
- consultar un pedido;
- confirmar el precio;
- cambiar estados válidos;
- registrar pagos parciales o totales.

Todavía no están disponibles como flujo completo de interfaz:

- recepción y pesaje real;
- fotografías y daños preexistentes;
- edición de pedidos;
- planificación de ciclos;
- rutas y entregas;
- caja completa;
- costos y rentabilidad;
- inventario, reclamos y abonos.

## 2. Inicio de sesión

1. Abrir `http://localhost:8080`.
2. Ingresar el usuario configurado en `APP_DEV_ADMIN_USERNAME`.
3. Ingresar la contraseña configurada en `APP_DEV_ADMIN_PASSWORD`.
4. La interfaz conserva el access token solo en memoria.
5. La renovación usa una cookie `HttpOnly`; no es necesario copiar tokens manualmente desde la UI.

## 3. Crear un cliente desde la interfaz

1. Ingresar en **Clientes**.
2. Elegir **Nuevo cliente**.
3. Completar nombre, apellido, teléfono y WhatsApp.
4. Completar el domicilio.
5. Seleccionar una zona habilitada:
   - Marcos Paz;
   - Mariano Acosta.
6. Guardar.
7. Verificar que el cliente aparezca en el listado.

Reglas relevantes:

- WhatsApp debe ser único entre clientes activos.
- Debe existir al menos un domicilio.
- Solo puede existir un domicilio principal activo por cliente.
- El código de zona debe corresponder a una zona activa.

## 4. Recorrido operativo mediante Swagger

Abrir `http://localhost:8081/api/swagger-ui.html`.

### 4.1 Autenticarse

Ejecutar `POST /api/auth/login`:

```json
{
  "username": "admin",
  "password": "LA_CONTRASENA_CONFIGURADA"
}
```

La respuesta incluye `data.accessToken`. Copiar únicamente el token, pulsar **Authorize** y cargar:

```text
Bearer EL_ACCESS_TOKEN
```

La cookie de refresh queda administrada por el navegador.

### 4.2 Consultar equivalencias

Ejecutar `GET /api/catalog/equivalences`.

Códigos útiles para pruebas:

| Código | Significado | Agrupación |
|---|---|---:|
| `TSHIRT` | Remera | 1 pieza = 1 unidad |
| `SHIRT` | Camisa o blusa | 1 pieza = 1 unidad |
| `PANTS` | Pantalón o jean | 1 pieza = 1 unidad |
| `SOCKS_3_PAIRS` | Tres pares de medias | 6 piezas = 1 unidad |
| `UNDERWEAR_3` | Tres prendas interiores | 3 piezas = 1 unidad |
| `HEAVY_SWEATSHIRT` | Buzo grueso | 1 pieza = 2 unidades |
| `BATH_SHEET` | Toallón | 1 pieza = 3 unidades |
| `COMFORTER` | Acolchado | requiere presupuesto y servicio separado |

La cantidad física nunca se reemplaza por la cantidad comercial. Ambas quedan registradas.

### 4.3 Crear un cliente por API

Ejecutar `POST /api/clients`:

```json
{
  "firstName": "Ana",
  "lastName": "Pérez",
  "phone": "1123456789",
  "whatsapp": "1123456789",
  "email": "ana@example.com",
  "acquisitionSource": "WhatsApp",
  "preferencesJson": "{\"fragrance\":\"suave\",\"dryerAllowed\":true}",
  "notes": "Cliente de prueba",
  "addresses": [
    {
      "zoneCode": "MARCOS_PAZ",
      "street": "Sarmiento",
      "number": "123",
      "extra": null,
      "locality": "Marcos Paz",
      "neighborhood": "Centro",
      "references": "Portón negro",
      "primaryAddress": true
    }
  ]
}
```

Guardar de la respuesta:

- `data.id`: identificador del cliente;
- `data.addresses[0].id`: identificador del domicilio.

### 4.4 Crear y cotizar un pedido

Ejecutar `POST /api/orders` usando los UUID anteriores:

```json
{
  "clientId": "UUID_CLIENTE",
  "addressId": "UUID_DOMICILIO",
  "serviceCode": "ROPA_LISTA_12",
  "promotionCode": "FIRST_TRIAL",
  "declaredWeightGrams": 2200,
  "exclusiveCycle": false,
  "pickupScheduledAt": "2026-07-21T10:00:00-03:00",
  "promisedAt": "2026-07-22T18:00:00-03:00",
  "notes": "Separar prendas oscuras",
  "items": [
    {
      "equivalenceCode": "TSHIRT",
      "physicalPieces": 4,
      "observations": null
    },
    {
      "equivalenceCode": "PANTS",
      "physicalPieces": 2,
      "observations": null
    },
    {
      "equivalenceCode": "SOCKS_3_PAIRS",
      "physicalPieces": 12,
      "observations": "Doce piezas físicas equivalen a seis pares"
    }
  ]
}
```

Resultado esperado del ejemplo:

- piezas físicas: 18;
- remeras: 4 unidades equivalentes;
- pantalones: 2 unidades equivalentes;
- 12 medias físicas: 2 grupos comerciales;
- total: 8 unidades equivalentes;
- peso declarado: 2.200 gramos;
- servicio dentro de los límites de 12 unidades y 2.500 gramos;
- promoción `FIRST_TRIAL` aplicable solo si es primera compra y no fue usada en ese domicilio.

Guardar `data.id` y `data.orderNumber`.

### 4.5 Revisar la explicación del precio

Ejecutar `GET /api/orders/{id}`.

Revisar:

- `quotedPrice`;
- `confirmedPrice`;
- `promotionId` o promoción informada;
- `priceBreakdown`;
- `limitReached`;
- `requiresQuote`.

Un pedido que requiere presupuesto no debe tratarse como precio definitivo.

### 4.6 Confirmar el precio

Ejecutar `POST /api/orders/{id}/confirm-price`.

Al confirmar:

- se congela el precio histórico;
- se registra auditoría;
- la promoción se consume en ese momento, no durante una cotización abandonada.

### 4.7 Cambiar el estado

Ejecutar `PATCH /api/orders/{id}/status`:

```json
{
  "newStatus": "WAITING_CONFIRMATION",
  "observation": "Cotización enviada por WhatsApp",
  "location": "Administración",
  "notificationReference": "wa-20260720-001"
}
```

Solo se aceptan transiciones definidas por la política. No se puede saltar arbitrariamente desde consulta hasta entregado.

Recorrido orientativo del núcleo actual:

1. `INQUIRY`;
2. `QUOTED`;
3. `WAITING_CONFIRMATION`;
4. `RESERVED`;
5. `PICKUP_SCHEDULED`;
6. `PICKED_UP`;
7. `RECEIVED`;
8. estados operativos posteriores;
9. `DELIVERED`;
10. `CLOSED`.

La lista exacta y las transiciones permitidas están en el enum y la política de estados. Swagger devuelve un error de dominio ante una transición inválida.

### 4.8 Registrar un pago

Ejecutar `POST /api/payments`:

```json
{
  "orderId": "UUID_PEDIDO",
  "methodCode": "TRANSFER",
  "amount": 3000.00,
  "paidAt": "2026-07-20T16:30:00-03:00",
  "reference": "Transferencia 123456",
  "notes": "Pago parcial"
}
```

La respuesta informa:

- total pagado;
- saldo restante;
- estado de pago del pedido.

No se permite:

- pagar antes de confirmar el precio;
- registrar un importe cero o negativo;
- superar el saldo pendiente.

Métodos iniciales configurados:

- efectivo;
- transferencia;
- Mercado Pago;
- otro.

Consultar los códigos efectivos en la base o en Swagger antes de automatizar clientes externos.

## 5. Flujo de uso recomendado para el corte actual

1. Iniciar sesión.
2. Crear cliente y domicilio desde la interfaz.
3. Consultar equivalencias desde Swagger.
4. Crear y cotizar el pedido por API.
5. Revisar unidades equivalentes, peso, límites y desglose.
6. Confirmar el precio.
7. Registrar cambios de estado conforme avanza el pedido.
8. Registrar uno o más pagos.
9. Consultar el pedido para verificar saldo y trazabilidad.
10. Cerrar el pedido solo cuando la transición y el pago correspondan.

## 6. Qué no debe hacerse todavía

- No utilizar la aplicación como planificación real de ciclos: compatibilidad y máquinas no están implementadas.
- No asumir que una bolsa de red evita compartir agua.
- No usar agenda o tablero como fuente contable: todavía no poseen métricas reales.
- No usar promociones complejas que indiquen validación manual como automáticas.
- No almacenar evidencias fotográficas fuera de un procedimiento controlado hasta implementar almacenamiento externo.
- No utilizar el perfil `dev` en producción.

## 7. Datos de prueba y reinicio

Los datos comerciales iniciales se crean por Flyway. Los clientes y pedidos de prueba no se recrean automáticamente.

Para reiniciar completamente el entorno de desarrollo:

```powershell
docker compose down -v --remove-orphans
docker compose up --build -d
```

Esto borra todos los datos locales.
