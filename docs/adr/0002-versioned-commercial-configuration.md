# ADR 0002: configuración comercial versionada

## Estado

Aceptado.

## Decisión

Servicios, equivalencias, precios y promociones viven en base de datos con vigencia. El pedido referencia la definición usada y guarda importes/desglose.

## Motivo

Cambiar una tarifa no puede reescribir pedidos históricos ni volver inexplicable un cobro.

## Consecuencia

Las ediciones futuras crean nuevas versiones o cierran vigencias; no sobrescriben valores aplicados.
