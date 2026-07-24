# Instrucciones para agentes automáticos

Este archivo aplica a todo el repositorio.

## Lectura obligatoria

Antes de analizar o modificar:

1. leer `docs/AI_CONTEXT.md`;
2. usar `docs/README.md` como índice;
3. consultar la documentación específica de la capa afectada;
4. leer cualquier `AGENTS.md` más cercano al archivo objetivo.

## Git canónico

- repositorio: `JerePrograma/fourbubbles`;
- rama de trabajo: `main`;
- fuente de verdad: `origin/main`;
- no crear ramas ni Pull Requests salvo autorización expresa.

Antes de editar:

```bash
git rev-parse --show-toplevel
git remote get-url origin
git status --short
git branch --show-current
git fetch origin --prune
git rev-parse HEAD
git rev-parse origin/main
git rev-list --left-right --count main...origin/main
git log -1 --oneline --decorate
```

Si el workspace está limpio y no hay divergencia:

```bash
git switch main
git pull --ff-only origin main
```

Cambios locales ajenos, remoto incorrecto, conflictos o divergencia no resoluble por fast-forward son `BLOQUEANTE`. No usar `reset --hard`, `clean`, `push --force`, stashes, ramas ni Pull Requests sin autorización.

## Reglas técnicas inviolables

- Monolito modular por funcionalidad bajo `ar.com.ropalista`.
- Los controladores delegan en aplicación; no incorporar reglas ni acceso nuevo a repositorios.
- Flyway es la autoridad. `V1` a `V10` están publicadas y son inmutables; todo cambio de esquema usa `V11+`.
- Hibernate permanece con `ddl-auto=validate`.
- No mezclar pedido declarado, recepción real, compatibilidad histórica ni ejecución productiva.
- No reinterpretar precios, recepciones, evaluaciones o ciclos históricos.
- `COMPAT-1` es semántica histórica; cualquier cambio exige una versión nueva.
- Los pares de pedidos se bloquean por UUID canónico.
- Una excepción no cambia el resultado de compatibilidad; en producción solo puede exigir `separationRequired`.
- `separationRequired` es una marca operativa, no trazabilidad física de bolsas o compartimentos.
- La creación de ciclos conserva idempotencia, bloqueo de máquina, bloqueo ordenado de pedidos y capacidad en gramos.
- Un programa usado conserva sus parámetros técnicos; la base lo protege mediante `V10`.
- El frontend no decide permisos, transiciones, capacidad, compatibilidad ni precio.
- Access token en memoria; refresh en cookie `HttpOnly`.
- No versionar secretos, credenciales, tokens, datos personales ni `.env`.

## Validaciones canónicas

```bash
cd backend
mvn clean verify
```

```bash
cd frontend
npm ci --no-audit --no-fund
npm run lint
npm test
npm run build
```

```powershell
.\scripts\tests\Local.Common.Tests.ps1
.\scripts\Start-Local.ps1 -Rebuild -SkipOpen
.\scripts\Verify-Local.ps1
```

```bash
docker compose config --quiet
docker compose build
```

La instalación debe registrar al menos diez migraciones Flyway exitosas. Los estados remotos esperados son `validation/ci-summary=success` y `validation/runtime-smoke=success`.

Antes de confirmar:

```bash
git diff --check
git diff --stat
git status --short
```

## Informe final

Informar: veredicto, SHA inicial/final, archivos y símbolos modificados, comandos con resultado real, validaciones no ejecutadas, commit, publicación, estado local/remoto, pendientes y bloqueantes.
