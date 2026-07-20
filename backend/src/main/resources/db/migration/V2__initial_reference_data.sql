insert into zone (id, code, name, locality, active, valid_from, created_at, updated_at, version)
values
('10000000-0000-0000-0000-000000000001', 'MARCOS_PAZ', 'Marcos Paz', 'Marcos Paz', true, date '2026-01-01', now(), now(), 0),
('10000000-0000-0000-0000-000000000002', 'MARIANO_ACOSTA', 'Mariano Acosta', 'Mariano Acosta', true, date '2026-01-01', now(), now(), 0);

insert into service_plan (
    id, code, name, description, max_equivalent_units, max_weight_grams, active,
    created_at, updated_at, version
) values
('20000000-0000-0000-0000-000000000001', 'ROPA_LISTA_12', 'Ropa Lista 12',
 'Lavado, secado, doblado, retiro y entrega en ruta programada. Aplica el primer límite alcanzado.',
 12.00, 2500, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000002', 'DUO_DOMICILIO', 'Dúo mismo domicilio',
 'Hasta 24 unidades equivalentes o 5 kg para un mismo domicilio.', 24.00, 5000, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000003', 'PLAN_MENSUAL_4', 'Plan mensual de 4 pedidos',
 'Cuatro pedidos de hasta 12 unidades equivalentes o 2,5 kg.', 12.00, 2500, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000004', 'SEGUNDA_BOLSA', 'Segunda bolsa mismo domicilio',
 'Segunda bolsa incluida en el mismo retiro.', 12.00, 2500, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000005', 'CICLO_EXCLUSIVO', 'Ciclo exclusivo',
 'Ciclo no compartido por preferencia, alergia o incompatibilidad.', null, 8500, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000006', 'FUERA_DE_RUTA', 'Fuera de ruta',
 'Servicio con logística fuera de una ruta programada.', 12.00, 2500, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000007', 'EXPRESS_24H', 'Express 24 horas',
 'Servicio prioritario sujeto a capacidad operativa.', 12.00, 2500, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000008', 'TRATAMIENTO_MANCHAS', 'Tratamiento de manchas',
 'Adicional sujeto a evaluación.', null, null, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000009', 'PLANCHADO_PRENDA', 'Planchado por prenda',
 'Precio unitario desde el valor vigente.', null, null, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000010', 'CAMPERA', 'Campera',
 'Servicio especial para campera.', null, null, true, now(), now(), 0),
('20000000-0000-0000-0000-000000000011', 'ACOLCHADO', 'Acolchado',
 'Servicio separado sujeto a tamaño y material.', null, null, true, now(), now(), 0);

insert into price_version (
    id, service_plan_id, amount, currency, channel, modality, valid_from, reason,
    created_at, updated_at, version
) values
('30000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001',6500.00,'ARS','DIRECT','ROUTE',date '2026-01-01','Precio inicial de referencia',now(),now(),0),
('30000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000002',12000.00,'ARS','DIRECT','ROUTE',date '2026-01-01','Precio inicial de referencia',now(),now(),0),
('30000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000003',24000.00,'ARS','DIRECT','SUBSCRIPTION',date '2026-01-01','Precio inicial de referencia',now(),now(),0),
('30000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000004',5800.00,'ARS','DIRECT','SAME_PICKUP',date '2026-01-01','Precio inicial de referencia',now(),now(),0),
('30000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000005',8500.00,'ARS','DIRECT','EXCLUSIVE',date '2026-01-01','Precio inicial de referencia',now(),now(),0),
('30000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000006',8000.00,'ARS','DIRECT','OUT_OF_ROUTE',date '2026-01-01','Precio inicial de referencia',now(),now(),0),
('30000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000007',9500.00,'ARS','DIRECT','EXPRESS',date '2026-01-01','Precio inicial de referencia',now(),now(),0),
('30000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000008',1500.00,'ARS','DIRECT','ADD_ON',date '2026-01-01','Precio mínimo inicial',now(),now(),0),
('30000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000009',800.00,'ARS','DIRECT','PER_ITEM',date '2026-01-01','Precio mínimo inicial',now(),now(),0),
('30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000010',5000.00,'ARS','DIRECT','SPECIAL',date '2026-01-01','Precio mínimo inicial',now(),now(),0),
('30000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000011',11000.00,'ARS','DIRECT','SPECIAL',date '2026-01-01','Precio mínimo inicial',now(),now(),0);

