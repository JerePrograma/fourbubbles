# Changelog

## 0.2.0 - Recepción física idempotente

Fecha: 2026-07-20.

### Agregado

- Migración Flyway V7.
- Agregado `OrderReception`, ítems reales y metadatos de evidencias.
- `Idempotency-Key` obligatorio y único.
- Peso y conteo reales sin sobrescribir la declaración original.
- Diferencias por pedido y por tipo de prenda.
- Daños, manchas, observaciones, etiqueta y bolsa.
- Política de aprobación por diferencia de piezas, daño o peso material.
- Umbral de peso: mayor a 250 g o 10 % del declarado.
- Decisión `APPROVED`/`REJECTED` con actor, fecha y notas.
- Transiciones automáticas desde `PICKED_UP` hasta `CLASSIFIED` o `WAITING_PRICE_APPROVAL`.
- Metadatos de archivo: clave, nombre, MIME, tamaño, SHA-256 y descripción.
- UI de recepción, consulta y decisión.
- Pruebas unitarias de umbral.
- Integración de idempotencia secuencial y concurrente, aprobación y permisos.

### Corregido o endurecido

- Una recepción repetida con la misma clave devuelve el mismo agregado.
- Otra clave no puede crear una segunda recepción del pedido.
- La composición real se deriva de los ítems y exige todos los códigos declarados.
- Códigos adicionales solo se admiten si su equivalencia está vigente.
- Fecha futura, recepción vacía y duplicados se rechazan.
- Las evidencias guardan metadatos; no se insertan binarios en PostgreSQL.

### Pendiente

- almacenamiento gestionado y carga binaria de fotos;
- compatibilidad;
- ciclos y máquinas;
- logística;
- caja, costos y rentabilidad;
- inventario y reclamos completos.

## 0.1.2 - Cierre administrativo de Fase 1

Fecha: 2026-07-20.

- Domicilios versionados e historial.
- Cotización manual trazable.
- Planificación temprana controlada.
- Promociones y pagos con control de concurrencia.
- Historial financiero y auditoría.
- Jerarquía RBAC.
- Verificación local autenticada.

## 0.1.1 - Hardening y flujo operativo

Fecha: 2026-07-20.

- Contratos de seguridad, correlación y protección de login.
- Preferencias tipadas.
- UI operativa de clientes, pedidos, estados y pagos.

## 0.1.0 - Plataforma y núcleo inicial

Fecha: 2026-07-20.

- Java/Spring, React, PostgreSQL, Flyway, seguridad, catálogo, pedidos, pagos, Docker y CI.
