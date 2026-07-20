# Four Bubbles / Ropa Lista

Sistema de gestión para una lavandería doméstica con retiro y entrega, inicialmente orientado a Marcos Paz y Mariano Acosta.

> Versión: **0.1.2**. El circuito administrativo base está implementado y operable desde la interfaz. Recepción física, producción, logística, costos y crecimiento continúan en fases posteriores.

## Qué incluye 0.1.2

### Plataforma

- Java 21, Spring Boot 3 y Maven.
- React 18, TypeScript, Vite, React Router, React Hook Form, Zod y Vitest.
- PostgreSQL 16.
- Flyway V1–V6 como única autoridad del esquema.
- Hibernate con `ddl-auto=validate`.
- Monolito modular por dominio.
- OpenAPI/Swagger y Actuator.
- Docker Compose, Nginx e imágenes multi-stage.
- CI de backend, frontend y contenedores.
- smoke test que levanta el stack, inicia sesión y consulta una API protegida.

### Seguridad y observabilidad

- JWT HS256 de corta duración.
- refresh token opaco, hasheado, rotativo y revocable.
- cookie `HttpOnly`, `SameSite=Strict` y segura en producción.
- BCrypt.
- roles `ADMIN`, `OPERATOR`, `DRIVER` y `REPORT_VIEWER`.
- jerarquía `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- autorización por método para operaciones sensibles.
- contrato JSON uniforme para 401, 403, validaciones y errores de negocio.
- `X-Request-ID`, MDC y logs correlacionados.
- protección básica contra intentos repetidos de login.

### Operación administrativa

- clientes, estado y preferencias tipadas;
- múltiples domicilios activos;
- un único domicilio principal;
- cambio de principal, baja lógica, vigencia e historial;
- catálogo versionado de servicios, equivalencias, precios y promociones;
- pedidos con piezas físicas, grupos, unidades equivalentes y peso declarado;
- precio automático histórico y desglose explicable;
- cotización manual exclusiva de administrador, conservando el cálculo original;
- edición controlada de retiro, promesa y notas antes de confirmar;
- confirmación de precio y estados trazables;
- revalidación promocional bajo bloqueo pesimista;
- pagos parciales y totales, saldo e historial;
- serialización de pagos concurrentes para impedir sobrecobros;
- consulta administrativa de auditoría;
- interfaz de clientes, pedidos, pagos y auditoría.

## Lo que no está terminado

- recepción con peso y conteo reales;
- diferencias contra lo declarado;
- fotografías, daños, manchas y aprobación del cliente;
- etiquetas, bolsas e idempotencia de recepción;
- compatibilidad de prendas y ciclos de lavado/secado;
- máquinas, calidad, relavados y trazabilidad física;
- rutas, paradas, kilómetros y WhatsApp;
- caja, costos, margen y rentabilidad;
- abonos, comercios, inventario, mantenimiento y reclamos.

El detalle está en [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) y [docs/ROADMAP.md](docs/ROADMAP.md).

## Inicio rápido en Windows

Requisitos: Git, Docker Desktop y PowerShell 7.

```powershell
git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
git pull --ff-only origin main
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

`Start-Local.ps1` crea `.env` cuando no existe, genera secretos aleatorios, construye el stack y muestra una sola vez la contraseña del administrador de desarrollo.

`Verify-Local.ps1` comprueba:

1. servicios de Compose;
2. health del backend;
3. seis migraciones Flyway o más;
4. carga de la SPA;
5. login con `.env`;
6. consulta autenticada del catálogo.

Accesos locales:

| Componente | URL |
|---|---|
| Aplicación | `http://localhost:8080` |
| API | `http://localhost:8081/api` |
| Swagger | `http://localhost:8081/api/swagger-ui.html` |
| Health | `http://localhost:8081/api/actuator/health` |

Guía completa: [docs/WINDOWS_SETUP.md](docs/WINDOWS_SETUP.md).

## Inicio manual con Docker

```powershell
Copy-Item '.env.example' '.env'
```

