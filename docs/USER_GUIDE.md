# Guía de uso funcional

Versión: `0.4.0`.

## 1. Preparar el pedido

1. Crear cliente y domicilio.
2. Crear pedido y confirmar precio.
3. Programar retiro y avanzar a `PICKED_UP`.
4. Registrar recepción.
5. Resolver diferencias hasta `CLASSIFIED`.
6. Guardar perfil de tratamiento.

## 2. Compatibilidad

Para compartir dos pedidos:

1. ambos deben tener perfil;
2. evaluar compatibilidad;
3. revisar `compatible`, `effectivelyCompatible`, razones y recomendación;
4. una excepción solo puede autorizarla `ADMIN`.

Una excepción no borra razones ni exclusividad.

## 3. Abrir Producción

La navegación **Producción** está disponible para usuarios autenticados.

La pantalla permite consultar:

- máquinas;
- programas;
- ciclos;
- estado, etapa, pesos, pedidos e historial.

La administración de máquina/programa por API es solo `ADMIN`. La UI actual prioriza operación base.

## 4. Planificar lavado

Como `ADMIN` u `OPERATOR`:

1. elegir lavadora activa;
2. elegir programa `WASH`;
3. seleccionar uno o dos pedidos;
4. ingresar notas;
5. crear el ciclo.

El cliente genera una clave idempotente. Repetir la misma solicitud no duplica el ciclo.

Dos pedidos requieren evaluación vigente y no exclusividad. Si solo una excepción los habilita, revisar `separationRequired=true`. La aplicación no rastrea la separación física.

## 5. Ejecutar ciclo

- `PLANNED`: puede iniciarse o cancelarse;
- `RUNNING`: puede completarse;
- `COMPLETED`/`CANCELLED`: finales.

Al iniciar lavado: pedidos → `WASHING`.

Al completar:

- con secadora permitida → `WAITING_DRY`;
- sin secadora → `QUALITY_CONTROL`.

## 6. Secado

Para pedidos `WAITING_DRY`:

1. elegir secadora;
2. elegir programa `DRY`;
3. planificar ciclo;
4. iniciar;
5. completar.

Resultado: `QUALITY_CONTROL`.

## 7. Calidad

Abrir el pedido en control de calidad y registrar:

- `PASS`: pasa a `FOLDING`;
- `REWASH`: pasa a `REWASH_REQUIRED`.

La observación es obligatoria. `REWASH_REQUIRED` permite planificar un nuevo lavado.

## 8. Errores frecuentes

- máquina ocupada/no disponible;
- capacidad excedida;
- programa incompatible con perfil;
- pedido ya asignado;
- evaluación no vigente;
- exclusividad;
- clave idempotente reutilizada con otra planificación.

Corregir la causa; no forzar estados desde base o frontend.

## 9. Roles

- `ADMIN`: configuración, operación, excepción y auditoría;
- `OPERATOR`: operación productiva;
- `DRIVER`: consulta;
- `REPORT_VIEWER`: consulta.

`DRIVER` no registra recepción ni modifica ciclos.

## 10. Límites

No hay asignación automática, fraccionamiento, tracking físico de separación, costos, consumos, mantenimiento completo, rutas ni almacenamiento de fotos.
