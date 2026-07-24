# Instrucciones para agentes automáticos

Este archivo aplica a todo el repositorio.

## Lectura obligatoria

Antes de analizar o modificar:

1. leer [`docs/AI_CONTEXT.md`](docs/AI_CONTEXT.md);
2. usar [`docs/README.md`](docs/README.md) como índice;
3. consultar la documentación específica de la capa afectada;
4. leer todos los `AGENTS.md` más cercanos al archivo objetivo, si en el futuro se agregan.

## Estado Git canónico

Repositorio: `JerePrograma/fourbubbles`.

Rama de trabajo: `main`.

Fuente de verdad: `origin/main`.

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

Cambios locales ajenos, remoto incorrecto, conflictos o divergencia no resoluble por fast-forward son `BLOQUEANTE`. No usar `reset --hard`, `clean`, `push --force`, stashes, ramas ni Pull Requests sin autorización expresa.

## Reglas técnicas inviolables

- El backend es un monolito modular por funcionalidad bajo `ar.com.ropalista`.
- Los controladores deben delegar en aplicación y no incorporar reglas de dominio. `CatalogController` es una excepción técnica existente; no extender ese patrón.
- Flyway es la autoridad del esquema. No modificar migraciones publicadas `V1` a `V8`; agregar `V9` o superior.
- Hibernate usa `ddl-auto=validate`.
- No mezclar composición declarada del pedido con el snapshot real de recepción.
- No reinterpretar snapshots históricos de precio, recepción o compatibilidad.
- `CompatibilityEngine.RULE_VERSION` identifica semántica histórica. Cambiar reglas requiere una versión nueva.
- Al bloquear dos pedidos, conservar el orden UUID canónico usado por `CompatibilityService.compareCanonical`.
- Una excepción de compatibilidad no cambia `compatible`, razones ni recomendación original.
- El frontend no es autoridad de permisos, transiciones, precio ni compatibilidad.
- El access token permanece en memoria; el refresh token viaja en cookie `HttpOnly`.
- No versionar secretos, credenciales, tokens, datos personales ni archivos `.env`.

## Validaciones canónicas

Desde la raíz, según el alcance:

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

Antes de confirmar cambios:

```bash
git diff --check
git diff --stat
git status --short
```

Los estados remotos esperados son `validation/ci-summary=success` y `validation/runtime-smoke=success`.

## Documentación

Actualizar la fuente canónica correspondiente, no duplicar información dinámica. La entrada para agentes es [`docs/AI_CONTEXT.md`](docs/AI_CONTEXT.md). Estado, incidencias y roadmap viven en documentos dinámicos separados.

## Informe final

Informar como mínimo: veredicto, SHA inicial/final, archivos y símbolos modificados, comandos ejecutados con resultado real, validaciones no ejecutadas, commit, publicación, estado local/remoto, pendientes y bloqueantes.