Reemplazar en `.env`:

- `POSTGRES_PASSWORD` y `DB_PASSWORD` con el mismo valor;
- `JWT_SECRET_BASE64` con al menos 32 bytes aleatorios en Base64;
- `APP_DEV_ADMIN_PASSWORD` con una contraseña propia.

Luego:

```powershell
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

## Flujo funcional disponible

1. Iniciar sesión.
2. Crear un cliente y su domicilio principal.
3. Configurar preferencias operativas.
4. Agregar domicilios alternativos cuando corresponda.
5. Elegir el domicilio principal vigente.
6. Crear un pedido desde **Pedidos → Nuevo pedido**.
7. Agregar prendas físicas; la interfaz calcula grupos, unidades y peso estimado.
8. Revisar el precio automático y sus límites.
9. Si requiere presupuesto, un `ADMIN` registra una cotización manual con motivo.
10. Editar retiro, promesa y notas mientras el pedido esté en `INQUIRY` o `QUOTED`.
11. Confirmar el precio.
12. Avanzar únicamente por las transiciones habilitadas.
13. Registrar pagos parciales o totales.
14. Consultar el historial financiero.
15. Consultar auditoría con un usuario `ADMIN`.

Guía completa: [docs/USER_GUIDE.md](docs/USER_GUIDE.md).

## Validación técnica

Backend:

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

Smoke local:

```powershell
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

## Endpoints principales

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/auth/login` | iniciar sesión |
| POST | `/api/auth/refresh` | rotar la sesión |
| POST | `/api/auth/logout` | cerrar y revocar sesión |
| GET | `/api/catalog/services` | servicios vigentes |
| GET | `/api/catalog/equivalences` | equivalencias vigentes |
| POST | `/api/clients` | crear cliente |
| GET | `/api/clients` | buscar clientes |
| GET | `/api/clients/{id}` | consultar cliente, domicilios e historial |
| PUT | `/api/clients/{id}` | actualizar perfil y preferencias |
| POST | `/api/clients/{id}/addresses` | agregar domicilio |
| POST | `/api/clients/{id}/addresses/{addressId}/make-primary` | cambiar principal |
| DELETE | `/api/clients/{id}/addresses/{addressId}` | dar de baja un domicilio |
| POST | `/api/orders` | crear y cotizar pedido |
| GET | `/api/orders` | buscar pedidos |
| GET | `/api/orders/{id}` | consultar detalle |
| POST | `/api/orders/{id}/manual-quote` | cotización manual de administrador |
| PATCH | `/api/orders/{id}/planning` | editar planificación temprana |
| POST | `/api/orders/{id}/confirm-price` | congelar precio |
| PATCH | `/api/orders/{id}/status` | cambiar estado |
| POST | `/api/payments` | registrar pago |
| GET | `/api/payments?orderId=...` | historial de pagos |
| GET | `/api/audit` | buscar auditoría como administrador |

## Documentación

- [Estado integral](docs/PROJECT_STATUS.md)
- [Alcance funcional](docs/FUNCTIONAL_SCOPE.md)
- [Guía Windows](docs/WINDOWS_SETUP.md)
- [Guía de uso](docs/USER_GUIDE.md)
- [Arquitectura](docs/ARCHITECTURE.md)
- [Modelo de datos](docs/DATA_MODEL.md)
- [Contrato API](docs/API.md)
- [Pruebas](docs/TESTING.md)
- [Seguridad](docs/SECURITY.md)
- [Operación](docs/OPERATIONS.md)
- [Supuestos](docs/ASSUMPTIONS.md)
- [Roadmap](docs/ROADMAP.md)
- [Changelog](CHANGELOG.md)

## Advertencia productiva

El Compose actual usa el perfil `dev`. Es adecuado para desarrollo, demostración y pruebas, no para operación comercial sin TLS, secretos administrados, backups restaurables, observabilidad central, almacenamiento de evidencias, límites de recursos y estrategia de rollback.
