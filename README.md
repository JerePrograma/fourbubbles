# Four Bubbles / Ropa Lista

Sistema de gestión para una lavandería doméstica con retiro y entrega, inicialmente orientado a Marcos Paz y Mariano Acosta.

> Versión funcional: **0.4.2**. Última actualización documental: **2026-07-26**.

## Estado

Están implementados:

- administración de clientes, domicilios, catálogo, pedidos, precios, promociones, pagos y auditoría;
- recepción física idempotente con peso, conteo, inspección, diferencias y decisión;
- compatibilidad explicable mediante perfiles, razones, recomendación y excepción administrativa;
- producción con máquinas, programas, ciclos, capacidad, lavado, secado y control de calidad;
- separación física trazable por contenedor para ciclos compartidos habilitados mediante excepción;
- métricas productivas por período sobre estados, etapas, pesos, duración y separación.

Una carga compartida exige perfiles vigentes, compatibilidad efectiva, ausencia de exclusividad y capacidad suficiente. Cuando depende de una excepción, cada pedido debe tener un contenedor distinto confirmado por un operador antes de iniciar el ciclo.

## Stack

- Java 21, Spring Boot 3, Maven, JPA/Hibernate y Flyway;
- PostgreSQL 16;
- React 18, TypeScript, Vite y Vitest;
- Docker Compose, Nginx, PowerShell 7 y GitHub Actions.

Flyway `V1`–`V11` es la autoridad del esquema. Hibernate usa `ddl-auto=validate`.

## Inicio local

```powershell
git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
git pull --ff-only origin main
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Los scripts preservan secretos existentes, detectan conflictos de puertos, esperan health real y validan al menos once migraciones.

## Flujo funcional

1. Crear cliente, domicilio y pedido.
2. Confirmar precio, retirar y registrar recepción.
3. Resolver diferencias hasta `CLASSIFIED`.
4. Guardar perfil y evaluar compatibilidad cuando se comparta tratamiento.
5. Planificar un ciclo de uno o dos pedidos.
6. Cuando el ciclo compartido dependa de excepción, abrir **Separación** y confirmar un contenedor distinto por pedido.
7. Iniciar/completar lavado y secado cuando corresponda.
8. Abrir **Métricas** para revisar la operación del período.
9. Resolver calidad: `PASS → FOLDING`; `REWASH → REWASH_REQUIRED`.
10. Registrar pagos y consultar auditoría.

## Límites actuales

- la confirmación de separación es operativa, no una verificación física automatizada;
- las métricas no calculan costos ni capacidad histórica;
- no hay asignación automática óptima, insumos, costos ni mantenimiento completo;
- evidencias de recepción almacenan metadata, no archivos;
- no hay rutas, caja completa, inventario ni reclamos;
- no hay navegador E2E, carga, DAST ni accesibilidad automatizada;
- Compose usa `dev` y no es despliegue productivo.

## Documentación

- [Contexto para agentes](docs/AI_CONTEXT.md)
- [Índice documental](docs/README.md)
- [Estado integral](docs/PROJECT_STATUS.md)
- [Release 0.4.2](docs/RELEASE_0_4_2.md)
- [Release 0.4.1](docs/RELEASE_0_4_1.md)
- [API](docs/API.md)
- [Modelo de datos](docs/DATA_MODEL.md)
- [Alcance funcional](docs/FUNCTIONAL_SCOPE.md)
- [Guía de uso](docs/USER_GUIDE.md)
- [Pruebas](docs/TESTING.md)
- [Roadmap](docs/ROADMAP.md)
- [Changelog](CHANGELOG.md)
