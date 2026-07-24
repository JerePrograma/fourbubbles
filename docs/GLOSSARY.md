# Glosario de dominio

## Pedido declarado

Datos previos al retiro. Entidad: `LaundryOrder`.

## Recepción

Snapshot físico único con peso, conteo, inspección y diferencias. Entidad: `OrderReception`.

## Perfil de tratamiento

Condiciones efectivas del pedido recibido. Entidad: `OrderTreatmentProfile`.

## `COMPAT-1`

Versión inmutable de las reglas actuales de compatibilidad.

## Compatibilidad efectiva

`compatible OR exception exists`. No garantiza capacidad ni disponibilidad.

## Máquina de producción

Equipo `WASHER` o `DRYER`, con capacidad, estado y vigencia. Entidad: `ProductionMachine`.

## Programa de producción

Configuración técnica `WASH` o `DRY`. Entidad: `ProductionProgram`.

## Ciclo

Asignación física planificada a una máquina/programa para uno o dos pedidos. Entidad: `ProductionCycle`.

## Ciclo activo

Ciclo `PLANNED` o `RUNNING`. Una máquina admite como máximo uno.

## `Idempotency-Key` de ciclo

Clave de 8–120 caracteres. Repetir la misma planificación devuelve el mismo ciclo; reutilizarla con otro payload produce conflicto.

## Peso planificado

Suma del peso real de recepción de los pedidos asignados.

## `separationRequired`

Marca persistida cuando la combinación fue habilitada por excepción. No representa bolsa, compartimento ni verificación física.

## `WAITING_DRY`

Estado entre lavado completado y secado iniciado.

## Control de calidad

Decisión posterior al tratamiento: `PASS` o `REWASH`.

## Programa usado

Programa referenciado por al menos un ciclo. Sus parámetros técnicos quedan protegidos por `V10`.

## Fuente de verdad

- integración: `origin/main`;
- esquema: Flyway;
- permisos: backend;
- reglas: servicios/policies;
- comportamiento crítico: pruebas.
