# Four Bubbles — Ropa Lista

Monolito modular para gestionar una lavandería doméstica con retiro y entrega en Marcos Paz y Mariano Acosta.

## Estado de esta entrega

Esta rama contiene la base técnica y el primer corte vertical de la Fase 1:

- autenticación JWT con access y refresh token;
- usuarios y permisos iniciales;
- clientes, domicilios, zonas y preferencias;
- prendas equivalentes versionadas;
- servicios, precios vigentes y promociones;
- pedidos, ítems, pesaje, cotización, confirmación de precio y trazabilidad de estados;
- pagos básicos;
- auditoría técnica y funcional;
- React responsive con login, tablero, clientes y pedidos;
- PostgreSQL, Flyway, Docker Compose, Nginx y GitHub Actions.

No pretende completar todavía ciclos, logística avanzada, caja, costos, inventario ni tableros avanzados. Esos módulos permanecen en las siguientes fases.

## Requisitos

- Java 21
- Maven 3.9+
- Node.js 22+
- Docker con Docker Compose

## Inicio reproducible con Docker

```bash
cp .env.example .env
# Cambiar al menos JWT_SECRET_BASE64 y DEV_ADMIN_PASSWORD.
docker compose up --build
```

- Frontend: http://localhost:3000
- API: http://localhost:8080/api/v1
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## Inicio sin Docker

### Base de datos

Crear una base PostgreSQL y exportar:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/ropa_lista
export DB_USERNAME=ropa_lista
export DB_PASSWORD=...
export JWT_SECRET_BASE64=...
export DEV_ADMIN_EMAIL=admin@local.invalid
export DEV_ADMIN_PASSWORD=...
```

### Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Verificación

```bash
cd backend
mvn verify

cd ../frontend
npm install
npm run lint
npm run build
```

La prueba de integración PostgreSQL usa Testcontainers y se omite automáticamente cuando Docker no está disponible.

## Seguridad

- No se incluyen secretos reales.
- El administrador de desarrollo se crea únicamente con el perfil `dev` y variables de entorno.
- Las contraseñas se almacenan con BCrypt.
- Los refresh tokens se almacenan hasheados.
- `ddl-auto` está configurado en `validate`; Flyway es la única fuente del esquema.
- La zona horaria de negocio es `America/Argentina/Buenos_Aires`.

## Documentación

- [Arquitectura](docs/architecture/domain-model.md)
- [Plan por fases](docs/phase-plan.md)
- [ADR](docs/adr/)
