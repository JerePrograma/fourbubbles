# Índice de documentación

Entrada para agentes: [`AI_CONTEXT.md`](AI_CONTEXT.md).

| Documento | Propósito |
|---|---|
| [`../README.md`](../README.md) | presentación e inicio |
| [`AI_CONTEXT.md`](AI_CONTEXT.md) | contexto denso para nuevas sesiones |
| [`PROJECT_STATUS.md`](PROJECT_STATUS.md) | estado actual |
| [`RELEASE_0_4_0.md`](RELEASE_0_4_0.md) | alcance del corte productivo |
| [`ROADMAP.md`](ROADMAP.md) | próximos cortes |
| [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md) | deuda verificada |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | límites y flujos |
| [`REPOSITORY_MAP.md`](REPOSITORY_MAP.md) | rutas y símbolos |
| [`DATA_MODEL.md`](DATA_MODEL.md) | esquema Flyway `V1`–`V10` |
| [`API.md`](API.md) | contrato HTTP |
| [`FUNCTIONAL_SCOPE.md`](FUNCTIONAL_SCOPE.md) | funciones y límites |
| [`USER_GUIDE.md`](USER_GUIDE.md) | uso manual |
| [`SECURITY.md`](SECURITY.md) | sesiones, roles y riesgos |
| [`TESTING.md`](TESTING.md) | gates y pruebas |
| [`CONFIGURATION.md`](CONFIGURATION.md) | variables y perfiles |
| [`OPERATIONS.md`](OPERATIONS.md) | inicio, actualización y recuperación |
| [`WINDOWS_SETUP.md`](WINDOWS_SETUP.md) | procedimiento PowerShell |
| [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md) | diagnóstico |
| [`ASSUMPTIONS.md`](ASSUMPTIONS.md) | supuestos explícitos |
| [`DECISIONS.md`](DECISIONS.md) | ADR y decisiones |
| [`GLOSSARY.md`](GLOSSARY.md) | términos |
| [`../CHANGELOG.md`](../CHANGELOG.md) | evolución |

## Reglas de mantenimiento

- código, migraciones y pruebas prevalecen sobre documentación;
- Flyway `V1`–`V10` es inmutable;
- estado y roadmap incluyen fecha/versión;
- no copiar secretos;
- no presentar `separationRequired` como tracking físico;
- actualizar backend, frontend, pruebas y documentos de contrato en el mismo corte.
