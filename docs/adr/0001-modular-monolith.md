# ADR 0001 — Monolito modular

## Estado

Aceptado.

## Contexto

El negocio comienza con una persona, una instalación física y baja carga transaccional. El dominio sí posee reglas complejas, pero no existen necesidades de escalado independiente ni equipos autónomos por módulo.

## Decisión

Implementar un único backend Spring Boot organizado por módulos funcionales. Cada módulo separa `domain`, `application`, `infrastructure` y `api` cuando la complejidad lo justifica.

## Consecuencias

- despliegue y transacciones simples;
- menor costo operativo;
- límites de módulo explícitos;
- posibilidad futura de extraer módulos solamente si aparecen métricas y razones reales.
