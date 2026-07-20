# Supuestos explícitos

1. La moneda inicial es ARS, pero cada precio/pago conserva código ISO.
2. Las equivalencias pueden admitir decimales aunque los seeds actuales sean enteros.
3. Tres pares de medias equivalen a seis piezas físicas por grupo.
4. Los límites exactos de 12 unidades o 2.500 g son admitidos; se exceden al superar el valor.
5. La capacidad segura del servicio puede bloquear antes de la planificación de ciclos.
6. Un pedido que excede límites puede registrarse para presupuesto, pero no confirmarse automáticamente.
7. Las promociones complejas quedan activas como catálogo, pero `automatic_applicable=false` evita descuentos incorrectos.
8. La promoción de primera compra se evalúa contra pedidos previos no eliminados.
9. La duración del refresh token y el `Max-Age` de la cookie usan el mismo parámetro de configuración.
10. La dirección principal debe ser exactamente una al crear el cliente.
11. La zona se valida por código activo, no por geocodificación.
12. La combinación de pedidos y las bolsas de red no aíslan el agua; esa afirmación no aparece en el sistema.
13. Los estados se modelan como enum porque tienen semántica operacional; las transiciones opcionales se extenderán sin convertir el flujo en texto libre.
