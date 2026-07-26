# Release 0.4.4 — Configuración productiva administrativa

Fecha: 2026-07-26.

## Objetivo

Completar la administración operativa de máquinas y programas existentes desde la SPA sin duplicar reglas ni alterar contratos backend.

## Cambios por ruta y símbolo

| Ruta | Símbolo | Cambio | Motivo | Prueba |
|---|---|---|---|---|
| `frontend/src/pages/ProductionConfigurationPage.tsx` | página | selección, edición y guardado | cerrar UI administrativa | TypeScript/build |
| `frontend/src/production/configurationForms.ts` | mappers | requests tipados y normalización WASH/DRY | evitar payloads inválidos | `configurationForms.test.ts` |
| `frontend/src/App.tsx` | ruta | `/production/configuration` | acceso SPA | TypeScript/build |
| `frontend/src/components/AppShell.tsx` | navegación | enlace solo para `ADMIN` | reducir exposición accidental | TypeScript/build |
| `frontend/src/production-configuration.css` | estilos | formularios responsive | usabilidad | build |

## Invariantes

- solo `ADMIN` ve la navegación y formulario;
- backend conserva autorización real;
- código/tipo de máquina son inmutables;
- código/etapa de programa son inmutables;
- programas `DRY` no envían temperatura, fragancia ni suavizante;
- ciclos activos y programas usados pueden bloquear actualizaciones;
- errores backend se muestran sin silenciamiento.

## Esquema y contratos

Sin migración, dependencia ni endpoint nuevo. Se reutilizan los `PUT` existentes.

## Validación requerida

Frontend TypeScript/Vitest/build, backend Maven completo, PowerShell, contenedores y runtime smoke; ambos estados agregados en `success` para el SHA final.

## Pendiente

E2E de navegador, versiones de artefactos, capacidad histórica, logística, costos y hardening productivo.
