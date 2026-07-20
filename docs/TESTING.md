# Estrategia y estado de pruebas

Última actualización: 2026-07-20.

Versión: `0.2.0`.

## Gate de pull request

### Backend

```bash
cd backend
mvn clean verify
```

Ejecuta Java 21, JUnit/Mockito, MockMvc, Testcontainers PostgreSQL 16, Flyway V1–V7 y validación Hibernate/JPA.

### Frontend

```bash
cd frontend
npm ci
npm run lint
npm test
npm run build
```

### Contenedores

```bash
docker compose config --quiet
docker compose build
```

### Runtime

`runtime-smoke.yml` inicia stack, espera readiness, carga SPA, inicia sesión y consulta una API protegida.

## Suite backend

### Unitarias existentes

- precios y promociones;
- equivalencias;
- límites;
- transiciones;
- intentos de login.

### `ReceptionDifferencePolicyTest`

Comprueba:

- variación pequeña sin aprobación;
- umbral absoluto/relativo de peso;
- diferencia de piezas;
- daño detectado.

### Integración administrativa

- contratos 401/403/400;
- clientes y domicilios;
- pedidos, precio y estados;
- promociones concurrentes;
- pagos concurrentes;
- auditoría.

### `ReceptionFlowIT`

Casos:

1. recepción normal clasifica el pedido;
2. retry secuencial con la misma clave devuelve el mismo ID;
3. otra clave sobre el mismo pedido responde conflicto;
4. diferencia material queda pendiente;
5. aprobación mueve a `CLASSIFIED`;
6. evidencia metadata se conserva;
7. dos solicitudes concurrentes con la misma clave reciben 200 y el mismo ID;
8. `DRIVER` no registra recepción;
9. `DRIVER` sí consulta recepción.

## Riesgos cubiertos

### Idempotencia

No se prueba solo la repetición secuencial. También se lanzan dos requests simultáneos contra el mismo pedido y clave.

### Estado

La prueba crea un pedido real, confirma precio y atraviesa:

```text
WAITING_CONFIRMATION → RESERVED → PICKUP_SCHEDULED → PICKED_UP
```

La recepción valida las transiciones automáticas posteriores.

### Persistencia

Testcontainers valida:

- V7;
- constraints de una recepción por pedido;
- constraints de claves/etiquetas;
- mapeos de tres nuevas entidades;
- auditoría JPA.

### Autorización

Los payloads son válidos para que las pruebas midan realmente 403 y no fallen antes por 400.

## Frontend

- TypeScript estricto;
- build de la pantalla de recepción;
- tipos de recepción/decisión;
- envío de cabecera `Idempotency-Key`;
- renderizado de diferencias y evidencias.

El cálculo decisivo de aprobación permanece en backend; React solo presenta el resultado.

## Verificación local

```powershell
.\scripts\Start-Local.ps1 -Rebuild
.\scripts\Verify-Local.ps1
```

Requiere siete migraciones o más y comprueba autenticación/API protegida.

## Pendiente de pruebas futuras

### Evidencias binarias

- carga incompleta;
- hash incorrecto;
- MIME falso;
- archivo demasiado grande;
- malware;
- permisos de lectura;
- borrado/retención.

### Compatibilidad

- matrices;
- explicación;
- excepciones;
- combinaciones críticas.

### Producción

- capacidad de ciclo;
- carreras de asignación;
- fallas de máquina;
- relavado.

### Logística

- solapamiento;
- rutas;
- retiro/entrega idempotentes.

### Pagos externos

- webhooks duplicados;
- conciliación;
- reembolsos.

## Criterio de release

No se fusiona cuando:

- falla un job;
- queda diagnóstico temporal;
- V7 no aplica en PostgreSQL real;
- frontend no compila;
- runtime no inicia/autentica;
- documentación afirma carga de archivos inexistente;
- idempotencia concurrente no está probada.
