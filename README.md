# Four Bubbles / Ropa Lista

Sistema de gestión integral para una lavandería doméstica con retiro y entrega en Marcos Paz y Mariano Acosta.

> Versión: **0.1.1**. La plataforma base y el flujo operativo inicial de Fase 1 están implementados. El MVP completo de recepción, producción, logística y finanzas todavía no está terminado.

## Alcance disponible

- Monolito modular Java 21 + Spring Boot 3.
- PostgreSQL 16 y migraciones Flyway desde la primera tabla.
- Autenticación JWT de corta duración y refresh token opaco rotativo en cookie `HttpOnly`.
- Roles `ADMIN`, `OPERATOR`, `DRIVER` y `REPORT_VIEWER`.
- Respuestas JSON uniformes, incluyendo 401 y 403 generados por Spring Security.
- Protección básica de login por usuario y origen.
- `X-Request-ID` y correlación de logs.
- Auditoría persistente de operaciones sensibles.
- Clientes, domicilios, zonas y preferencias operativas tipadas.
- Alta, búsqueda y actualización de clientes desde la interfaz.
- Catálogo versionado de servicios, equivalencias, precios y promociones.
- Pedidos con piezas físicas, grupos, unidades equivalentes, peso, precio histórico y estados trazables.
- Alta guiada, búsqueda, filtros, detalle y operación de pedidos desde la interfaz.
- Confirmación de precio y transiciones válidas desde el detalle del pedido.
- Pagos parciales o totales con saldo y estado de pago.
- Docker Compose, Nginx y GitHub Actions.
- Pruebas unitarias, MockMvc, Vitest e integración con PostgreSQL mediante Testcontainers.
- Dependencias frontend bloqueadas mediante `package-lock.json` y `npm ci`.
- Scripts PowerShell para iniciar y verificar el entorno local.
- Documentación funcional, técnica, operativa, de seguridad, pruebas y backlog.

## Requisitos recomendados

Camino simple:

- Git;
- Docker Desktop o Docker Engine;
- Docker Compose v2;
- PowerShell 7 en Windows.

Ejecución sin Docker:

- Java 21;
- Maven 3.9 o superior;
- Node.js 22;
- PostgreSQL 16.

## Inicio rápido en Windows

```powershell
git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

`Start-Local.ps1` crea `.env` solo si no existe, genera secretos aleatorios, construye los contenedores y espera a que el backend quede saludable. La contraseña administrativa generada se muestra una sola vez.

La guía manual completa está en [docs/WINDOWS_SETUP.md](docs/WINDOWS_SETUP.md).

## Inicio manual con Docker

```powershell
Copy-Item -LiteralPath '.env.example' -Destination '.env'
```

Reemplazar obligatoriamente:

- `POSTGRES_PASSWORD` y `DB_PASSWORD` con el mismo valor;
- `JWT_SECRET_BASE64` con al menos 32 bytes aleatorios codificados en Base64;
- `APP_DEV_ADMIN_PASSWORD` con una contraseña propia.

Parámetros opcionales del bloqueo de login:

- `LOGIN_MAX_ATTEMPTS`, valor inicial `5`;
- `LOGIN_ATTEMPT_WINDOW`, valor inicial `PT15M`;
- `LOGIN_BLOCK_DURATION`, valor inicial `PT15M`.

Iniciar:

```powershell
docker compose up --build -d
docker compose ps
```

Accesos:

- aplicación: `http://localhost:8080`;
- backend: `http://localhost:8081/api`;
- Swagger: `http://localhost:8081/api/swagger-ui.html`;
- salud: `http://localhost:8081/api/actuator/health`.

El administrador de desarrollo se crea con `APP_DEV_ADMIN_USERNAME` y `APP_DEV_ADMIN_PASSWORD`. No existe una contraseña real en el repositorio.

## Flujo funcional de 0.1.1

1. Iniciar sesión.
2. Crear un cliente con domicilio y preferencias.
3. Buscar o editar el cliente cuando corresponda.
4. Entrar en **Pedidos** y seleccionar **Nuevo pedido**.
5. Elegir cliente, domicilio y servicio vigente.
6. Agregar las piezas físicas; la vista previa calcula grupos, unidades y peso estimado.
7. Informar peso declarado, promoción, retiro y promesa cuando correspondan.
8. Crear y cotizar el pedido.
9. Abrir el pedido desde el listado.
10. Revisar prendas, límites y desglose del precio.
11. Confirmar el precio cuando no requiera cotización manual.
12. Avanzar únicamente por las transiciones habilitadas.
13. Registrar pagos parciales o totales.
14. Continuar el recorrido de estados hasta entrega y cierre.

