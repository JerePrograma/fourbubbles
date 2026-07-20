# Arquitectura

## Decisión principal

Monolito modular. Cada módulo contiene sus capas de dominio, aplicación, persistencia, infraestructura y API cuando corresponden.

## Módulos implementados

```text
auth        identidad, roles y sesiones
audit       eventos sensibles
catalog     servicios y equivalencias
customer    clientes y domicilios
location    zonas
pricing     precios y promociones
order       pedido, ítems, límites y estados
payment     medios y pagos
common      API, excepciones y base auditable
config      infraestructura transversal
```

## Dependencias permitidas

- API depende de aplicación y DTO.
- Aplicación orquesta dominios y repositorios.
- Dominio no depende de controladores.
- Persistencia implementa acceso a datos del módulo.
- Módulos pueden consultar catálogos compartidos, pero no deben generar ciclos bidireccionales de lógica.

## Transacciones

Las operaciones que crean pedido, ítems, historial, uso promocional y auditoría se ejecutan en una transacción. Lo mismo aplica al registro de pago y actualización de saldo.

## Persistencia

- UUID para identificadores internos.
- Secuencia separada para número humano del pedido.
- `NUMERIC(15,2)` para dinero.
- gramos enteros para peso.
- `TIMESTAMPTZ` para eventos.
- `DATE` para vigencias puramente comerciales por día.
- eliminación lógica donde existe historial operativo.
- restricciones e índices creados por Flyway.

## Integraciones futuras

Las fotografías deben almacenarse en un servicio de objetos o filesystem administrado; la base guardará metadatos y referencias. WhatsApp comienza con plantillas/enlaces, no con una dependencia obligatoria de API externa.
