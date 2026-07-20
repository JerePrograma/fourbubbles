# Estrategia y estado de pruebas

Última actualización: 2026-07-20.

Versión: `0.1.1`.

## Pipeline

`.github/workflows/ci.yml` ejecuta tres jobs independientes.

### Backend

- Java 21 Temurin;
- `mvn clean verify`;
- JUnit 5 y Mockito;
- MockMvc;
- pruebas `*IT` mediante Failsafe;
- PostgreSQL 16 mediante Testcontainers;
- Flyway V1–V5;
- validación Hibernate/JPA.

### Frontend

- Node.js 22;
- `npm ci`;
- validación TypeScript con `npm run lint`;
- Vitest con `npm test`;
- build Vite.

### Contenedores

- `docker compose config --quiet`;
- build de imágenes backend y frontend.

## Comandos locales

```bash
cd backend
mvn clean verify

cd ../frontend
npm ci
npm run lint
npm test
npm run build

cd ..
docker compose config --quiet
docker compose build
```

## Pruebas unitarias backend

### Equivalencias

- agrupación de medias;
- agrupación incompleta;
- redondeo hacia arriba;
- preservación de piezas físicas;
- unidades equivalentes;
- peso estimado.

### Límites

- límite exacto de unidades;
- límite exacto de peso;
- exceso por unidades;
- exceso por peso;
- primer límite alcanzado;
- capacidad segura.

### Estados

- transiciones permitidas;
- saltos inválidos;
- exposición de transiciones válidas.

### Precios y promociones

- precio base;
- precio fijo promocional;
- primera compra;
- bloqueo de promoción manual;
- cupos simulados explícitamente.

### Login

- bloqueo al alcanzar la cantidad configurada;
- limpieza de fallos tras autenticación correcta;
- liberación al vencer el bloqueo.

## Pruebas frontend

`orderDraft.test.ts` verifica:

- redondeo de grupos incompletos sin perder piezas físicas;
- acumulación de equivalencias;
- peso conocido y desconocido;
- necesidad de cotización;
- ciclo exclusivo;
- conversión segura de fechas locales a instante ISO.

El cálculo mostrado por la UI es orientativo. La autoridad final sigue siendo el backend, que recalcula todas las reglas.

## Integración PostgreSQL

Las pruebas comparten un contenedor PostgreSQL 16 durante la suite.

### ApplicationContextIT

- inicia PostgreSQL;
- aplica Flyway;
- construye el contexto;
- valida entidades contra el esquema.

### ApiContractIT

- 401 uniforme para solicitud sin autenticación;
- 403 uniforme para rol insuficiente;
- `X-Request-ID` seguro conservado;
- identificador inseguro reemplazado;
- errores de validación con violaciones de campo;
- parámetros enum inválidos como 400.

### OperationalFlowIT

Recorrido real:

1. crea usuario administrativo;
2. inicia sesión;
3. crea cliente con domicilio y preferencias tipadas;
4. actualiza perfil y preferencias;
5. crea un pedido con prendas reales del seed;
6. busca el pedido por número;
7. confirma el precio;
8. registra un pago parcial;
9. registra el pago final;
10. comprueba saldo y estado `PAID`.

## Defectos que las pruebas ya detectaron

- palabra reservada usada como columna SQL;
- divergencia `CHAR(3)`/`VARCHAR(3)`;
- mapeo UUID ambiguo;
- algoritmo JWT no fijado;
- configuración TypeScript incorrecta;
- contratos 401/403 no uniformes;
- lógica frontend sensible a zona horaria;
- ausencia de prueba integrada del flujo de pago.

## Cobertura funcional cualitativa

| Área | Estado de prueba |
|---|---|
| autenticación básica | integración |
| refresh/logout | implementación, falta integración específica |
| autorización por rol | integración parcial |
| correlación | integración |
| limitador de login | unitario |
| cliente alta/actualización | integración |
| preferencias tipadas | integración |
| equivalencias | unitario + integración indirecta |
| límites | unitario + flujo integrado |
| precio | unitario + integración |
| promociones | unitario, concurrencia pendiente |
| búsqueda de pedidos | integración |
| confirmación de precio | integración |
| pagos parcial/total | integración |
| exceso de pago | lógica implementada, falta API específica |
| UI borrador de pedido | unitario puro + build |
| UI completa | build, falta E2E de navegador |
| Docker | construcción, falta smoke HTTP iniciado |

## Matriz de reglas de negocio

| Regla | Estado |
|---|---|
| cálculo de equivalencias | cubierta |
| grupos de medias/ropa interior | cubierta |
| límites por unidad y peso | cubierta |
| ciclo exclusivo en pedido | modelado y probado parcialmente |
| precios y vigencia | cubierta parcialmente |
| primera compra | cubierta |
| una promoción por domicilio | implementada; concurrencia pendiente |
| cupos | parcial; falta prueba concurrente |
| pagos parciales y totales | cubierta en integración |
| transiciones | cubierta |
| auditoría | ejercitada indirectamente; falta consulta |
| compatibilidad | pendiente Fase 2 |
| ciclos y máquinas | pendiente Fase 2 |
| costos y margen | pendiente Fase 4 |
| reclamos | pendiente Fase 5 |

## Próximas pruebas prioritarias

1. login bloqueado mediante endpoint real y reloj controlable;
2. refresh, rotación y logout;
3. pago superior al saldo;
4. consumo promocional y carrera concurrente;
5. actualización de WhatsApp duplicado;
6. domicilios versionados;
7. cotización manual y autorización;
8. eliminación lógica;
9. historial de pagos;
10. pruebas React de formularios y permisos;
11. E2E de navegador;
12. smoke test HTTP sobre Compose iniciado;
13. backup y restauración automatizados.

## Criterio de terminado

Una fase no se considera finalizada hasta que:

- compila;
- las migraciones aplican sobre PostgreSQL vacío;
- Hibernate valida el esquema;
- las reglas críticas poseen pruebas;
- frontend ejecuta lint, tests y build con lockfile;
- las imágenes se construyen;
- documentación y backlog reflejan el estado real;
- CI está verde antes de integrar.
