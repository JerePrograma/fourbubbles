# Four Bubbles / Ropa Lista

Sistema de gestión para una lavandería doméstica con retiro y entrega, inicialmente orientado a Marcos Paz y Mariano Acosta.

> Versión: **0.4.0**. Administración, recepción, compatibilidad y producción física base están implementadas. Logística, caja/costos, almacenamiento binario y crecimiento continúan en fases posteriores.

## Implementado

### Plataforma

- Java 21, Spring Boot 3, Maven, React 18, TypeScript, Vite y Vitest.
- PostgreSQL 16 y Flyway V1-V10 como autoridad del esquema.
- Hibernate con `ddl-auto=validate`.
- Monolito modular, OpenAPI, Actuator, Docker Compose y Nginx.
- CI de backend, frontend, contenedores y smoke test del stack real.

### Seguridad y consistencia

- JWT HS256, refresh token opaco rotativo, BCrypt y cookie segura.
- jerarquía `ADMIN > OPERATOR > DRIVER > REPORT_VIEWER`.
- autorización por método, errores JSON, `X-Request-ID` y logs correlacionados.
- bloqueo pesimista en promociones, pagos, recepción, compatibilidad, máquinas, programas, ciclos y pedidos.
- recepción y planificación de ciclos idempotentes.
- excepciones de compatibilidad exclusivas de `ADMIN` y auditadas.

### Administración y recepción

- clientes, preferencias y domicilios versionados;
- servicios, equivalencias, precios y promociones versionados;
- pedidos, planificación, cotización automática/manual y estados;
- pagos parciales/totales e historial;
- recepción física con peso, conteo, daños, manchas, etiqueta y bolsa;
- aprobación o rechazo de diferencias;
- auditoría consultable.

### Compatibilidad

- perfil efectivo por pedido clasificado;
- color, material, temperatura, secadora, suavizante y fragancia;
- condiciones hipoalergénicas, bebé, mascotas, suciedad y exclusividad;
- razones `HARD`/`WARNING` y recomendación compartida;
- evaluaciones históricas por versiones;
- excepción administrativa separada del resultado original;
- concurrencia A/B frente a B/A sin duplicar evaluaciones.

### Producción

- máquinas lavadora/secadora con código, capacidad, estado y vigencia;
- programas de lavado/secado con duración y parámetros técnicos;
- parámetros técnicos de un programa congelados desde su primer uso;
- ciclos `PLANNED`, `RUNNING`, `COMPLETED` y `CANCELLED`;
- `Idempotency-Key` obligatoria para planificar;
- uno o dos pedidos por ciclo;
- peso real de recepción y límite de capacidad;
- máquina única por ciclo activo;
- pedido único por etapa activa;
- compatibilidad efectiva vigente para ciclos compartidos;
- separación requerida cuando la combinación depende de excepción;
- exclusividad respetada incluso con excepción;
- estados reales `WAITING_WASH`, `WASHING`, `WAITING_DRY`, `DRYING`, `QUALITY_CONTROL` y `REWASH_REQUIRED`;
- finalización de lavado/secado;
- control de calidad `PASS` o `REWASH`;
- historial de ciclo y pedido;
- auditoría de configuración y ejecución;
- UI de planificación, operación, calidad, máquinas y programas.

## Pendiente

- inventario real de insumos y consumo por ciclo;
- mantenimiento preventivo/correctivo completo;
- carga binaria gestionada de fotografías;
- rutas, kilómetros, paradas, agenda y WhatsApp;
- caja, costos, margen y rentabilidad;
- abonos, comercios, reclamos y compensaciones.

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

La verificación comprueba contenedores, health, diez migraciones Flyway o más, SPA, login y una API protegida.

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
8. Guardar el perfil de compatibilidad.
9. Evaluar otro pedido cuando se pretenda compartir tratamiento.
10. Abrir **Producción**.
11. Elegir programa, máquina y uno o dos pedidos.
12. Planificar el ciclo.
13. Iniciar el ciclo.
14. Completar informando peso real.
15. Planificar secado cuando corresponda.
16. Resolver control de calidad como aprobado o relavado.
17. Registrar pagos y consultar auditoría.

## Reglas de producción

### Planificación

- Solo `ADMIN` u `OPERATOR`.
- Una clave idempotente identifica la planificación.
- La máquina debe estar activa y libre.
- El programa debe corresponder al tipo de máquina.
- El peso real total no puede superar la capacidad.
- El programa no puede ser más agresivo que ningún perfil.
- Un ciclo compartido requiere evaluación vigente y efectivamente compatible.
- Un pedido exclusivo nunca comparte ciclo.
- Una excepción de compatibilidad exige separación física y no anula capacidad o exclusividad.

### Lavado y secado

```text
CLASSIFIED / REWASH_REQUIRED
→ WAITING_WASH
→ WASHING
→ WAITING_DRY o QUALITY_CONTROL
→ DRYING
→ QUALITY_CONTROL
→ FOLDING o REWASH_REQUIRED
```

Si un perfil no admite secadora, el lavado completado pasa directamente a control de calidad. El secado no se registra como mecánico en ese caso.

### Historial

Cada ciclo conserva:

- máquina y programa;
- pesos planificado/real;
- pedidos y posición;
- requisito de separación;
- inicio, finalización o cancelación;
- historial de estados y actor.

Los parámetros técnicos de un programa utilizado no pueden modificarse. Puede cambiar su nombre, notas o activación sin reinterpretar ciclos previos.

## Endpoints principales

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/auth/login` | iniciar sesión |
| GET/POST/PUT | `/api/clients...` | clientes y domicilios |
| GET/POST/PATCH | `/api/orders...` | pedidos, precio y estados |
| POST/GET | `/api/orders/{id}/reception...` | recepción y decisión |
| PUT/GET | `/api/orders/{id}/compatibility-profile` | perfil de tratamiento |
| POST | `/api/compatibility/evaluate` | evaluar dos pedidos |
| GET/POST/PUT | `/api/production/machines...` | máquinas |
| GET/POST/PUT | `/api/production/programs...` | programas |
| GET/POST | `/api/production/cycles...` | planificar y consultar ciclos |
| POST | `/api/production/cycles/{id}/start` | iniciar ciclo |
| POST | `/api/production/cycles/{id}/complete` | completar ciclo |
| POST | `/api/production/cycles/{id}/cancel` | cancelar planificación |
| PATCH | `/api/production/orders/{id}/quality-control` | control de calidad |
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

Compose usa perfil `dev`. No es una topología productiva. Faltan TLS, secretos administrados, backups restaurables, almacenamiento de objetos, observabilidad central, límites de recursos, recuperación ante fallos y rollback probado.
