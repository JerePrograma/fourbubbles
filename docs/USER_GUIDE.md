# Guía de uso funcional

Versión: `0.4.4`.

## 1. Preparar el pedido

1. Crear cliente, domicilio y pedido.
2. Confirmar precio y avanzar hasta `PICKED_UP`.
3. Registrar recepción y resolver diferencias hasta `CLASSIFIED`.
4. Guardar perfil de tratamiento.

## 2. Compatibilidad

Para compartir dos pedidos, evaluar compatibilidad. Una excepción solo puede autorizarla `ADMIN` y no borra razones ni exclusividad.

## 3. Planificar producción

Como `ADMIN` u `OPERATOR`:

1. abrir **Producción**;
2. elegir máquina y programa;
3. seleccionar uno o dos pedidos;
4. planificar el ciclo.

## 4. Confirmar separación

Cuando el ciclo muestre `separationRequired=true`:

1. abrir **Separación**;
2. localizar el ciclo `PLANNED`;
3. asignar un código físico distinto a cada pedido, por ejemplo `BAG-001` y `BAG-002`;
4. confirmar cada asignación;
5. verificar que el ciclo figure `LISTO`.

El ciclo no podrá iniciarse mientras exista una confirmación pendiente. Repetir el mismo código sobre el mismo pedido es seguro; reutilizarlo en otro pedido del ciclo devuelve conflicto.

No confirmar códigos ficticios. La operación queda auditada con usuario y fecha.

## 5. Consultar métricas

Abrir **Métricas** para revisar los últimos 30 días: ciclos, finalización, duración media, pedidos, pesos y separación. Usar **Actualizar** para recalcular. No interpretar como costo, margen o utilización histórica.

## 6. Ejecutar ciclo

- `PLANNED`: iniciar o cancelar;
- `RUNNING`: completar;
- `COMPLETED`/`CANCELLED`: finales.

El lavado lleva a `WAITING_DRY` o `QUALITY_CONTROL`. El secado lleva a `QUALITY_CONTROL`.

## 7. Calidad

- `PASS → FOLDING`;
- `REWASH → REWASH_REQUIRED`.

La observación es obligatoria.

## 8. Roles

- `ADMIN`: configuración, operación, separación, excepción y auditoría;
- `OPERATOR`: operación y separación;
- `DRIVER`: consulta;
- `REPORT_VIEWER`: consulta.

## 9. Catálogo

Los listados de servicios y equivalencias mantienen el mismo comportamiento visible. El cambio 0.4.3 es interno y no exige acciones nuevas.

## 10. Configuración productiva

Como `ADMIN`, abrir **Configuración**:

1. seleccionar una máquina o programa existente;
2. editar únicamente los campos habilitados;
3. guardar y revisar el mensaje del backend.

Código/tipo de máquina y código/etapa de programa son inmutables. Una máquina con ciclo activo no se modifica. Los parámetros técnicos de un programa utilizado están protegidos por la base.

## 11. Límites

La confirmación es una declaración operativa auditada. No hay sensor, imagen obligatoria, optimizador, costos, rutas ni almacenamiento binario de fotos.
