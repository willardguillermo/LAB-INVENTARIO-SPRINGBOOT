-- =====================================================================
-- Datos de demostración del módulo de inventario hospitalario.
--
-- NO se ejecuta automáticamente: Spring Boot solo carga schema.sql y
-- data.sql, y este archivo tiene otro nombre a propósito.
--
-- Requisito: haber arrancado la app al menos una vez para que Hibernate
-- cree las tablas. Carga manual (XAMPP, puerto 3306):
--
--   C:\xampp\mysql\bin\mysql.exe -uroot -P3306 --default-character-set=utf8mb4 inventario_hospital < src\main\resources\datos-demo.sql
--
-- Se puede volver a ejecutar: primero borra solo sus propias filas
-- (identificadas por código de producto, RUC o nombre exacto) y después
-- las vuelve a insertar. No toca otros datos.
-- Proveedores ficticios.
-- =====================================================================

SET NAMES utf8mb4;
START TRANSACTION;

-- ---------- limpieza de una carga anterior ----------
DELETE FROM productos WHERE codigo IN (
    'MED-001', 'MED-002', 'MED-003', 'MED-004', 'MED-005', 'MED-006',
    'INS-001', 'INS-002', 'INS-003', 'INS-004', 'INS-005', 'INS-006',
    'MAT-001', 'MAT-002', 'MAT-003', 'MAT-004');

-- Solo se borran si ningún otro producto las usa.
-- 'Equipos de protección personal' es el nombre anterior de 'Bioseguridad'.
DELETE c FROM categoria c
WHERE c.nombre IN ('Medicamentos', 'Soluciones y antisépticos', 'Bioseguridad',
                   'Equipos de protección personal', 'Material de curación', 'Insumos médicos')
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.categoria_id = c.id);

DELETE u FROM unidad_medida u
WHERE u.abreviatura IN ('UND', 'CJA', 'FCO', 'AMP', 'VIA', 'PQT')
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.unidad_medida_id = u.id);

DELETE pr FROM proveedor pr
WHERE pr.ruc IN ('20601234561', '20512345672', '20487654323')
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.proveedor_id = pr.id);

-- ---------- categorías ----------
INSERT INTO categoria (nombre, descripcion, estado) VALUES
('Medicamentos', 'Fármacos de uso hospitalario en sus distintas presentaciones', 1),
('Soluciones y antisépticos', 'Soluciones parenterales, desinfectantes y antisépticos', 1),
('Bioseguridad', 'Guantes, mascarillas, batas y demás equipos de protección personal', 1),
('Material de curación', 'Gasas, apósitos, vendas y esparadrapos', 1),
('Insumos médicos', 'Jeringas, catéteres, equipos de venoclisis y similares', 1);

-- ---------- unidades de medida ----------
INSERT INTO unidad_medida (nombre, abreviatura, estado) VALUES
('Unidad', 'UND', 1),
('Caja', 'CJA', 1),
('Frasco', 'FCO', 1),
('Ampolla', 'AMP', 1),
('Vial', 'VIA', 1),
('Paquete', 'PQT', 1);

-- ---------- proveedores (ficticios) ----------
INSERT INTO proveedor (ruc, razon_social, contacto, telefono, email, direccion, estado) VALUES
('20601234561', 'Distribuidora Médica Andina S.A.C.', 'Rosa Quispe', '01 456 7890',
 'ventas@medicaandina.example', 'Av. Arequipa 1450, Lima', 'ACTIVO'),
('20512345672', 'Farmacéutica del Pacífico S.A.', 'Jorge Salazar', '01 321 6540',
 'pedidos@farmapacifico.example', 'Jr. Huallaga 820, Lima', 'ACTIVO'),
('20487654323', 'Insumos Hospitalarios Lima E.I.R.L.', 'Carmen Torres', '01 789 1234',
 'contacto@insumoslima.example', 'Av. Colonial 2310, Callao', 'ACTIVO');

-- ---------- productos ----------
-- Se usan variables con los ids recién creados (o los existentes, si no se borraron)
SET @cat_med  = (SELECT id FROM categoria WHERE nombre = 'Medicamentos' ORDER BY id LIMIT 1);
SET @cat_sol  = (SELECT id FROM categoria WHERE nombre = 'Soluciones y antisépticos' ORDER BY id LIMIT 1);
SET @cat_bio  = (SELECT id FROM categoria WHERE nombre = 'Bioseguridad' ORDER BY id LIMIT 1);
SET @cat_cur  = (SELECT id FROM categoria WHERE nombre = 'Material de curación' ORDER BY id LIMIT 1);
SET @cat_ins  = (SELECT id FROM categoria WHERE nombre = 'Insumos médicos' ORDER BY id LIMIT 1);

SET @und = (SELECT id FROM unidad_medida WHERE abreviatura = 'UND' ORDER BY id LIMIT 1);
SET @cja = (SELECT id FROM unidad_medida WHERE abreviatura = 'CJA' ORDER BY id LIMIT 1);
SET @fco = (SELECT id FROM unidad_medida WHERE abreviatura = 'FCO' ORDER BY id LIMIT 1);
SET @amp = (SELECT id FROM unidad_medida WHERE abreviatura = 'AMP' ORDER BY id LIMIT 1);
SET @via = (SELECT id FROM unidad_medida WHERE abreviatura = 'VIA' ORDER BY id LIMIT 1);
SET @pqt = (SELECT id FROM unidad_medida WHERE abreviatura = 'PQT' ORDER BY id LIMIT 1);

