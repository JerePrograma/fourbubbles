# Four Bubbles / Ropa Lista

Sistema de gestión para una lavandería doméstica con retiro y entrega, inicialmente orientado a Marcos Paz y Mariano Acosta.

> Versión funcional: **0.4.0**. Última actualización documental: **2026-07-24**.

## Estado

Están implementados cuatro cortes funcionales:

- administración de clientes, domicilios, catálogo, pedidos, precios, promociones, pagos y auditoría;
- recepción física idempotente con peso, conteo, inspección, diferencias y decisión;
- compatibilidad explicable mediante perfiles, razones, recomendación y excepción administrativa;
- producción base con máquinas, programas, ciclos, capacidad, lavado, secado y control de calidad.

La producción admite uno o dos pedidos. Una carga compartida exige perfiles vigentes, compatibilidad efectiva, ausencia de exclusividad y capacidad suficiente. Una excepción no elimina el riesgo original: el ciclo queda marcado con `separationRequired`.

## Stack

- Java 21, Spring Boot 3, Maven, JPA/Hibernate y Flyway;
- PostgreSQL 16;
- React 18, TypeScript, Vite y Vitest;
- Docker Compose, Nginx, PowerShell 7 y GitHub Actions.

Flyway `V1`–`V10` es la autoridad del esquema. Hibernate usa `ddl-auto=validate`.

## Inicio local

Requisitos: Git, Docker Desktop con contenedores Linux y PowerShell 7.

```powershell
git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
git pull --ff-only origin main
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Los scripts crean o completan `.env` sin reemplazar secretos, detectan conflictos de puertos, esperan health real y validan al menos diez migraciones.

Puertos host configurables:

| Variable | Predeterminado |
|---|---:|
| `POSTGRES_HOST_PORT` | 5432 |
| `BACKEND_HOST_PORT` | 8081 |
| `FRONTEND_HOST_PORT` | 8080 |

Los puertos internos no cambian: `postgres:5432`, `backend:8080`, `frontend:80`.

## Flujo funcional

1. Crear cliente y domicilio.
2. Crear pedido, revisar precio y confirmar.
3. Programar retiro y avanzar a `PICKED_UP`.
4. Registrar recepción y resolver diferencias hasta `CLASSIFIED`.
5. Guardar perfil de tratamiento.
6. Evaluar compatibilidad cuando dos pedidos puedan compartir tratamiento.
7. Abrir **Producción**, seleccionar máquina, programa y uno o dos pedidos.
8. Planificar ciclo con `Idempotency-Key`.
9. Iniciar y completar lavado.
10. Planificar/completar secado cuando el perfil lo permite.
11. Resolver control de calidad: `PASS` lleva a `FOLDING`; `REWASH` a `REWASH_REQUIRED`.
12. Registrar pagos y consultar auditoría.

## Límites actuales

- separación física interna no modelada: solo existe `separationRequired`;
- no hay asignación automática óptima, insumos, costos ni mantenimiento completo;
- evidencias de recepción almacenan metadata, no archivos;
- no hay rutas, caja completa, inventario ni reclamos;
- no hay navegador E2E, carga, DAST ni accesibilidad automatizada;
- Compose usa `dev` y no es despliegue productivo.

## Documentación

- [Contexto para agentes](docs/AI_CONTEXT.md)
- [Índice documental](docs/README.md)
- [Estado integral](docs/PROJECT_STATUS.md)
- [Release 0.4.0](docs/RELEASE_0_4_0.md)
- [Arquitectura](docs/ARCHITECTURE.md)
- [API](docs/API.md)
- [Modelo de datos](docs/DATA_MODEL.md)
- [Alcance funcional](docs/FUNCTIONAL_SCOPE.md)
- [Guía de uso](docs/USER_GUIDE.md)
- [Pruebas](docs/TESTING.md)
- [Operación](docs/OPERATIONS.md)
- [Roadmap](docs/ROADMAP.md)
- [Incidencias](docs/KNOWN_ISSUES.md)
- [Changelog](CHANGELOG.md)
