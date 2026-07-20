# ADR 0001: monolito modular

## Estado

Aceptado.

## Decisión

Implementar un único despliegue con módulos funcionales internos.

## Motivo

El volumen inicial no justifica microservicios. La separación modular conserva límites de dominio sin introducir red, consistencia distribuida ni operación innecesaria.

## Consecuencia

Los módulos deben evitar dependencias cíclicas de negocio. Una eventual extracción solo se evaluará con evidencia de carga, autonomía o despliegue independiente.
