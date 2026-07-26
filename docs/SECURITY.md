# Seguridad

Versión funcional: `0.4.2`.

## Sesiones

BCrypt, access JWT corto, refresh opaco/rotativo/revocable, cookie `HttpOnly` y access token solo en memoria.

## Jerarquía

```text
ADMIN > OPERATOR > DRIVER > REPORT_VIEWER
```

## Matriz productiva

| Operación | Permiso mínimo |
|---|---|
| configurar máquina/programa | `ADMIN` |
| operar ciclo/calidad | `OPERATOR` |
| confirmar separación | `OPERATOR` |
| consultar producción/separación/métricas | `REPORT_VIEWER` |
| excepción de compatibilidad | `ADMIN` |
| auditoría | `ADMIN` |

`DRIVER` no registra recepción, modifica ciclos ni confirma separación.

## Autoridad del backend

- Bean Validation valida códigos;
- Spring Security valida rol;
- servicios validan ciclo, asignación, unicidad e idempotencia;
- dominio impide iniciar con separaciones pendientes;
- PostgreSQL aplica constraints e índice único;
- UI no es control de seguridad.

## Separación

- solo sobre asignaciones `separationRequired=true`;
- solo antes de iniciar;
- contenedor distinto por pedido dentro del ciclo;
- actor y fecha persistidos;
- evento de auditoría por confirmación;
- lectura para cuatro roles, escritura para `ADMIN`/`OPERATOR`.

La confirmación prueba una acción autenticada, no la permanencia física de la separación. No inventar códigos ni usar este control como sustituto de procedimientos operativos reales.

## Métricas

Endpoint de solo lectura para cuatro roles. El servicio valida rango y no acepta escrituras ni parámetros de consulta mayores a 366 días.

## Riesgos abiertos

MFA, rate limit distribuido, WAF/topología productiva, secretos administrados, backup/restore, privacidad/retención, DAST, carga y evidencia física automatizada.
