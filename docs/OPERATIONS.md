# Operación y despliegue

## Perfiles

- `dev`: crea administrador desde variables y habilita logs de aplicación en DEBUG.
- `test`: usado por Testcontainers y validación de migraciones.
- `prod`: cookies seguras, logs estructurados básicos y sin SQL.

## Salud

- liveness/readiness mediante Actuator;
- Compose espera PostgreSQL antes de iniciar backend;
- frontend espera readiness del backend.

## Base de datos

Flyway es la única vía admitida para cambios de esquema. `ddl-auto=validate` detecta divergencias sin modificar producción.

## Backups

Pendiente automatizar. Procedimiento mínimo esperado:

```bash
pg_dump --format=custom --file=ropalista.dump ropalista
pg_restore --clean --if-exists --dbname=ropalista_restore_test ropalista.dump
```

No se considera un backup válido hasta comprobar restauración.

## Despliegue

El Compose actual sirve desarrollo y validación. Producción requiere:

- secretos administrados fuera del archivo `.env`;
- TLS;
- volúmenes y backups;
- límites de recursos;
- observabilidad;
- despliegue sin perfil `dev`;
- migración previa controlada.
