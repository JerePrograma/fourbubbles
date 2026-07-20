# Supuestos explícitos

Versión: `0.2.0`.

## Comerciales

1. Moneda inicial ARS, conservando código ISO.
2. Equivalencias admiten decimales.
3. Tres pares de medias son seis piezas físicas.
4. Límites exactos son admitidos; se exceden al superar el valor.
5. Pedidos fuera de regla pueden requerir presupuesto manual.
6. Promociones complejas pueden existir sin aplicación automática.
7. Primera compra se evalúa contra pedidos no eliminados.
8. La cotización no reserva promoción; confirmación sí.

## Cliente/domicilio

9. Cliente nuevo requiere exactamente un principal.
10. Debe conservar un domicilio activo.
11. Baja no modifica pedidos históricos.
12. Zona se valida por código, no geocodificación.

## Pedido/precio

13. Declaración, recepción y producción son snapshots distintos.
14. Precio automático, cotizado y confirmado son distintos.
15. Confirmación congela el valor.
16. Planificación temprana solo en `INQUIRY`/`QUOTED`.
17. Estados posteriores no prueban que el módulo físico exista.

## Recepción

18. Solo se recibe desde `PICKED_UP`.
19. Existe una recepción base por pedido.
20. Una corrección futura deberá versionarse; no sobrescribirá silenciosamente.
21. `Idempotency-Key` identifica el intento lógico, no una solicitud HTTP aislada.
22. Un retry debe reutilizar la clave.
23. Todos los códigos declarados deben informarse, incluso con real cero.
24. Se permiten códigos adicionales si la equivalencia está vigente.
25. El total real debe ser mayor a cero.
26. La fecha puede tolerar hasta cinco minutos de diferencia futura por reloj.
27. Daño exige aprobación.
28. Diferencia de piezas exige aprobación.
29. Peso exige aprobación si supera 250 g o 10 %.
30. Mancha sola se registra sin obligar a aprobación.
31. Aprobación es decisión operativa autenticada, no firma digital del cliente.
32. Rechazo cancela el pedido en este corte.
33. Etiqueta es identificador lógico; no implica impresión física.
34. Bolsa es texto libre hasta implementar inventario/trazabilidad.

## Evidencias

35. 0.2.0 registra metadata de un objeto ya existente.
36. No se verifica presencia binaria, MIME ni hash contra almacenamiento.
37. `objectKey` debe ser único y no contener secretos de acceso.
38. El hash es SHA-256 hexadecimal.
39. Object storage, URLs firmadas, antivirus y retención quedan pendientes.

## Pagos

40. Pagos internos son registros manuales, no conciliación automática.
41. Total pagado considera estado `PAID`.
42. Bloqueo por pedido serializa cobros de esta aplicación.
43. Proveedores externos requerirán idempotencia independiente.

## Seguridad/operación

44. Refresh y cookie usan la misma duración parametrizada.
45. IP observada depende del proxy.
46. Limitador de login local sirve para una instancia.
47. Administrador `dev` se crea solo si no existe.
48. Cambiar `.env` no cambia usuario persistido.
49. Compose es local, no productivo.
50. V1–V7 no se editan después de aplicarse.

## Próxima fase

51. Compatibilidad consumirá datos reales de recepción.
52. Cuando no exista recepción no se asignará un ciclo productivo real.
53. Reglas de compatibilidad deben ser versionadas y explicables.
54. Excepciones deberán ser exclusivas de administrador y auditadas.
