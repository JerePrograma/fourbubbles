# Four Bubbles / Ropa Lista

Sistema de gestión para una lavandería doméstica con retiro y entrega, inicialmente orientado a Marcos Paz y Mariano Acosta.

> Versión: **0.2.0**. El circuito administrativo y la recepción física base están implementados. Compatibilidad, producción, logística, caja/costos y crecimiento continúan en fases posteriores.

## Alcance disponible

### Plataforma

- Java 21, Spring Boot 3 y Maven.
- React 18, TypeScript, Vite, React Router, React Hook Form, Zod y Vitest.
- PostgreSQL 16.
- Flyway V1–V7 como única autoridad del esquema.
- Hibernate con `ddl-auto=validate`.
- Monolito modular por dominio.
- OpenAPI/Swagger y Actuator.
- Docker Compose, Nginx e imágenes multi-stage.
- CI de backend, frontend y contenedores.
- smoke test con stack real, login y API protegida.

### Seguridad y consistencia

- JWT HS256 y refresh token opaco rotativo.
- BCrypt, cookies seguras y access token solo en memoria.
- jerarquía `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- autorización por método.
- respuestas JSON uniformes, `X-Request-ID` y logs correlacionados.
- limitador básico de login.
- bloqueo pesimista para promociones, pagos y recepción.
- idempotencia de recepción mediante `Idempotency-Key`.

### Operación

- clientes, preferencias y domicilios versionados;
- servicios, equivalencias, precios y promociones versionados;
- pedidos, planificación, cotización automática/manual y estados;
- promociones revalidadas al confirmar;
- pagos parciales/totales e historial;
- auditoría consultable;
- recepción física con peso y conteo reales;
- diferencias contra lo declarado;
- inspección por prenda, daños y manchas;
- aprobación o rechazo de diferencias;
- etiqueta y bolsa;
- metadatos de evidencias externas;
- UI completa para clientes, pedidos, recepción, pagos y auditoría.

## Lo que todavía no está terminado

- motor de compatibilidad;
- ciclos, máquinas, lavado, secado, calidad y relavado;
- carga binaria/almacenamiento gestionado de fotografías;
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

La verificación comprueba contenedores, health, siete migraciones Flyway o más, SPA, login y una API protegida.

| Componente | URL |
|---|---|
| Aplicación | `http://localhost:8080` |
| API | `http://localhost:8081/api` |
| Swagger | `http://localhost:8081/api/swagger-ui.html` |
| Health | `http://localhost:8081/api/actuator/health` |

Guía completa: [docs/WINDOWS_SETUP.md](docs/WINDOWS_SETUP.md).

## Flujo funcional disponible

1. Iniciar sesión.
2. Crear cliente y domicilio principal.
3. Agregar/cambiar/desactivar domicilios conservando historial.
4. Crear pedido y registrar composición declarada.
5. Revisar precio automático o aplicar cotización manual como `ADMIN`.
6. Confirmar precio y programar retiro.
7. Avanzar hasta `PICKED_UP`.
8. Abrir **Recepción** desde el pedido.
9. Registrar peso real, conteo por prenda, daños, manchas, bolsa y evidencia metadata.
10. Si no hay diferencias materiales, el pedido queda `CLASSIFIED`.
11. Si hay diferencias, queda `WAITING_PRICE_APPROVAL` hasta aprobar o rechazar.
12. Continuar por las transiciones habilitadas.
13. Registrar pagos y consultar historial.
14. Consultar auditoría como `ADMIN`.

Guía completa: [docs/USER_GUIDE.md](docs/USER_GUIDE.md).

## Recepción e idempotencia

`POST /api/orders/{id}/reception` exige:

```http
Idempotency-Key: web-reception-<uuid>
```

La misma clave para el mismo pedido devuelve la recepción existente. Otra clave no crea una segunda recepción. La operación bloquea el pedido y conserva separados los datos declarados y reales.

Se exige aprobación cuando existe:

- diferencia de piezas;
- daño detectado;
- diferencia de peso superior a 250 g o al 10 % declarado.

Una mancha se registra, pero no obliga por sí sola a aprobación.

## Validación técnica

```bash
cd backend
mvn clean verify

cd ../frontend
npm ci
npm run lint
npm test
npm run build

cd ..
docker compose config --quiet
docker compose build
```

## Endpoints principales

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/auth/login` | iniciar sesión |
| GET/POST/PUT | `/api/clients...` | clientes y domicilios |
| GET/POST/PATCH | `/api/orders...` | pedidos, precio y estados |
| POST | `/api/orders/{id}/reception` | recepción idempotente |
| GET | `/api/orders/{id}/reception` | consultar recepción |
| POST | `/api/orders/{id}/reception/decision` | aprobar/rechazar diferencias |
| POST/GET | `/api/payments` | pagos e historial |
| GET | `/api/audit` | auditoría administrativa |

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

Compose usa perfil `dev`. No es una topología productiva. Faltan TLS, secretos administrados, backups restaurables, almacenamiento de objetos, observabilidad central, límites de recursos y procedimiento de rollback probado.
