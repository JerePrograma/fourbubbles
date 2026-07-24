# Configuración

Fuentes: `.env.example`, `docker-compose.yml`, `backend/src/main/resources/application*.yml`, `frontend/src/api/httpClient.ts` y scripts de `scripts/`.

No copiar valores reales de `.env` a documentación, issues, logs ni commits.

## Variables del stack local

| Variable | Obligatoria | Predeterminado seguro/documentado | Consumidor | Ausencia o valor inválido |
|---|---|---|---|---|
| `COMPOSE_PROJECT_NAME` | recomendada | `fourbubbles` | Compose/scripts | riesgo de colisión; el script la agrega |
| `POSTGRES_HOST_PORT` | no | `5432` | Compose/scripts | usa default; fuera de rango o duplicado se rechaza |
| `BACKEND_HOST_PORT` | no | `8081` | Compose/scripts | usa default; fuera de rango o duplicado se rechaza |
| `FRONTEND_HOST_PORT` | no | `8080` | Compose/scripts | usa default; fuera de rango o duplicado se rechaza |
| `POSTGRES_DB` | sí para scripts | `ropalista` | PostgreSQL/backend | `Start-Local.ps1` la agrega |
| `POSTGRES_USER` | sí para scripts | `ropalista` | PostgreSQL/backend | `Start-Local.ps1` la agrega |
| `POSTGRES_PASSWORD` | sí | sin valor válido en repositorio | PostgreSQL/backend | Compose y script fallan |
| `DB_HOST` | no | `localhost` en Spring; `postgres` en Compose | datasource | cambia según ejecución |
| `DB_PORT` | no | `5432` | datasource | usa default |
| `DB_NAME` | no | `ropalista` | datasource | usa default |
| `DB_USER` | no | `ropalista` | datasource | usa default |
| `DB_PASSWORD` | sí fuera de Compose local | placeholder en ejemplo | datasource | conexión falla |
| `JWT_SECRET_BASE64` | sí | ninguno | JWT | debe ser Base64 y representar al menos 32 bytes |
| `JWT_ACCESS_TTL` | no | `PT15M` | seguridad | usa default ISO-8601 |
| `REFRESH_TOKEN_TTL` | no | `P30D` | seguridad | usa default ISO-8601 |
| `LOGIN_MAX_ATTEMPTS` | no | `5` en plantilla | throttling | usa configuración de aplicación |
| `LOGIN_ATTEMPT_WINDOW` | no | `PT15M` | throttling | usa configuración de aplicación |
| `LOGIN_BLOCK_DURATION` | no | `PT15M` | throttling | usa configuración de aplicación |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:5173,http://localhost:8080` | Spring Security | restringe orígenes |
| `APP_TIME_ZONE` | no | `America/Argentina/Buenos_Aires` | Jackson/aplicación | usa default |
| `APP_DEV_ADMIN_USERNAME` | no en `dev` | `admin` | inicializador `dev` | usa default |
| `APP_DEV_ADMIN_PASSWORD` | sí en `dev` | ninguno válido | inicializador `dev` | arranque `dev` falla |
| `VITE_API_BASE_URL` | no | `/api` | frontend | proxy relativo |
| `SERVER_PORT` | no | `8080` | backend | usa default |
| `SECURE_COOKIE` | no | `false`; `prod` fuerza `true` | cookie refresh | depende del perfil |

`Start-Local.ps1` genera secretos aleatorios solo al crear `.env`. Si `.env` existe, agrega claves faltantes y no reemplaza valores.

## Perfiles Spring

### `dev`

- perfil predeterminado;
- crea/reconcilia administrador inicial desde variables;
- logging `DEBUG` para `ar.com.ropalista`;
- usado por Compose.

Cambiar `APP_DEV_ADMIN_PASSWORD` después de persistir el usuario no cambia su hash. Se debe restaurar la contraseña original o recrear explícitamente el volumen local.

### `test`

- activado por las integraciones;
- conserva Flyway y `ddl-auto=validate`;
- usa secreto JWT exclusivo de tests;
- Testcontainers suministra PostgreSQL.

### `prod`

- `secure-cookie=true`;
- respeta cabeceras reenviadas;
- logging JSON;
- no convierte Compose en despliegue productivo.

## Configuración persistente

Flyway define esquema y seeds. Hibernate únicamente valida. La configuración comercial versionada vive en base: servicios, equivalencias, precios y promociones.

## Puertos y redes

Puertos internos invariantes:

```text
postgres:5432
backend:8080
frontend:80
```

Los puertos host son configurables. La comunicación entre contenedores nunca debe usar los puertos publicados.

## Ejemplo seguro

```dotenv
POSTGRES_PASSWORD=<DB_PASSWORD>
DB_PASSWORD=<DB_PASSWORD>
JWT_SECRET_BASE64=<JWT_SECRET_BASE64>
APP_DEV_ADMIN_PASSWORD=<DEV_ADMIN_PASSWORD>
```

## Producción

`NO VERIFICADO`: no existe integración con un gestor de secretos, rotación automatizada, configuración de dominio/TLS, backup/restore ni rollback productivo. Consultar [`OPERATIONS.md`](OPERATIONS.md) y [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md).