SET @prov_andina   = (SELECT id FROM proveedor WHERE ruc = '20601234561');
SET @prov_pacifico = (SELECT id FROM proveedor WHERE ruc = '20512345672');
SET @prov_lima     = (SELECT id FROM proveedor WHERE ruc = '20487654323');

-- Stock: stock_minimo <= punto_reposicion <= stock_maximo. Tres productos inactivos.
INSERT INTO productos (codigo, codigo_barras, nombre, descripcion, tipo_producto, marca, fabricante,
                       stock_minimo, punto_reposicion, stock_maximo,
                       maneja_lote, maneja_vencimiento, activo,
                       categoria_id, unidad_medida_id, proveedor_id) VALUES
('MED-001', '7750000000017', 'Paracetamol 500 mg tableta x 100', 'Analgésico y antipirético',
 'Medicamento', 'Genérico', 'Laboratorios Andinos', 20, 40, 200, 1, 1, 1, @cat_med, @cja, @prov_pacifico),
('MED-002', '7750000000024', 'Amoxicilina 500 mg cápsula x 100', 'Antibiótico betalactámico',
 'Medicamento', 'Genérico', 'Laboratorios Andinos', 15, 30, 150, 1, 1, 1, @cat_med, @cja, @prov_pacifico),
('MED-003', '7750000000031', 'Omeprazol 20 mg cápsula x 30', 'Inhibidor de la bomba de protones',
 'Medicamento', 'Genérico', 'Farmacéutica del Sur', 10, 25, 120, 1, 1, 1, @cat_med, @cja, @prov_pacifico),
('MED-004', '7750000000048', 'Metamizol 1 g/2 mL ampolla', 'Analgésico de uso parenteral',
 'Medicamento', 'Genérico', 'Farmacéutica del Sur', 50, 100, 500, 1, 1, 1, @cat_med, @amp, @prov_andina),
('MED-005', '7750000000055', 'Cloruro de sodio 0.9 % 1000 mL', 'Solución salina isotónica para infusión',
 'Medicamento', 'Genérico', 'Soluciones Parenterales S.A.', 100, 200, 800, 1, 1, 1, @cat_sol, @fco, @prov_andina),
('MED-006', '7750000000062', 'Ceftriaxona 1 g vial', 'Antibiótico cefalosporina. Presentación descontinuada',
 'Medicamento', 'Genérico', 'Laboratorios Andinos', 30, 60, 300, 1, 1, 0, @cat_med, @via, @prov_pacifico),
('INS-001', NULL, 'Alcohol etílico 70° 1 L', 'Antiséptico de uso externo',
 'Insumo', 'Genérico', 'Química Industrial Lima', 10, 20, 80, 1, 1, 1, @cat_sol, @fco, @prov_lima),
('INS-002', NULL, 'Clorhexidina 2 % solución 500 mL', 'Antiséptico para preparación de piel',
 'Insumo', 'Genérico', 'Química Industrial Lima', 8, 15, 60, 1, 1, 1, @cat_sol, @fco, @prov_lima),
('INS-003', NULL, 'Guantes de nitrilo talla M x 100', 'Guantes de examen sin polvo',
 'Insumo', 'Genérico', 'Protección Médica S.A.C.', 30, 60, 300, 1, 1, 1, @cat_bio, @cja, @prov_lima),
('INS-004', NULL, 'Mascarilla N95', 'Respirador con filtro de partículas',
 'Insumo', 'Genérico', 'Protección Médica S.A.C.', 100, 250, 1000, 1, 1, 1, @cat_bio, @und, @prov_lima),
('INS-006', NULL, 'Mascarilla quirúrgica descartable x 50', 'Mascarilla de tres pliegues con elástico',
 'Insumo', 'Genérico', 'Protección Médica S.A.C.', 20, 40, 200, 1, 1, 1, @cat_bio, @cja, @prov_lima),
('INS-005', NULL, 'Bata quirúrgica descartable talla L', 'Reemplazada por la presentación estéril',
 'Insumo', 'Genérico', 'Protección Médica S.A.C.', 20, 40, 150, 1, 0, 0, @cat_bio, @und, NULL),
('MAT-001', NULL, 'Gasa estéril 10 x 10 cm x 100', 'Gasa de algodón de 12 capas',
 'Material médico', 'Genérico', 'Textiles Médicos del Perú', 25, 50, 250, 1, 1, 1, @cat_cur, @pqt, @prov_andina),
('MAT-002', NULL, 'Jeringa descartable 5 mL x 100', 'Jeringa con aguja 21G x 1 1/2"',
 'Material médico', 'Genérico', 'Plásticos Médicos S.A.', 20, 40, 200, 1, 1, 1, @cat_ins, @cja, @prov_andina),
('MAT-003', NULL, 'Catéter intravenoso 20G x 50', 'Catéter periférico con cámara de flujo',
 'Material médico', 'Genérico', 'Plásticos Médicos S.A.', 10, 20, 100, 1, 1, 1, @cat_ins, @cja, NULL),
('MAT-004', NULL, 'Equipo de venoclisis macrogotero', 'Modelo anterior sin regulador de precisión',
 'Material médico', 'Genérico', 'Plásticos Médicos S.A.', 40, 80, 400, 1, 1, 0, @cat_ins, @und, @prov_andina);

COMMIT;
