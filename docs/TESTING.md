# Estrategia y estado de pruebas

Última actualización: 2026-07-21.

Versión funcional: `0.3.0`.

## Gate continuo

### Backend

```bash
cd backend
mvn clean verify
```

El gate usa Java 21, PostgreSQL 16 mediante Testcontainers, Flyway V1-V8 y validación JPA.

Resultado histórico aceptado para 0.3.0:

- 25 pruebas unitarias;
- 19 pruebas de integración;
- 44 casos backend totales;
- 0 fallos.

### Frontend

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

El gate comprueba TypeScript estricto, Vitest y build Vite.

### PowerShell

```powershell
.\scripts\tests\Local.Common.Tests.ps1
```

Las pruebas no requieren Docker y cubren:

- lectura de `.env`, comentarios, comillas y valores con `=`;
- agregado idempotente de variables faltantes;
- preservación de secretos existentes;
- normalización de salida Compose vacía, objeto único, array y JSON por líneas;
- validación de rango y duplicación de puertos;
- inicialización de un `.env` existente sin reemplazos.

El job `powershell` se ejecuta con PowerShell 7 en GitHub Actions.

### Contenedores

```bash
docker compose config --quiet
docker compose build
```

El job usa puertos host alternativos para comprobar que Compose no depende de `5432`, `8080` ni `8081`.

## Runtime smoke

El workflow `Runtime smoke` prueba el stack real con:

```text
POSTGRES_HOST_PORT=15432
BACKEND_HOST_PORT=18081
FRONTEND_HOST_PORT=18080
```

Secuencia validada por el workflow:

1. construye e inicia frontend con `--no-deps`, antes de que exista backend;
2. exige que Nginx permanezca saludable, validando resolución DNS diferida;
3. inicia PostgreSQL y backend;
4. espera readiness;
5. valida SPA;
6. valida rechazo anónimo 401/403 por el proxy frontend;
7. realiza login por Nginx;
8. consulta catálogo protegido por Nginx;
9. consulta `flyway_schema_history` y exige ocho migraciones o más;
10. imprime diagnóstico ante falla y elimina stack y volumen al finalizar.

## Verificación local

```powershell
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

`Verify-Local.ps1` no continúa después de una aserción fallida. En su bloque de error muestra `docker compose ps --all` y logs recientes.

## Cobertura funcional existente

### Compatibilidad

- carga compatible sin razones duras;
- exclusividad bloqueante;
- color desconocido/distinto;
- materiales compatibles e incompatibles;
- aislamiento hipoalergénico;
- cruce bebé/mascotas;
- suciedad pesada contra carga sensible;
- fragancia incompatible;
- reducción de temperatura;
- deshabilitación de secadora y suavizante;
- persistencia de perfil efectivo;
- orden UUID canónico;
- reevaluación por cambio de versión;
- excepción exclusiva de `ADMIN`;
- concurrencia A/B y B/A sin snapshots duplicados.

### Seguridad y concurrencia

- 401 sin autenticación;
- 403 por rol insuficiente;
- cotización manual y auditoría `ADMIN`;
- recepción permitida a `DRIVER`;
- decisión de recepción restringida;
- promoción concurrente;
- pagos sin sobrecobro;
- recepción idempotente concurrente;
- compatibilidad concurrente.

## Diagnóstico de fallos

Los gates válidos son:

- `CI / backend`;
- `CI / frontend`;
- `CI / powershell`;
- `CI / containers`;
- `Runtime smoke / compose-smoke`.

No se declara validado un cambio solo porque `docker compose config` pase: el smoke debe confirmar ejecución real.

## Casos aún faltantes

- pruebas E2E de navegador;
- accesibilidad automatizada;
- property-based testing del motor;
- pruebas de carga;
- restauración desde backup;
- seguridad dinámica;
- ciclos/máquinas y capacidad;
- logística y costos.
