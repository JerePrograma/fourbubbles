# Guía de uso funcional

Versión: `0.3.0`.

Esta guía describe funciones realmente disponibles. Compatibilidad no equivale a crear o ejecutar un ciclo de lavado.

## 1. Iniciar sesión

Abrir `http://localhost:8080` y usar las credenciales generadas por `Start-Local.ps1`.

## 2. Roles

| Rol | Alcance principal |
|---|---|
| ADMIN | todas las funciones, cotización manual, auditoría y excepciones |
| OPERATOR | clientes, pedidos, recepción, perfiles, evaluación y pagos |
| DRIVER | consulta operativa y registro de recepción |
| REPORT_VIEWER | consultas de pedidos, perfiles y evaluaciones |

## 3. Crear cliente

Ir a **Clientes → Nuevo cliente**.

Completar:

- nombre y apellido;
- teléfono y WhatsApp;
- correo opcional;
- origen;
- preferencias;
- notas;
- domicilio principal.

El WhatsApp debe ser único entre clientes activos.

## 4. Administrar domicilios

En **Editar cliente** se puede:

- agregar domicilio;
- marcar otro como principal;
- desactivar un domicilio no principal;
- revisar historial.

El sistema conserva el domicilio asociado a pedidos anteriores.

## 5. Crear pedido

Ir a **Pedidos → Nuevo pedido**.

Seleccionar:

- cliente;
- domicilio;
- servicio;
- promoción opcional;
- tipos de prenda y cantidades;
- peso declarado opcional;
- ciclo exclusivo si corresponde;
- retiro y promesa;
- notas.

El sistema calcula piezas, unidades equivalentes, límites y precio automático.

## 6. Cotizar y confirmar

En el detalle del pedido:

1. revisar el precio automático;
2. como `ADMIN`, aplicar cotización manual con motivo si corresponde;
3. confirmar el precio;
4. actualizar retiro/promesa antes del bloqueo;
5. avanzar por estados permitidos.

Flujo previo a recepción:

```text
INQUIRY/QUOTED
→ WAITING_CONFIRMATION
→ RESERVED
→ PICKUP_SCHEDULED
→ PICKED_UP
```

## 7. Registrar recepción

En **Pedidos**, abrir **Recibir** o **Recepción**.

Solo se registra desde `PICKED_UP`.

Completar:

- fecha real;
- peso real;
- cantidad real por código de prenda;
- daño;
- mancha;
- observaciones;
- bolsa opcional;
- metadata de evidencia opcional.

No inventar evidencia. La aplicación solo almacena referencia, nombre, MIME, tamaño y hash; el archivo debe existir en un almacenamiento externo real.

## 8. Resultado de recepción

Sin diferencias materiales:

```text
PICKED_UP
→ RECEIVED
→ PENDING_INSPECTION
→ CLASSIFIED
```

Con diferencia de piezas, daño o peso mayor a 250 g/10 %:

```text
PICKED_UP
→ RECEIVED
→ PENDING_INSPECTION
→ WAITING_PRICE_APPROVAL
```

Un `ADMIN` u `OPERATOR` decide:

- aprobar → `CLASSIFIED`;
- rechazar → `CANCELLED`.

## 9. Crear perfil de compatibilidad

Cuando el pedido está `CLASSIFIED`, abrir **Compatibilidad**.

Completar:

- grupo de color;
- grupo de material;
- temperatura máxima;
- secadora;
- fragancia;
- suavizante;
- hipoalergénico;
- ropa de bebé;
- contacto con mascotas;
- suciedad pesada;
- ciclo exclusivo;
- notas.

Guardar como `ADMIN` u `OPERATOR`.

### Importante

La respuesta puede ser más restrictiva que el formulario. El backend conserva preferencias del cliente y condiciones del pedido:

- una prohibición de secadora o suavizante no se puede activar;
- hipoalergénico fuerza fragancia `NONE`;
- exclusividad no se puede desmarcar si ya era obligatoria.

Revisar el perfil devuelto después de guardar.

## 10. Preparar el segundo pedido

La comparación requiere otro pedido:

- distinto;
- en `CLASSIFIED`;
- con recepción;
- con perfil guardado.

Abrir la compatibilidad del segundo pedido y guardar su perfil.

## 11. Evaluar compatibilidad

Volver al primer pedido:

1. seleccionar el candidato;
2. pulsar **Evaluar compatibilidad**;
3. revisar el resultado;
4. leer todas las razones;
5. revisar la recomendación.

### Resultado compatible

`compatible=true` y `effectivelyCompatible=true`.

No existen razones `HARD`. Puede haber advertencias que reduzcan temperatura o deshabiliten secadora/suavizante.

### Resultado bloqueado

`compatible=false` y `effectivelyCompatible=false`.

Razones duras posibles:

- `EXCLUSIVE_CYCLE_REQUIRED`;
- `UNKNOWN_COLOR`;
- `COLOR_GROUP_MISMATCH`;
- `MATERIAL_GROUP_MISMATCH`;
- `HYPOALLERGENIC_ISOLATION`;
- `BABY_PET_CROSS_CONTAMINATION`;
- `HEAVY_SOIL_SENSITIVE_LOAD`;
- `FRAGRANCE_POLICY_MISMATCH`.

### Recomendación

Muestra:

- temperatura máxima común;
- secadora permitida o no;
- suavizante permitido o no;
- política de fragancia;
- programa `NORMAL` o `GENTLE`;
- modo `SHARED` o `BLOCKED`.

## 12. Autorizar una excepción

Solo `ADMIN` y solo para una evaluación originalmente incompatible.

1. escribir un motivo concreto;
2. pulsar **Autorizar excepción**;
3. verificar actor y fecha.

La excepción no elimina las razones ni cambia `compatible`. El resultado pasa a `effectivelyCompatible=true` y queda auditado.

No usar excepciones para tapar perfiles incorrectos. Corregir el perfil y reevaluar si los datos estaban mal.

## 13. Versionado de evaluaciones

Si se evalúa el mismo par sin cambios, se reutiliza el mismo snapshot.

Si se modifica un perfil:

- aumenta su versión;
- la próxima evaluación crea un snapshot nuevo;
- la evaluación anterior permanece consultable por ID;
- una excepción anterior no se transfiere a la nueva evaluación.

## 14. Pagos

En el pedido con precio confirmado:

1. seleccionar medio;
2. ingresar importe;
3. informar referencia/notas;
4. registrar.

El backend impide sobrepago, incluso ante solicitudes concurrentes.

## 15. Auditoría

Como `ADMIN`, abrir **Auditoría**.

Entidades relevantes:

- `CLIENT`;
- `ADDRESS`;
- `ORDER`;
- `ORDER_RECEPTION`;
- `PAYMENT`;
- `TREATMENT_PROFILE`;
- `COMPATIBILITY_EVALUATION`.

Acciones de compatibilidad:

- `CREATE`/`UPDATE` del perfil;
- `CREATE` de evaluación;
- `AUTHORIZE_EXCEPTION`.

## 16. Qué no hace todavía

- no crea ciclos;
- no elige máquina;
- no suma capacidad;
- no inicia lavado o secado;
- no registra control de calidad real;
- no arma rutas;
- no calcula costos o margen;
- no almacena fotos.

Los estados posteriores pueden cambiar administrativamente, pero no sustituyen módulos físicos pendientes.
