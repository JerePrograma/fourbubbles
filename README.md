# Four Bubbles / Ropa Lista

Sistema de gestión integral para una lavandería doméstica con retiro y entrega en Marcos Paz y Mariano Acosta.

> Estado: **plataforma base finalizada y Fase 1 parcial**. El repositorio contiene una vertical operativa verificable, pero todavía no representa el MVP completo de producción, logística y finanzas.

## Alcance disponible

- Monolito modular Java 21 + Spring Boot 3.
- PostgreSQL 16 y migraciones Flyway desde la primera tabla.
- Autenticación JWT de corta duración y refresh token opaco rotativo en cookie `HttpOnly`.
- Roles iniciales: administrador, operador, repartidor y consulta/reportes.
- Auditoría persistente de operaciones sensibles implementadas.
- Clientes con domicilios y validación de zona.
- Catálogo de servicios, equivalencias, precios y promociones versionados.
- Pedidos con número legible, piezas físicas, grupos, unidades equivalentes, peso declarado, precio histórico y trazabilidad de estados.
- Pagos parciales o totales con saldo.
- Frontend React/TypeScript mobile first con login, clientes y alta de cliente.
- API OpenAPI/Swagger para el recorrido operativo todavía no cubierto por pantallas.
- Docker Compose, Nginx y GitHub Actions.
- Pruebas unitarias e integración con PostgreSQL mediante Testcontainers.
- Dependencias frontend bloqueadas mediante `package-lock.json` y `npm ci`.
- Scripts PowerShell para iniciar y verificar el entorno local.
- Documentación de alcance, arquitectura, datos, seguridad, operación, uso, pruebas, decisiones y backlog.

## Requisitos recomendados

Para el camino simple:

- Git;
- Docker Desktop o Docker Engine;
- Docker Compose v2;
- PowerShell 7 en Windows.

Para ejecución sin Docker:

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

`Start-Local.ps1` crea `.env` únicamente si no existe, genera secretos aleatorios, construye los contenedores y espera a que el backend esté saludable. La contraseña administrativa generada se muestra una sola vez en la consola.

La guía manual completa y el diagnóstico están en [docs/WINDOWS_SETUP.md](docs/WINDOWS_SETUP.md).

## Inicio rápido manual con Docker

Linux/macOS:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item -LiteralPath '.env.example' -Destination '.env'
```

Reemplazar obligatoriamente:

- `POSTGRES_PASSWORD` y `DB_PASSWORD` con el mismo valor;
- `JWT_SECRET_BASE64` con al menos 32 bytes aleatorios codificados en Base64;
- `APP_DEV_ADMIN_PASSWORD` con una contraseña de desarrollo propia.

Iniciar:

```bash
docker compose up --build -d
docker compose ps
```

Accesos:

- aplicación: `http://localhost:8080`;
- backend directo: `http://localhost:8081/api`;
- Swagger: `http://localhost:8081/api/swagger-ui.html`;
- salud: `http://localhost:8081/api/actuator/health`.

El usuario administrador de desarrollo se crea al iniciar con `APP_DEV_ADMIN_USERNAME` y `APP_DEV_ADMIN_PASSWORD`. No existe una contraseña real almacenada en el repositorio.

## Flujo funcional actual

1. Iniciar sesión en la aplicación.
2. Crear un cliente y su domicilio principal desde la interfaz.
3. Abrir Swagger para consultar equivalencias.
4. Crear y cotizar un pedido con piezas físicas, peso declarado y servicio.
5. Revisar unidades equivalentes, límites y desglose del precio.
6. Confirmar el precio histórico.
7. Registrar transiciones válidas de estado.
8. Registrar pagos parciales o totales.
9. Consultar el pedido y su saldo.

El recorrido detallado con cuerpos JSON copiables está en [docs/USER_GUIDE.md](docs/USER_GUIDE.md).

## Pruebas y validación

Backend completo, incluidas pruebas `*IT` con Testcontainers:

```bash
cd backend
mvn clean verify
```

Solo pruebas unitarias:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

Contenedores:

```bash
docker compose config --quiet
docker compose build
```

El workflow `.github/workflows/ci.yml` ejecuta esos tres grupos en cada pull request y en cada actualización de `main`.

## Endpoints implementados

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/auth/login` | Iniciar sesión y emitir cookie de renovación |
| POST | `/api/auth/refresh` | Rotar refresh token y emitir access token |
| POST | `/api/auth/logout` | Revocar sesión actual |
| GET | `/api/catalog/equivalences` | Consultar equivalencias vigentes |
| POST | `/api/clients` | Crear cliente con domicilios |
| GET | `/api/clients` | Buscar clientes paginados |
| GET | `/api/clients/{id}` | Consultar cliente y domicilios actuales |
| POST | `/api/orders` | Crear y cotizar un pedido |
| GET | `/api/orders/{id}` | Consultar pedido |
| POST | `/api/orders/{id}/confirm-price` | Congelar precio confirmado |
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
  src/api/ auth/ components/ models/ pages/
infra/nginx/
scripts/
docs/
  adr/
.github/workflows/
```

La separación es por módulo funcional. No se utiliza una estructura global basada únicamente en controladores, servicios y repositorios.

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

- La interfaz todavía no contiene alta y gestión completa de pedidos; ese recorrido se valida mediante Swagger.
- La recepción, peso real, fotografías y daños preexistentes siguen pendientes.
- Compatibilidad, ciclos, máquinas, bolsas, secado y relavados pertenecen a Fase 2.
- Rutas, agenda operativa, retiros, entregas y WhatsApp pertenecen a Fase 3.
- Costos, caja completa, tiempos y rentabilidad pertenecen a Fase 4.
- Abonos, comercios, inventario, mantenimiento, reclamos y tableros avanzados pertenecen a Fase 5.
- No debe usarse el perfil `dev` como despliegue de producción.
