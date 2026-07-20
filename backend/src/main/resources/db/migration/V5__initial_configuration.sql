-- Datos de negocio iniciales. Todos conservan vigencia y pueden ser reemplazados por nuevas versiones.
INSERT INTO zones (id, code, name, locality, active, created_at, updated_at, created_by, updated_by, version) VALUES
('10000000-0000-0000-0000-000000000001', 'MARCOS_PAZ', 'Marcos Paz', 'Marcos Paz', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('10000000-0000-0000-0000-000000000002', 'MARIANO_ACOSTA', 'Mariano Acosta', 'Mariano Acosta', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0);

INSERT INTO service_offerings
(id, code, name, description, max_equivalent_units, max_weight_grams, safe_capacity_grams, requires_quote, valid_from, active, created_at, updated_at, created_by, updated_by, version)
VALUES
('20000000-0000-0000-0000-000000000001', 'ROPA_LISTA_12', 'Ropa Lista 12', 'Lavado, secado y doblado dentro de ruta programada', 12, 2500, 2500, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000002', 'DUO_HOME', 'Dúo mismo domicilio', 'Hasta 24 prendas equivalentes o 5 kg', 24, 5000, 5000, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000003', 'MONTHLY_4', 'Plan mensual 4 pedidos', 'Abono de cuatro pedidos', 12, 2500, 2500, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000004', 'SECOND_BAG', 'Segunda bolsa', 'Segunda bolsa en el mismo domicilio y retiro', 12, 2500, 2500, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000005', 'EXCLUSIVE_CYCLE', 'Ciclo exclusivo', 'Ciclo no compartido', NULL, 5000, 5000, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000006', 'OUT_OF_ROUTE', 'Fuera de ruta', 'Servicio fuera de recorrido programado', 12, 2500, 2500, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000007', 'EXPRESS_24H', 'Express 24 horas', 'Entrega dentro de 24 horas sujeto a capacidad', 12, 2500, 2500, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000008', 'STAIN_TREATMENT', 'Tratamiento de manchas', 'Adicional sujeto a inspección', NULL, NULL, NULL, TRUE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000009', 'IRONING_ITEM', 'Planchado por prenda', 'Precio inicial por prenda', NULL, NULL, NULL, TRUE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000010', 'JACKET', 'Campera', 'Servicio especial', NULL, NULL, NULL, TRUE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('20000000-0000-0000-0000-000000000011', 'COMFORTER', 'Acolchado', 'Servicio separado', NULL, NULL, NULL, TRUE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0);

INSERT INTO garment_equivalences
(id, code, name, category, physical_units_per_group, equivalent_units, estimated_weight_grams, common_wash_allowed, dryer_allowed, exclusive_cycle_required, quote_required, valid_from, active, created_at, updated_at, created_by, updated_by, version)
VALUES
('30000000-0000-0000-0000-000000000001', 'TSHIRT', 'Remera', 'ROPA_COTIDIANA', 1, 1, 180, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000002', 'SHIRT', 'Camisa o blusa', 'ROPA_COTIDIANA', 1, 1, 220, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000003', 'PANTS', 'Pantalón o jean', 'ROPA_COTIDIANA', 1, 1, 600, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000004', 'LEGGING_SHORT', 'Calza o short', 'ROPA_COTIDIANA', 1, 1, 250, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000005', 'LIGHT_DRESS', 'Vestido liviano', 'ROPA_COTIDIANA', 1, 1, 300, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000006', 'LIGHT_SWEATSHIRT', 'Buzo liviano', 'ABRIGO', 1, 1, 500, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000007', 'SOCKS_3_PAIRS', 'Tres pares de medias', 'PEQUENAS', 6, 1, 180, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000008', 'UNDERWEAR_3', 'Tres prendas interiores', 'PEQUENAS', 3, 1, 180, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000009', 'BABY_SMALL_3', 'Tres prendas pequeñas de bebé', 'BEBE', 3, 1, 150, TRUE, TRUE, TRUE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000010', 'HEAVY_SWEATSHIRT', 'Buzo grueso', 'ABRIGO', 1, 2, 850, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000011', 'HEAVY_SWEATER', 'Sweater pesado', 'ABRIGO', 1, 2, 700, TRUE, FALSE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000012', 'LIGHT_JACKET', 'Campera liviana', 'ABRIGO', 1, 2, 900, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000013', 'BATH_TOWEL', 'Toalla de baño', 'BLANQUERIA', 1, 2, 650, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000014', 'HEAVY_JACKET', 'Campera gruesa', 'ABRIGO', 1, 3, 1400, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000015', 'BATH_SHEET', 'Toallón', 'BLANQUERIA', 1, 3, 900, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000016', 'VOLUMINOUS_COAT', 'Camperón voluminoso', 'ESPECIAL', 1, 3, 1800, FALSE, FALSE, TRUE, TRUE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000017', 'SINGLE_SHEET', 'Sábana individual', 'BLANQUERIA', 1, 2, 700, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000018', 'DOUBLE_SHEET', 'Sábana doble', 'BLANQUERIA', 1, 3, 900, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000019', 'KING_SHEET', 'Sábana king', 'BLANQUERIA', 1, 4, 1100, TRUE, TRUE, FALSE, FALSE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000020', 'COMFORTER', 'Acolchado', 'SERVICIO_SEPARADO', 1, 1, NULL, FALSE, FALSE, TRUE, TRUE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('30000000-0000-0000-0000-000000000021', 'HEAVY_BLANKET', 'Frazada pesada', 'SERVICIO_SEPARADO', 1, 1, NULL, FALSE, FALSE, TRUE, TRUE, CURRENT_DATE, TRUE, NOW(), NOW(), 'flyway', 'flyway', 0);

INSERT INTO price_definitions
(id, service_id, amount, currency_code, valid_from, reason, active, created_at, updated_at, created_by, updated_by, version)
VALUES
('40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 6500, 'ARS', NOW(), 'Precio inicial de referencia', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 12000, 'ARS', NOW(), 'Precio inicial de referencia', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 24000, 'ARS', NOW(), 'Precio inicial de referencia', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 5800, 'ARS', NOW(), 'Precio inicial de referencia', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 8500, 'ARS', NOW(), 'Precio inicial de referencia', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 8000, 'ARS', NOW(), 'Precio inicial de referencia', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 9500, 'ARS', NOW(), 'Precio inicial de referencia', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 1500, 'ARS', NOW(), 'Precio desde; requiere confirmación manual', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 800, 'ARS', NOW(), 'Precio desde; requiere confirmación manual', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 5000, 'ARS', NOW(), 'Precio desde; requiere confirmación manual', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('40000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 11000, 'ARS', NOW(), 'Precio desde; requiere confirmación manual', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0);

INSERT INTO promotions
(id, code, name, description, valid_from, valid_to, total_quota, daily_quota, monthly_quota, new_customers_only, automatic_applicable, applicable_service_code, one_per_address, stackable, fixed_price, percentage_discount, credit_amount, status, created_at, updated_at, created_by, updated_by, version)
VALUES
('50000000-0000-0000-0000-000000000001', 'FIRST_TRIAL', 'Primera prueba', '12 prendas por precio promocional', NOW(), NULL, NULL, NULL, NULL, TRUE, TRUE, 'ROPA_LISTA_12', TRUE, FALSE, 5500, NULL, NULL, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0),
('50000000-0000-0000-0000-000000000002', 'DIRECT_COMPETITION', 'Competencia directa', 'Hasta 25 prendas o 5 kg', NOW(), NULL, NULL, NULL, NULL, FALSE, FALSE, NULL, TRUE, FALSE, 6900, NULL, NULL, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0),
('50000000-0000-0000-0000-000000000003', 'NEIGHBORS', 'Promo vecinos', 'Precio promocional por domicilio', NOW(), NULL, NULL, NULL, NULL, FALSE, FALSE, NULL, TRUE, FALSE, 5800, NULL, NULL, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0),
('50000000-0000-0000-0000-000000000004', 'HOME_DUO', 'Dúo del mismo hogar', '24 prendas', NOW(), NULL, NULL, NULL, NULL, FALSE, TRUE, 'DUO_HOME', TRUE, FALSE, 11500, NULL, NULL, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0),
('50000000-0000-0000-0000-000000000005', 'FOUNDER_PLAN', 'Plan fundador', 'Cuatro pedidos durante sesenta días', NOW(), NOW() + INTERVAL '60 days', NULL, NULL, NULL, TRUE, TRUE, 'MONTHLY_4', TRUE, FALSE, 23000, NULL, NULL, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0),
('50000000-0000-0000-0000-000000000006', 'SECOND_PURCHASE', 'Segunda compra', 'Segundo pedido a precio promocional', NOW(), NULL, NULL, NULL, NULL, FALSE, FALSE, NULL, FALSE, FALSE, 5800, NULL, NULL, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0),
('50000000-0000-0000-0000-000000000007', 'REFERRAL', 'Recomendación', 'Crédito para referente y referido', NOW(), NULL, NULL, NULL, NULL, FALSE, FALSE, NULL, FALSE, FALSE, NULL, NULL, 500, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0),
('50000000-0000-0000-0000-000000000008', 'SECOND_BAG', 'Segunda bolsa', 'Segunda bolsa en el mismo retiro', NOW(), NULL, NULL, NULL, NULL, FALSE, TRUE, 'SECOND_BAG', TRUE, FALSE, 5800, NULL, NULL, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0),
('50000000-0000-0000-0000-000000000009', 'FULL_ROUTE', 'Ruta completa', 'Descuento sujeto a mínimo de domicilios', NOW(), NULL, NULL, NULL, NULL, FALSE, FALSE, NULL, FALSE, FALSE, NULL, 0.10, NULL, 'ACTIVE', NOW(), NOW(), 'flyway', 'flyway', 0);

INSERT INTO payment_methods (id, code, name, active, created_at, updated_at, created_by, updated_by, version) VALUES
('60000000-0000-0000-0000-000000000001', 'CASH', 'Efectivo', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('60000000-0000-0000-0000-000000000002', 'TRANSFER', 'Transferencia', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('60000000-0000-0000-0000-000000000003', 'MERCADO_PAGO', 'Mercado Pago', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0),
('60000000-0000-0000-0000-000000000004', 'OTHER', 'Otro', TRUE, NOW(), NOW(), 'flyway', 'flyway', 0);
