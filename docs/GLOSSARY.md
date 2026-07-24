# Glosario de dominio

## Four Bubbles / Ropa Lista

Nombres encontrados para el mismo producto. `Ropa Lista` aparece en paquetes, artefactos y UI; `Four Bubbles` identifica el repositorio/proyecto.

## Pedido declarado

Información previa al retiro: cliente, domicilio, servicio, prendas, piezas físicas, unidades equivalentes, peso estimado, exclusividad y cotización. Entidad: `LaundryOrder`.

## Pieza física

Unidad real contable de prenda. No es equivalente a grupo ni a unidad tarifaria.

## Grupo

Agrupación comercial definida por `physicalUnitsPerGroup`; por ejemplo, varias piezas pueden formar un grupo tarifario.

## Unidad equivalente

Magnitud decimal usada para límites y precio. Se calcula desde equivalencias versionadas y no reemplaza el conteo físico.

## Equivalencia de prenda

Regla versionada que relaciona código, piezas por grupo, unidades equivalentes, peso estimado y restricciones. Entidad: `GarmentEquivalence`.

## Snapshot

Copia histórica de datos relevantes en el momento de una operación. Evita reinterpretar pedidos, recepción o evaluaciones con configuración futura.

## Recepción

Registro físico único del pedido desde `PICKED_UP`. Incluye conteo/peso reales, inspección, etiqueta, bolsa, diferencias y metadata de evidencia. Entidad: `OrderReception`.

## `Idempotency-Key`

Cabecera obligatoria de recepción. Repetir la misma clave para el mismo pedido devuelve el mismo agregado; reutilizarla para otro pedido produce conflicto.

## Diferencia material

En recepción, diferencia de piezas, daño o diferencia de peso mayor a 250 g o 10 % declarado. Puede requerir decisión.

## `CLASSIFIED`

Estado en el que la recepción fue resuelta y el pedido puede obtener perfil de tratamiento. No significa que exista ciclo físico.

## Perfil de tratamiento efectivo

Condiciones del pedido recibido después de combinar request operativo, preferencias del cliente y exclusividad del pedido. Entidad: `OrderTreatmentProfile`.

## `COMPAT-1`

Versión inmutable de las reglas actuales de `CompatibilityEngine`.

## Razón `HARD`

Incompatibilidad que bloquea el tratamiento compartido según la versión de reglas.

## Razón `WARNING`

Diferencia resoluble aplicando la opción más restrictiva; no bloquea por sí sola.

## Evaluación de compatibilidad

Snapshot entre dos pedidos ordenados canónicamente, identificado por versiones de perfiles y reglas. Entidad: `CompatibilityEvaluation`.

## Excepción de compatibilidad

Autorización `ADMIN` sobre una evaluación originalmente incompatible. Conserva resultado, razones y recomendación. Entidad: `CompatibilityException`.

## Compatibilidad efectiva

Resultado derivado:

```text
compatible OR exception exists
```

No implica capacidad, máquina ni asignación de ciclo.

## Ciclo exclusivo

Restricción que impide compartir tratamiento. Puede provenir del pedido, preferencias o perfil y no puede relajarse desde la UI.

## Etiqueta de recepción

Código legible generado desde `reception_label_seq`, con prefijo `RCV-`, para identificar el snapshot físico.

## Bolsa

Código operativo opcional asociado a la recepción; no modela todavía separación física dentro de un ciclo.

## Precio automático, cotizado y confirmado

Valores distintos del pedido. El confirmado y su desglose deben permanecer históricos.

## Fuente de verdad

Para código e integración: `origin/main`. Para esquema: Flyway. Para permisos: anotaciones/control de seguridad backend. Para comportamiento crítico: servicio de aplicación y pruebas.
