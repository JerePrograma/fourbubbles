# Four Bubbles / Ropa Lista

Sistema de gestión integral para una lavandería doméstica con retiro y entrega en Marcos Paz y Mariano Acosta.

> Estado: **Fase 1 en curso**. La rama de trabajo contiene una plataforma ejecutable y una primera vertical operativa; no representa todavía el MVP completo.

## Qué incluye esta entrega

- Monolito modular Java 21 + Spring Boot 3.
- PostgreSQL 16 y migraciones Flyway desde la primera tabla.
- Autenticación JWT de corta duración y refresh token opaco rotativo en cookie `HttpOnly`.
- Roles iniciales: administrador, operador, repartidor y consulta/reportes.
- Auditoría persistente de operaciones sensibles implementadas.
- Clientes con domicilios y validación de zona.
- Catálogo de servicios, equivalencias y precios versionados.
- Promociones con vigencia, cupos y restricción por domicilio; las reglas no automatizadas se bloquean.
- Pedidos con número legible, piezas físicas, grupos, unidades equivalentes, peso, precio histórico y trazabilidad de estados.
- Registro de pagos parciales con saldo y estado de pago.
- Frontend React/TypeScript mobile first con inicio de sesión, renovación transparente, tablero inicial y clientes.
- Docker Compose, Nginx y GitHub Actions.
- Pruebas unitarias e integración con PostgreSQL mediante Testcontainers.
- Documentación de alcance, arquitectura, datos, seguridad, operación, pruebas, decisiones y backlog.

## Requisitos locales

- Docker Engine con Docker Compose v2, o:
  - Java 21;
  - Maven 3.6.3 o superior;
  - Node.js 22;
  - PostgreSQL 16.

## Inicio rápido con Docker

```bash
cp .env.example .env
```

Reemplazar obligatoriamente:

- `POSTGRES_PASSWORD`;
- `JWT_SECRET_BASE64`;
- `APP_DEV_ADMIN_PASSWORD`.

Generar un secreto JWT válido, por ejemplo:

```bash
openssl rand -base64 48
```

Iniciar:

```bash
docker compose up --build
```

Accesos:

- aplicación: `http://localhost:8080`;
- backend directo: `http://localhost:8081/api`;
- Swagger: `http://localhost:8081/api/swagger-ui.html`;
- salud: `http://localhost:8081/api/actuator/health`.

El usuario administrador de desarrollo se crea al iniciar con las variables `APP_DEV_ADMIN_USERNAME` y `APP_DEV_ADMIN_PASSWORD`. No existe una contraseña real almacenada en el repositorio.

## Ejecución sin Docker

Base de datos:

```bash
createdb ropalista
```

Backend:

```bash
cd backend
export DB_HOST=localhost
export DB_NAME=ropalista
export DB_USER=ropalista
export DB_PASSWORD=...
export JWT_SECRET_BASE64=...
export APP_DEV_ADMIN_PASSWORD=...
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Pruebas

```bash
cd backend
mvn clean test             # unitarias
mvn verify -DskipTests     # integración *IT con Testcontainers

cd ../frontend
npm run lint
npm run build
```

## Endpoints implementados

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/auth/login` | Iniciar sesión y emitir cookie de renovación |
| POST | `/api/auth/refresh` | Rotar refresh token y emitir access token |
| POST | `/api/auth/logout` | Revocar sesión actual |
| GET | `/api/catalog/equivalences` | Consultar equivalencias vigentes |
| POST | `/api/clients` | Crear cliente con domicilio principal |
| GET | `/api/clients` | Buscar clientes paginados |
| GET | `/api/clients/{id}` | Consultar cliente e historial domiciliario actual |
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
docs/
  adr/
.github/workflows/
```

La separación es por módulo funcional. No se utiliza una carpeta global de controladores/servicios/repositorios.

## Documentación obligatoria

- [Estado, alcance y progreso](docs/PROJECT_STATUS.md)
- [Alcance funcional completo](docs/FUNCTIONAL_SCOPE.md)
- [Arquitectura](docs/ARCHITECTURE.md)
- [Modelo de datos](docs/DATA_MODEL.md)
- [API](docs/API.md)
- [Pruebas](docs/TESTING.md)
- [Seguridad](docs/SECURITY.md)
- [Operación y despliegue](docs/OPERATIONS.md)
- [Supuestos explícitos](docs/ASSUMPTIONS.md)
- [Plan de fases](docs/ROADMAP.md)
- [Registro de cambios](CHANGELOG.md)

## Limitaciones verificadas de la entrega

- La UI de recepción y carga de pedido todavía no está terminada; la API sí está modelada.
- Compatibilidad y ciclos pertenecen a Fase 2 y no se simulan.
- Rutas y agenda operativa real pertenecen a Fase 3.
- Costos, caja completa y rentabilidad pertenecen a Fase 4.
- No hay integración directa con WhatsApp ni almacenamiento de fotografías todavía.
- No existe `package-lock.json` porque el entorno de generación no tuvo acceso de red a npm; el CI debe generarlo y se debe versionar en el siguiente corte.
