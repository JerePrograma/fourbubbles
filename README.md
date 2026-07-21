# Four Bubbles / Ropa Lista

Sistema de gestión para una lavandería doméstica con retiro y entrega, inicialmente orientado a Marcos Paz y Mariano Acosta.

> Versión funcional: **0.3.0**. Revisión operativa local: **2026-07-21**.

## Estado

El circuito administrativo, la recepción física y la evaluación explicable de compatibilidad están implementados. Ciclos, máquinas, logística, caja/costos y crecimiento siguen pendientes.

### Plataforma

- Java 21, Spring Boot 3, Maven, React 18, TypeScript, Vite y Vitest.
- PostgreSQL 16 y Flyway V1-V8 como autoridad del esquema.
- Hibernate con `ddl-auto=validate`.
- Monolito modular, OpenAPI, Actuator, Docker Compose y Nginx.
- CI de backend, frontend, PowerShell, contenedores y smoke test del stack real.

### Operación implementada

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

El detalle funcional está en [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) y [docs/ROADMAP.md](docs/ROADMAP.md).

## Inicio local resistente a conflictos

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

`Start-Local.ps1`:

- crea o completa `.env` de forma idempotente;
- no reemplaza secretos existentes;
- valida los tres puertos publicados;
- detecta contenedores o procesos ajenos antes de construir;
- no detiene proyectos ajenos;
- limpia un inicio parcial fallido sin eliminar PostgreSQL;
- espera health real de PostgreSQL, backend y frontend;
- abre la aplicación salvo que se use `-SkipOpen`.

`Verify-Local.ps1` valida cero, uno o varios objetos JSON de Compose sin asumir una colección fija, resuelve los puertos efectivos y comprueba contenedores, health, Flyway, SPA, proxy Nginx, rechazo anónimo, login y catálogo protegido.

### Puertos configurables

| Variable | Puerto host predeterminado | Puerto interno | Servicio |
|---|---:|---:|---|
| `POSTGRES_HOST_PORT` | 5432 | 5432 | PostgreSQL |
| `BACKEND_HOST_PORT` | 8081 | 8080 | Spring Boot |
| `FRONTEND_HOST_PORT` | 8080 | 80 | Nginx/React |

La comunicación interna no cambia: `backend -> postgres:5432` y `frontend -> backend:8080`.

Ejemplo para convivir con WordPress u otros proyectos:

```dotenv
POSTGRES_HOST_PORT=15432
BACKEND_HOST_PORT=18081
FRONTEND_HOST_PORT=18080
```

Después:

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

Las URLs se imprimen con los puertos efectivos. No se deben codificar `8080`, `8081` o `5432` en procedimientos locales.

## Comandos operativos

Iniciar o reconciliar sin reconstruir:

```powershell
.\scripts\Start-Local.ps1
```

Reconstruir imágenes:

```powershell
.\scripts\Start-Local.ps1 -Rebuild
```

Recrear todo, destruyendo la base local:

```powershell
.\scripts\Start-Local.ps1 -Reset -Rebuild
```

Verificar:

```powershell
.\scripts\Verify-Local.ps1
```

Detener preservando datos:

```powershell
docker compose down --remove-orphans
```

Destruir también PostgreSQL:

```powershell
docker compose down -v --remove-orphans
```

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

## Documentación

- [Estado integral](docs/PROJECT_STATUS.md)
- [Guía Windows](docs/WINDOWS_SETUP.md)
- [Operación](docs/OPERATIONS.md)
- [Pruebas](docs/TESTING.md)
- [Alcance funcional](docs/FUNCTIONAL_SCOPE.md)
- [Guía de uso](docs/USER_GUIDE.md)
- [Arquitectura](docs/ARCHITECTURE.md)
- [Modelo de datos](docs/DATA_MODEL.md)
- [Contrato API](docs/API.md)
- [Seguridad](docs/SECURITY.md)
- [Supuestos](docs/ASSUMPTIONS.md)
- [Roadmap](docs/ROADMAP.md)
- [Changelog](CHANGELOG.md)

## Advertencia productiva

Compose usa perfil `dev` y publica servicios solo en `127.0.0.1`. No es una topología productiva. Faltan TLS, secretos administrados, backups restaurables, almacenamiento de objetos, observabilidad central, límites de recursos y rollback probado.
