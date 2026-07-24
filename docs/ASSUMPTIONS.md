# Supuestos explícitos

Versión: `0.4.0`.

## Pedido, recepción y compatibilidad

1. Pedido declarado, recepción real y perfil efectivo son registros distintos.
2. Peso real de recepción es la fuente para capacidad productiva.
3. `exclusiveCycle` nunca se relaja.
4. `COMPAT-1` es determinista e histórico.
5. Una excepción no modifica el resultado original.
6. `effectivelyCompatible` habilita evaluación operativa, no garantiza máquina o capacidad.
7. Evidencias de recepción son metadata, no binarios.
8. La aprobación de diferencias no equivale a firma digital.

## Producción

1. La capacidad se expresa en gramos y el límite exacto es permitido.
2. Un ciclo contiene uno o dos pedidos.
3. Dos pedidos requieren evaluación vigente para las versiones actuales de perfil.
4. Un pedido exclusivo nunca comparte ciclo, aunque exista excepción.
5. Una excepción habilitada marca `separationRequired=true`.
6. `separationRequired` no prueba que existan bolsas, compartimentos o controles físicos.
7. La máquina y el programa deben corresponder a la etapa.
8. Lavado requiere temperatura y fragancia; secado no usa esos parámetros.
9. El programa debe respetar temperatura, suavizante, fragancia, delicadeza y permiso de secadora de todos los pedidos.
10. El peso planificado usa el peso real completo de cada pedido; no existe fraccionamiento.
11. La clave idempotente identifica máquina, programa y conjunto de pedidos; las notas no forman parte de esa identidad.
12. Un pedido no participa en dos ciclos activos de la misma etapa.
13. Un ciclo iniciado no se cancela.
14. Al completar lavado, un pedido sin secadora pasa directamente a calidad.
15. `PASS` lleva a doblado; `REWASH` exige nuevo lavado.
16. Los parámetros técnicos de un programa usado no se reinterpretan.
17. No existe asignación automática óptima ni secado natural como ciclo.

## Concurrencia

1. El volumen inicial admite bloqueos pesimistas.
2. Los pedidos se bloquean por UUID canónico.
3. La misma clave de ciclo se serializa con advisory lock.
4. Constraints e índices son última defensa, no sustituyen la transacción.

## Seguridad y operación

1. Compose es desarrollo/evaluación, no producción.
2. La auditoría técnica no sustituye controles legales.
3. Rate limit local no protege múltiples instancias.
4. Las imágenes futuras requieren privacidad y retención.
5. Flyway `V1`–`V10` es inmutable; cambios futuros usan `V11+`.
