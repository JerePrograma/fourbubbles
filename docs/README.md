# Índice de documentación

Entrada para agentes: [`AI_CONTEXT.md`](AI_CONTEXT.md).

| Documento | Propósito |
|---|---|
| [`../README.md`](../README.md) | presentación e inicio |
| [`AI_CONTEXT.md`](AI_CONTEXT.md) | contexto para nuevas sesiones |
| [`PROJECT_STATUS.md`](PROJECT_STATUS.md) | estado actual |
| [`RELEASE_0_4_1.md`](RELEASE_0_4_1.md) | separación física trazable |
| [`RELEASE_0_4_0.md`](RELEASE_0_4_0.md) | producción base |
| [`ROADMAP.md`](ROADMAP.md) | próximos cortes |
| [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md) | deuda verificada |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | límites y flujos |
| [`REPOSITORY_MAP.md`](REPOSITORY_MAP.md) | rutas y símbolos |
| [`DATA_MODEL.md`](DATA_MODEL.md) | esquema Flyway `V1`–`V11` |
| [`API.md`](API.md) | contrato HTTP |
| [`FUNCTIONAL_SCOPE.md`](FUNCTIONAL_SCOPE.md) | funciones y límites |
| [`USER_GUIDE.md`](USER_GUIDE.md) | uso manual |
| [`SECURITY.md`](SECURITY.md) | sesiones, roles y riesgos |
| [`TESTING.md`](TESTING.md) | gates y pruebas |
| [`../CHANGELOG.md`](../CHANGELOG.md) | evolución |

## Reglas de mantenimiento

- código, migraciones y pruebas prevalecen;
- Flyway `V1`–`V11` es inmutable;
- no copiar secretos;
- no confundir confirmación operativa con prueba física automatizada;
- actualizar contratos, pruebas y documentación en el mismo corte.
