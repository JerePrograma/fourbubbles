# Seguridad

Versión funcional: `0.4.0`.

## Sesiones

- BCrypt;
- access JWT HS256 de corta duración;
- refresh opaco, hasheado, rotativo y revocable;
- cookie `HttpOnly`, `SameSite=Strict`, `Secure` en `prod`;
- access token solo en memoria;
- throttling local de login.

## Jerarquía

```text
ADMIN > OPERATOR > DRIVER > REPORT_VIEWER
```

La jerarquía no elimina precondiciones de dominio.

## Matriz efectiva

| Operación | Permiso mínimo |
|---|---|
| clientes/pedidos/pagos | `OPERATOR` según endpoint |
| cotización manual | `ADMIN` |
| registrar/decidir recepción | `OPERATOR` |
| consultar recepción | `REPORT_VIEWER` |
| guardar perfil/evaluar | `OPERATOR` |
| autorizar excepción | `ADMIN` |
| crear/editar máquina/programa | `ADMIN` |
| consultar producción | `REPORT_VIEWER` |
| planificar/iniciar/completar/cancelar ciclo | `OPERATOR` |
| control de calidad | `OPERATOR` |
| auditoría | `ADMIN` |

`DRIVER` no registra recepción ni modifica producción en el código vigente.

## Autoridad del backend

- Bean Validation valida DTO;
- Spring Security valida rol;
- servicios validan estados, perfiles, capacidad e idempotencia;
- PostgreSQL aplica constraints/triggers;
- UI no es control de seguridad.

## Producción y concurrencia

- advisory lock por clave de ciclo;
- bloqueo pesimista de máquina, programa, pedidos y ciclo;
- pedidos ordenados por UUID;
- índice parcial para máquina activa;
- clave idempotente única;
- programa usado protegido por trigger;
- excepción no cambia compatibilidad original.

## Riesgo de separación

`separationRequired=true` solo conserva una advertencia. No existen bolsas/compartimentos identificados ni confirmación de separación. No usar cargas exceptuadas con riesgo real hasta implementar ese control.

## Secretos y datos

No versionar `.env`, credenciales, tokens ni datos personales. Evidencias futuras requieren object storage privado, autorización y retención.

## Riesgos abiertos

- MFA y gestión completa de usuarios;
- rate limit distribuido;
- WAF/topología productiva;
- secretos administrados;
- backup/restore;
- privacidad/retención;
- DAST y carga;
- separación física rastreable.