insert into garment_equivalence (
    id, code, name, category, physical_units_per_group, equivalent_units,
    common_wash_allowed, dryer_allowed, exclusive_cycle_required, quote_required,
    active, valid_from, created_at, updated_at, version
) values
('40000000-0000-0000-0000-000000000001','TSHIRT','Remera','ROPA_COTIDIANA',1,1,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000002','SHIRT','Camisa o blusa','ROPA_COTIDIANA',1,1,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000003','PANTS','Pantalón o jean','ROPA_COTIDIANA',1,1,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000004','LEGGINGS_SHORT','Calza o short','ROPA_COTIDIANA',1,1,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000005','LIGHT_DRESS','Vestido liviano','ROPA_COTIDIANA',1,1,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000006','LIGHT_SWEATSHIRT','Buzo liviano','ROPA_COTIDIANA',1,1,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000007','SOCKS_3_PAIRS','Tres pares de medias','PEQUENAS',6,1,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000008','UNDERWEAR_3','Tres prendas interiores','PEQUENAS',3,1,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000009','BABY_SMALL_3','Tres prendas pequeñas de bebé','BEBE',3,1,true,true,true,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000010','HEAVY_SWEATSHIRT','Buzo grueso','PESADAS',1,2,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000011','HEAVY_SWEATER','Sweater pesado','PESADAS',1,2,true,false,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000012','LIGHT_JACKET','Campera liviana','ABRIGO',1,2,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000013','BATH_TOWEL','Toalla de baño','BLANQUERIA',1,2,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000014','HEAVY_JACKET','Campera gruesa','ABRIGO',1,3,false,false,true,true,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000015','BATH_SHEET','Toallón','BLANQUERIA',1,3,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000016','VOLUMINOUS_COAT','Camperón voluminoso','ABRIGO',1,3,false,false,true,true,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000017','SINGLE_SHEET','Sábana individual','BLANQUERIA',1,2,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000018','DOUBLE_SHEET','Sábana doble','BLANQUERIA',1,3,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000019','KING_SHEET','Sábana king','BLANQUERIA',1,4,true,true,false,false,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000020','DUVET','Acolchado','ESPECIAL',1,12,false,false,true,true,true,date '2026-01-01',now(),now(),0),
('40000000-0000-0000-0000-000000000021','HEAVY_BLANKET','Frazada pesada','ESPECIAL',1,12,false,false,true,true,true,date '2026-01-01',now(),now(),0);

insert into promotion (
    id, code, name, description, starts_on, ends_on, total_quota, daily_quota, monthly_quota,
    new_customers_only, one_per_address, stackable, fixed_price, percentage_discount,
    credit_amount, active, created_at, updated_at, version
) values
('50000000-0000-0000-0000-000000000001','FIRST_TRIAL','Primera prueba','12 prendas por precio de prueba',date '2026-01-01',null,null,null,null,true,true,false,5500.00,null,null,true,now(),now(),0),
('50000000-0000-0000-0000-000000000002','DIRECT_COMPETITION','Competencia directa','Hasta 25 prendas o 5 kg',date '2026-01-01',null,null,null,null,false,true,false,6900.00,null,null,true,now(),now(),0),
('50000000-0000-0000-0000-000000000003','NEIGHBORS','Promo vecinos','12 prendas por domicilio',date '2026-01-01',null,null,null,null,false,true,false,5800.00,null,null,true,now(),now(),0),
('50000000-0000-0000-0000-000000000004','HOME_DUO','Dúo del mismo hogar','24 prendas por precio fijo',date '2026-01-01',null,null,null,null,false,true,false,11500.00,null,null,true,now(),now(),0),
('50000000-0000-0000-0000-000000000005','FOUNDER_PLAN','Plan fundador','4 pedidos durante 60 días',date '2026-01-01',date '2026-03-01',null,null,null,true,true,false,23000.00,null,null,false,now(),now(),0),
('50000000-0000-0000-0000-000000000006','SECOND_PURCHASE','Segunda compra','Segundo pedido a precio promocional',date '2026-01-01',null,null,null,null,false,false,false,5800.00,null,null,true,now(),now(),0),
('50000000-0000-0000-0000-000000000007','REFERRAL','Recomendación','$500 de crédito para referente y referido',date '2026-01-01',null,null,null,null,false,false,false,null,null,500.00,true,now(),now(),0),
('50000000-0000-0000-0000-000000000008','SECOND_BAG','Segunda bolsa','Segunda bolsa en el mismo retiro',date '2026-01-01',null,null,null,null,false,false,false,5800.00,null,null,true,now(),now(),0),
('50000000-0000-0000-0000-000000000009','FULL_ROUTE','Ruta completa','Descuento sujeto a mínimo de domicilios',date '2026-01-01',null,null,null,null,false,false,false,null,0.1000,null,true,now(),now(),0);

insert into payment_method(code, name) values
('CASH','Efectivo'),
('TRANSFER','Transferencia'),
('MERCADO_PAGO','Mercado Pago'),
('OTHER','Otro');

insert into equipment(id, equipment_type, brand, model, capacity_grams, status, created_at, updated_at)
values
('60000000-0000-0000-0000-000000000001','WASHER','Drean','10,5 kg',10500,'AVAILABLE',now(),now()),
('60000000-0000-0000-0000-000000000002','DRYER','Candy','9 kg',9000,'AVAILABLE',now(),now());

insert into claim_type(code, name) values
('DELAY','Demora'),('STAIN','Mancha'),('DAMAGE','Daño'),('LOSS','Pérdida'),
('MISSING_ITEM','Prenda faltante'),('ODOR','Olor'),('FOLDING','Doblado'),
('DELIVERY','Entrega'),('CHARGE','Cobro'),('OTHER','Otro');

insert into whatsapp_template(code, name, body) values
('FIRST_INQUIRY','Primera consulta','Hola {{nombre}}, gracias por consultar por Ropa Lista.'),
('SERVICE_EXPLANATION','Explicación del servicio','El servicio incluye lavado, secado, doblado y logística en ruta programada.'),
('PRICES','Precios','El precio se confirma según servicio, peso, equivalencias y vigencia.'),
('ZONE_CONFIRMATION','Confirmación de zona','Confirmamos cobertura para {{zona}} en la ruta {{ruta}}.'),
('RESERVATION','Reserva','Tu pedido {{pedido}} quedó reservado para {{fecha}}.'),
('REMINDER','Recordatorio','Te recordamos el retiro de {{pedido}} para {{fecha}}.'),
('ARRIVAL','Aviso de llegada','Estamos llegando al domicilio para el retiro.'),
('RECEPTION','Confirmación de recepción','Recibimos el pedido {{pedido}}.'),
('WEIGHT_ITEMS','Peso y prendas','Registramos {{peso}} g y {{unidades}} unidades equivalentes.'),
('PRICE_DIFFERENCE','Diferencia de precio','Detectamos una diferencia y necesitamos tu aprobación antes de continuar.'),
('STAINS','Consulta por manchas','Detectamos manchas que requieren evaluación adicional.'),
('ORDER_READY','Pedido terminado','Tu pedido {{pedido}} está listo para entregar.'),
('PAYMENT_REQUEST','Solicitud de pago','El saldo pendiente de {{pedido}} es {{importe}}.'),
('DELIVERY_COORDINATION','Coordinación de entrega','Coordinemos la entrega de {{pedido}}.'),
('FOLLOW_UP','Confirmación posterior','¿Recibiste todo correctamente?'),
('REVIEW','Reseña','Tu opinión nos ayuda a mejorar.'),
('SUBSCRIPTION','Oferta de abono','Tenemos un plan mensual configurable según frecuencia.'),
('PROMOTION','Promoción','Tenemos una promoción vigente sujeta a condiciones y cupo.'),
('OUT_OF_ZONE','Fuera de zona','El domicilio está fuera de la ruta actual. Podemos presupuestar un adicional.'),
('URGENCY','Urgencia','El servicio urgente depende de capacidad disponible.'),
('CLAIM','Reclamo','Registramos tu reclamo y lo estamos analizando.'),
('DELAY','Demora','Informamos una demora en {{pedido}}.'),
('CANCELLATION','Cancelación','El pedido {{pedido}} fue cancelado.'),
('COMPATIBILITY','Compatibilidad','Las bolsas separan prendas, pero los pedidos compatibles comparten agua de lavado.'),
('ALLERGIES','Alergias','Necesitamos confirmar restricciones de productos y ciclo exclusivo.'),
('BABY_CLOTHES','Ropa de bebé','La ropa de bebé se procesa según restricciones configuradas.'),
('COMMERCIAL_CUSTOMER','Cliente comercial','Podemos cotizar frecuencia, volumen y condiciones comerciales.'),
('FORGOTTEN_OBJECTS','Objetos olvidados','Encontramos un objeto en el pedido {{pedido}}.');

insert into service_policy(id, code, version, title, content, valid_from, active)
values
('70000000-0000-0000-0000-000000000001','GENERAL_SERVICE',1,'Políticas generales del servicio',
 'El cliente declara haber revisado bolsillos e informado prendas delicadas, manchas y daños preexistentes. Las bolsas de red separan físicamente las prendas, pero no aíslan el agua compartida.',date '2026-01-01',true);
