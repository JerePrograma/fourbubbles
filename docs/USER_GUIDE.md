# Guía de uso funcional

Versión: `0.2.0`.

## 1. Iniciar sesión

Abrir `http://localhost:8080` y usar las credenciales generadas por `Start-Local.ps1`.

## 2. Roles

| Rol | Alcance |
|---|---|
| `ADMIN` | administración, cotización manual y auditoría |
| `OPERATOR` | clientes, pedidos, recepción, decisiones y pagos |
| `DRIVER` | lectura y transiciones operativas |
| `REPORT_VIEWER` | consulta |

## 3. Crear cliente

1. **Clientes → Nuevo cliente**.
2. Completar datos y WhatsApp.
3. Configurar preferencias.
4. Cargar al menos un domicilio.
5. Marcar exactamente uno principal.
6. Guardar.

## 4. Domicilios

Desde **Editar cliente**:

- agregar alternativo;
- hacer principal;
- desactivar no principal;
- consultar historial.

La baja no borra pedidos anteriores.

## 5. Crear pedido

1. **Pedidos → Nuevo pedido**.
2. Elegir cliente/domicilio/servicio.
3. Agregar prendas y cantidades declaradas.
4. Informar promoción, peso declarado, retiro y notas cuando corresponda.
5. Revisar piezas, grupos, unidades y peso estimado.
6. Crear/cotizar.

## 6. Precio

El detalle diferencia:

- automático original;
- cotizado vigente;
- confirmado.

`ADMIN` puede aplicar cotización manual con motivo antes de confirmar.

## 7. Confirmar y preparar retiro

1. Confirmar precio.
2. Avanzar estados permitidos:

```text
WAITING_CONFIRMATION → RESERVED → PICKUP_SCHEDULED → PICKED_UP
```

La recepción solo se habilita desde `PICKED_UP`.

## 8. Registrar recepción

Desde **Pedidos**, elegir **Recibir** en la fila del pedido.

### Datos generales

- peso real obligatorio;
- fecha/hora opcional (por defecto ahora);
- código de bolsa opcional;
- condición general.

### Por prenda

Para cada código declarado:

- cantidad real;
- daño;
- mancha;
- observaciones.

Todos los códigos declarados deben permanecer en el formulario, incluso si la cantidad real es cero.

### Evidencia externa opcional

La sección acepta metadata:

- clave de objeto;
- nombre;
- MIME;
- tamaño;
- SHA-256;
- descripción.

No carga el archivo. Dejar vacía si no existe un almacenamiento externo real.

### Registrar

Seleccionar **Registrar recepción**.

El navegador envía una clave idempotente. Si la operación se repite con la misma clave, el backend devuelve la misma recepción.

## 9. Resultado de recepción

### Sin diferencia material

El pedido queda `CLASSIFIED`.

Se consideran tolerables:

- mismo conteo;
- sin daño;
- diferencia de peso de hasta 250 g y 10 %.

### Con diferencia material

El pedido queda `WAITING_PRICE_APPROVAL` cuando:

- faltan/sobran piezas;
- existe daño;
- el peso supera el umbral.

Una mancha se registra, pero no obliga por sí sola a decisión.

## 10. Aprobar o rechazar

`ADMIN` u `OPERATOR`:

1. revisar diferencias y evidencias;
2. escribir notas;
3. elegir **Aprobar diferencias** o **Rechazar**.

Resultado:

- aprobado → `CLASSIFIED`;
- rechazado → `CANCELLED`.

La decisión registra actor, fecha y notas. No equivale a firma electrónica del cliente.

## 11. Consultar recepción

La misma pantalla muestra:

- etiqueta;
- estado;
- peso/conteo declarado y real;
- diferencias;
- daño/mancha;
- bolsa;
- composición real;
- decisión;
- evidencia metadata.

Los roles de lectura pueden consultarla.

## 12. Continuar estados

Después de clasificar, el backend permite estados posteriores según la política. Sin embargo, compatibilidad y ciclos todavía no están implementados: no interpretar `WASHING` como un registro real de máquina/ciclo.

## 13. Registrar pagos

Con precio confirmado:

1. elegir medio;
2. ingresar importe;
3. agregar referencia;
4. registrar.

El sistema impide sobrepago, incluso concurrente.

## 14. Auditoría

Solo `ADMIN`.

Filtrar por entidad, ID o acción. Recepción genera eventos `ORDER_RECEPTION` con acciones `CREATE` y `DECIDE`.

## 15. Demostración completa

1. Crear cliente.
2. Administrar domicilios.
3. Crear pedido con 2 remeras y 2.500 g declarados.
4. Confirmar precio.
5. Llevarlo a `PICKED_UP`.
6. Registrar 2 remeras y 2.650 g: clasifica sin aprobación.
7. En otro pedido, registrar 1 remera, 3.100 g y daño: queda pendiente.
8. Aprobar la diferencia.
9. Consultar etiqueta, composición real y auditoría.
10. Registrar pago parcial y total.

## 16. Errores frecuentes

### “La recepción solo puede registrarse desde PICKED_UP”

Avanzar el pedido por las transiciones permitidas.

### “Pedido ya recibido”

Abrir **Ver recepción**. No generar una segunda recepción.

### “Clave de idempotencia en conflicto”

La clave fue usada en otro pedido. Crear una nueva operación desde la UI correcta; no reutilizar claves entre pedidos.

### Faltan prendas declaradas

No eliminar filas del formulario. Usar cantidad real cero.

### Evidencia inválida

Completar nombre, tamaño y SHA-256 hexadecimal de 64 caracteres, o dejar toda la evidencia vacía.

## 17. Límites que deben comunicarse

- metadata no significa archivo cargado;
- aprobación no es firma digital;
- no existen compatibilidad/ciclos/rutas/caja completos;
- no inferir costos o rentabilidad desde cobros;
- no usar estados como sustituto de producción real.
