# Supuestos explícitos

Versión: `0.3.0`.

## Comerciales

1. La moneda inicial es ARS, conservando código ISO.
2. Las equivalencias admiten decimales.
3. Tres pares de medias son seis piezas físicas.
4. Los límites exactos son admitidos; se exceden al superar el valor.
5. Pedidos fuera de regla pueden requerir presupuesto manual.
6. Promociones complejas pueden existir sin aplicación automática.
7. Un pago registrado es un cobro operativo, no un movimiento contable completo.

## Clientes y domicilios

1. El WhatsApp identifica de forma única a un cliente activo.
2. Un cliente activo debe conservar al menos un domicilio activo.
3. Existe un único domicilio principal activo.
4. Cambiar o desactivar un domicilio no altera pedidos históricos.
5. Las preferencias JSON persistidas son válidas y tipadas por el servicio de clientes.

## Pedido declarado

1. La composición declarada representa la información previa al retiro.
2. Piezas físicas y unidades equivalentes son magnitudes distintas.
3. El peso declarado puede ser estimado o desconocido.
4. `exclusiveCycle` declarado no puede relajarse en etapas posteriores.
5. Los estados posteriores a `CLASSIFIED` no implican ejecución física mientras no exista producción.

## Recepción

1. Solo existe una recepción física por pedido.
2. La repetición legítima usa la misma `Idempotency-Key`.
3. El snapshot real no sobrescribe la declaración.
4. Todos los códigos declarados deben aparecer en la recepción, incluso con cantidad cero si corresponde.
5. Prendas adicionales requieren equivalencia vigente.
6. Una diferencia material de peso es mayor a 250 g o al 10 % declarado.
7. Daño obliga aprobación; mancha sola no.
8. La aprobación es una decisión operativa, no firma digital del cliente.
9. Las evidencias son metadata de objetos externos, no binarios almacenados.

## Compatibilidad

1. Solo se evalúan pedidos distintos en `CLASSIFIED` y con recepción/perfil.
2. El perfil representa condiciones de tratamiento del pedido recibido, no preferencias generales del cliente.
3. El request puede ser endurecido por preferencias del cliente o por el pedido.
4. Una prohibición del cliente prevalece sobre una opción permisiva del formulario.
5. `hypoallergenic=true` implica fragancia `NONE` en el perfil efectivo.
6. `exclusiveCycle=true` prevalece si lo exige el pedido, el cliente o el formulario.
7. `UNKNOWN` en color bloquea combinación.
8. Grupos de color distintos bloquean combinación en `COMPAT-1`.
9. Algodón, sintético y mixto pueden combinarse según las reglas codificadas; delicado y lana requieren coincidencia compatible.
10. Cargas hipoalergénicas no se mezclan con estándar.
11. Ropa de bebé no se mezcla con contacto de mascotas.
12. Suciedad pesada no se mezcla con cargas sensibles.
13. Políticas de fragancia distintas bloquean combinación.
14. Diferencias de temperatura, secadora o suavizante pueden resolverse usando la opción más restrictiva y generan advertencia.
15. La evaluación es por pares, no optimiza un conjunto de pedidos.
16. El motor `COMPAT-1` es determinista.
17. La identidad histórica incluye versiones de perfiles y reglas.
18. Modificar un perfil produce una evaluación nueva; no reescribe la anterior.
19. Una excepción no cambia `compatible`, razones ni recomendación.
20. `effectivelyCompatible` significa resultado original compatible o excepción existente.
21. Una excepción no asigna máquina ni garantiza capacidad.

## Concurrencia

1. El orden UUID de bloqueo es estable para evitar interbloqueos al evaluar A/B y B/A.
2. Los constraints únicos son última defensa, no el mecanismo normal del flujo.
3. Los bloqueos pesimistas son aceptables por el volumen operativo inicial.
4. Si el volumen crece, deberán medirse contención y tiempos de espera antes de migrar a otro esquema.

## Seguridad

1. Compose es solo desarrollo/evaluación.
2. El administrador inicial no constituye gestión productiva de usuarios.
3. El rate limit local no protege múltiples instancias.
4. La auditoría ayuda a trazabilidad, pero no sustituye controles legales o firma digital.
5. Las imágenes futuras pueden contener datos personales y requieren política específica.

## Próxima fase

1. Compatibilidad efectiva será un requisito para compartir ciclo.
2. Ciclo y máquina serán agregados independientes.
3. La capacidad se medirá en gramos y no podrá superarse.
4. Un pedido exclusivo nunca compartirá ciclo aunque exista excepción de compatibilidad entre perfiles.
5. La excepción deberá ser considerada explícitamente por producción, no inferida de forma silenciosa.
