# Índice de documentación

Entrada recomendada para personas: este archivo.

Entrada recomendada para agentes: [`AI_CONTEXT.md`](AI_CONTEXT.md).

## Fuentes canónicas

| Documento | Tipo | Propósito y audiencia | Consultar cuando | Fuente de verdad relacionada |
|---|---|---|---|---|
| [`../README.md`](../README.md) | OPERATIVO | Presentación y arranque rápido | primera aproximación o inicio local | scripts y Compose |
| [`AI_CONTEXT.md`](AI_CONTEXT.md) | ESTABLE/DINÁMICO | Resumen denso para nuevas sesiones automatizadas | antes de cualquier tarea | este índice y documentos enlazados |
| [`PROJECT_STATUS.md`](PROJECT_STATUS.md) | DINÁMICO | Estado funcional, riesgos y progreso | planificación y cierre de tareas | código, pruebas y workflows |
| [`ROADMAP.md`](ROADMAP.md) | DINÁMICO | Orden de cortes futuros | priorización | estado funcional vigente |
| [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md) | DINÁMICO | Incidencias y deuda verificadas | diagnóstico o diseño de cambios | rutas y símbolos citados |
| [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md) | OPERATIVO | Síntomas, diagnóstico y solución segura | fallos de entorno o flujo | scripts, servicios y pruebas |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | ESTABLE | Componentes, límites y flujos críticos | cambios transversales o de dominio | paquetes, servicios y persistencia |
| [`REPOSITORY_MAP.md`](REPOSITORY_MAP.md) | ESTABLE | Mapa de rutas, módulos y puntos de entrada | localizar código | árbol de `main` |
| [`DATA_MODEL.md`](DATA_MODEL.md) | ESTABLE | Entidades, tablas y relaciones | persistencia o migraciones | Flyway `V1`–`V8` |
| [`API.md`](API.md) | ESTABLE | Contrato HTTP verificado | backend, frontend o integración | controladores y DTO |
| [`SECURITY.md`](SECURITY.md) | ESTABLE | Sesiones, roles, permisos y riesgos | autenticación/autorización | configuración y anotaciones |
| [`CONFIGURATION.md`](CONFIGURATION.md) | OPERATIVO | Variables, perfiles y precedencia | entorno local, test o producción | `.env.example`, YAML y Compose |
| [`OPERATIONS.md`](OPERATIONS.md) | OPERATIVO | Inicio, health, actualización y recuperación | operar el stack | scripts oficiales |
| [`WINDOWS_SETUP.md`](WINDOWS_SETUP.md) | OPERATIVO | Guía PowerShell/Windows | preparar una estación Windows | scripts oficiales |
| [`TESTING.md`](TESTING.md) | OPERATIVO | Gates y trazabilidad de pruebas | validar cambios | Maven, npm y workflows |
| [`FUNCTIONAL_SCOPE.md`](FUNCTIONAL_SCOPE.md) | ESTABLE | Alcance funcional detallado | requisitos y límites | servicios y pruebas |
| [`USER_GUIDE.md`](USER_GUIDE.md) | OPERATIVO | Recorrido funcional | uso manual | UI y API |
| [`ASSUMPTIONS.md`](ASSUMPTIONS.md) | ESTABLE | Supuestos explícitos del dominio | modificar reglas | código y restricciones |
| [`DECISIONS.md`](DECISIONS.md) | HISTÓRICO | Índice de decisiones y evidencia | evaluar trade-offs | ADR, migraciones y commits |
| [`adr/0001-modular-monolith.md`](adr/0001-modular-monolith.md) | HISTÓRICO | Monolito modular | límites de despliegue | arquitectura inicial |
| [`adr/0002-versioned-commercial-configuration.md`](adr/0002-versioned-commercial-configuration.md) | HISTÓRICO | Configuración comercial versionada | precios/promociones | modelo comercial |
| [`adr/0003-token-strategy.md`](adr/0003-token-strategy.md) | HISTÓRICO | Access JWT y refresh opaco | sesiones | seguridad |
| [`GLOSSARY.md`](GLOSSARY.md) | ESTABLE | Lenguaje del dominio | lectura o diseño | entidades y servicios |
| [`../CHANGELOG.md`](../CHANGELOG.md) | HISTÓRICO | Cambios por versión funcional | evolución | commits publicados |

## Reglas de mantenimiento

- Arquitectura, contratos y convenciones no deben contener estados temporales.
- Estado actual, incidencias y roadmap deben incluir fecha o commit de referencia.
- Las versiones de dependencias se consultan en `backend/pom.xml`, `frontend/package.json` y `frontend/package-lock.json`; no se duplican como inventario manual.
- Las migraciones publicadas se describen, no se copian completas.
- Un comando solo se documenta cuando aparece en scripts, manifiestos, README o CI.
- Las discrepancias se registran en [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md); no se presentan como comportamiento esperado.