Swagger sigue disponible para inspección y pruebas técnicas, pero ya no es obligatorio para el recorrido operativo implementado.

La guía detallada está en [docs/USER_GUIDE.md](docs/USER_GUIDE.md).

## Pruebas y validación

Backend, incluidas pruebas `*IT` con PostgreSQL real:

```bash
cd backend
mvn clean verify
```

Frontend:

```bash
cd frontend
npm ci
npm run lint
npm test
npm run build
```

Contenedores:

```bash
docker compose config --quiet
docker compose build
```

El workflow `.github/workflows/ci.yml` ejecuta backend, frontend y construcción de imágenes en cada pull request y actualización de `main`.

## Endpoints principales

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/auth/login` | Iniciar sesión y emitir cookie de renovación |
| POST | `/api/auth/refresh` | Rotar refresh token y emitir access token |
| POST | `/api/auth/logout` | Revocar la sesión actual |
| GET | `/api/catalog/equivalences` | Consultar equivalencias vigentes |
| GET | `/api/catalog/services` | Consultar servicios vigentes |
| POST | `/api/clients` | Crear cliente con domicilios y preferencias |
| GET | `/api/clients` | Buscar clientes paginados |
| GET | `/api/clients/{id}` | Consultar cliente y domicilios actuales |
| PUT | `/api/clients/{id}` | Actualizar perfil, estado y preferencias |
| POST | `/api/orders` | Crear y cotizar un pedido |
| GET | `/api/orders` | Buscar pedidos por número, cliente y estado |
| GET | `/api/orders/{id}` | Consultar detalle y transiciones permitidas |
| POST | `/api/orders/{id}/confirm-price` | Congelar el precio confirmado |
| PATCH | `/api/orders/{id}/status` | Cambiar estado con transición y auditoría |
| POST | `/api/payments` | Registrar pago parcial o total |

## Estructura

```text
backend/
  src/main/java/ar/com/ropalista/
    auth/ audit/ catalog/ common/ config/
    customer/ location/ order/ payment/ pricing/
  src/main/resources/db/migration/
frontend/
  src/api/ auth/ components/ models/ order/ pages/
infra/nginx/
scripts/
docs/
  adr/
.github/workflows/
```

La separación se realiza por módulo funcional. No se utiliza una estructura global basada exclusivamente en controladores, servicios y repositorios.

## Documentación

- [Estado, alcance y progreso](docs/PROJECT_STATUS.md)
- [Alcance funcional completo](docs/FUNCTIONAL_SCOPE.md)
- [Guía de puesta en marcha en Windows](docs/WINDOWS_SETUP.md)
- [Guía de uso funcional](docs/USER_GUIDE.md)
- [Arquitectura](docs/ARCHITECTURE.md)
- [Modelo de datos](docs/DATA_MODEL.md)
- [API](docs/API.md)
- [Pruebas](docs/TESTING.md)
- [Seguridad](docs/SECURITY.md)
- [Operación y despliegue](docs/OPERATIONS.md)
- [Supuestos explícitos](docs/ASSUMPTIONS.md)
- [Plan de fases](docs/ROADMAP.md)
- [Registro de cambios](CHANGELOG.md)

## Limitaciones actuales

- Los pedidos con `requiresQuote=true` todavía no pueden recibir un ajuste manual de precio desde la aplicación.
- La recepción, peso real, fotografías y daños preexistentes siguen pendientes.
- La edición histórica de domicilios continúa pendiente; 0.1.1 evita sobrescribir domicilios existentes.
- El bloqueo de login es local a cada instancia y requiere almacenamiento compartido para despliegues distribuidos.
- Compatibilidad, ciclos, máquinas, bolsas, secado y relavados pertenecen a Fase 2.
- Rutas, agenda operativa, retiros, entregas y WhatsApp pertenecen a Fase 3.
- Costos, caja completa, tiempos y rentabilidad pertenecen a Fase 4.
- Abonos, comercios, inventario, mantenimiento, reclamos y tableros avanzados pertenecen a Fase 5.
- El perfil `dev` no debe utilizarse como despliegue productivo.
