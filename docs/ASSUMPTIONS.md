# Supuestos explícitos

Versión de referencia: `0.1.2`.

## Comerciales

1. La moneda inicial es ARS, pero precio y pago conservan código ISO.
2. Las equivalencias pueden admitir decimales aunque varios seeds sean enteros.
3. Tres pares de medias representan seis piezas físicas por grupo.
4. Los límites exactos de 12 unidades o 2.500 g son admitidos; se exceden al superar el valor.
5. La capacidad segura puede exigir división o servicio diferente antes de planificar ciclos.
6. Un pedido fuera de regla puede registrarse para presupuesto manual.
7. Las promociones complejas pueden existir como catálogo con `automatic_applicable=false` sin aplicarse automáticamente.
8. Primera compra se evalúa contra pedidos previos no eliminados.
9. El cálculo de cotización no reserva cupo; la confirmación sí consume bajo bloqueo.
10. Una cotización manual representa una decisión administrativa y exige motivo.

## Clientes y domicilios

11. Al crear un cliente debe existir exactamente un domicilio principal.
12. Cada cliente conserva al menos un domicilio activo.
13. Dar de baja un domicilio no elimina su historia ni cambia pedidos existentes.
14. La zona se valida por código activo, no por geocodificación.
15. Un cambio de principal no implica mudanza retroactiva de pedidos.

## Pedidos

16. El precio automático, el precio cotizado y el confirmado son conceptos diferentes.
17. Confirmar precio congela el valor para pagos posteriores.
18. Retiro, promesa y notas solo son planificación editable durante consulta/cotización.
19. Los estados son enum por su semántica operativa; las transiciones no son texto libre.
20. La presencia de un estado no prueba que el módulo físico correspondiente exista.
21. `RECEIVED` todavía no registra peso, conteo ni evidencia.
22. La combinación de pedidos y las bolsas de red no aíslan el agua; el sistema no afirma lo contrario.

## Pagos

23. Los pagos internos son registros manuales confirmados; no representan conciliación bancaria automática.
24. El total pagado considera pagos con estado `PAID`.
25. Un bloqueo por pedido es suficiente para serializar cobros dentro de esta aplicación.
26. Integraciones externas futuras requerirán clave idempotente propia.

## Seguridad y operación

27. Duración del refresh token y `Max-Age` usan el mismo parámetro.
28. El origen de red observado depende del proxy configurado.
29. El limitador de login en memoria es válido para desarrollo y una instancia, no para escalado horizontal.
30. El perfil `dev` puede crear un administrador solo cuando no existe.
31. Cambiar la contraseña en `.env` no cambia un usuario persistido.
32. Compose representa un entorno local, no un despliegue productivo.
33. Una migración aplicada no se edita; se crea una nueva.

## Próxima fase

34. La recepción se modelará como operación idempotente, no como simple cambio de estado.
35. Fotografías y archivos se almacenarán fuera de PostgreSQL; la base guardará metadatos.
36. Diferencias relevantes de peso, composición o precio necesitarán una decisión explícita.
37. Compatibilidad y ciclos deben consumir datos reales de recepción, no solo declaraciones iniciales.
