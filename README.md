# Four Bubbles / Ropa Lista

Sistema de gestión para una lavandería doméstica con retiro y entrega, inicialmente orientado a Marcos Paz y Mariano Acosta.

> Versión: **0.3.0**. El circuito administrativo, la recepción física y la evaluación explicable de compatibilidad están implementados. Ciclos, máquinas, logística, caja/costos y crecimiento siguen pendientes.

## Implementado

### Plataforma

- Java 21, Spring Boot 3, Maven, React 18, TypeScript, Vite y Vitest.
- PostgreSQL 16 y Flyway V1-V8 como autoridad del esquema.
- Hibernate con `ddl-auto=validate`.
- Monolito modular, OpenAPI, Actuator, Docker Compose y Nginx.
- CI de backend, frontend, contenedores y smoke test del stack real.

### Seguridad y consistencia

- JWT HS256, refresh token opaco rotativo, BCrypt y cookie segura.
- jerarquía `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- autorización por método, errores JSON, `X-Request-ID` y logs correlacionados.
- bloqueo pesimista en promociones, pagos, recepción y compatibilidad.
- recepción idempotente mediante `Idempotency-Key`.
- excepciones de compatibilidad exclusivas de `ADMIN` y auditadas.

### Operación

- clientes, preferencias y domicilios versionados;
- servicios, equivalencias, precios y promociones versionados;
- pedidos, planificación, cotización automática/manual y estados;
- pagos parciales/totales e historial;
- recepción física con peso, conteo, daños, manchas, etiqueta y bolsa;
- aprobación o rechazo de diferencias;
- perfil de tratamiento por pedido clasificado;
- comparación de dos pedidos con razones `HARD` y `WARNING`;
- recomendación de temperatura, secadora, suavizante, fragancia y programa;
- evaluaciones históricas por versión de perfil y versión de reglas;
- excepción administrativa separada del resultado original;
- UI para clientes, pedidos, recepción, compatibilidad, pagos y auditoría.

## Pendiente

- ciclos reales, máquinas, capacidad, lavado, secado, calidad y relavado;
- asignación de pedidos compatibles a ciclos;
- almacenamiento binario gestionado de fotografías;
- rutas, kilómetros, paradas y WhatsApp;
- caja, costos, margen y rentabilidad;
- abonos, inventario, mantenimiento y reclamos.

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

La verificación comprueba contenedores, health, ocho migraciones Flyway o más, SPA, login y una API protegida.

| Componente | URL |
|---|---|
| Aplicación | `http://localhost:8080` |
| API | `http://localhost:8081/api` |
| Swagger | `http://localhost:8081/api/swagger-ui.html` |
| Health | `http://localhost:8081/api/actuator/health` |

## Flujo funcional

1. Iniciar sesión.
2. Crear cliente y domicilio.
3. Crear pedido con composición declarada.
4. Revisar o ajustar precio y confirmar.
5. Programar retiro y avanzar hasta `PICKED_UP`.
6. Registrar recepción real.
7. Resolver diferencias hasta `CLASSIFIED`.
8. Abrir **Compatibilidad** y guardar el perfil.
9. Seleccionar otro pedido `CLASSIFIED` con perfil.
10. Evaluar compatibilidad y revisar razones/recomendación.
11. Como `ADMIN`, autorizar una excepción cuando corresponda.
12. Registrar pagos y consultar auditoría.

La compatibilidad no crea ni ejecuta ciclos: solo determina si dos pedidos podrían compartir tratamiento con las reglas vigentes.

## Perfil efectivo

El perfil solicitado nunca puede relajar restricciones persistidas:

- una prohibición de secadora o suavizante del cliente se conserva;
- la exigencia hipoalergénica se conserva y fuerza fragancia `NONE`;
- la exclusividad del pedido o cliente conserva `exclusiveCycle=true`.

Las evaluaciones se identifican por par ordenado, versiones de perfiles y versión del motor `COMPAT-1`. Actualizar un perfil produce una evaluación histórica nueva. Una excepción no altera el resultado original: solo cambia `effectivelyCompatible` y registra motivo, actor y fecha.

## Endpoints principales

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/auth/login` | iniciar sesión |
| GET/POST/PUT | `/api/clients...` | clientes y domicilios |
| GET/POST/PATCH | `/api/orders...` | pedidos, precio y estados |
| POST/GET | `/api/orders/{id}/reception...` | recepción y decisión |
| PUT/GET | `/api/orders/{id}/compatibility-profile` | perfil de tratamiento |
| POST | `/api/compatibility/evaluate` | evaluar dos pedidos |
| GET | `/api/compatibility/evaluations/{id}` | consultar evaluación |
| POST | `/api/compatibility/evaluations/{id}/exception` | excepción administrativa |
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

Compose usa perfil `dev`. No es una topología productiva. Faltan TLS, secretos administrados, backups restaurables, almacenamiento de objetos, observabilidad central, límites de recursos y rollback probado.
