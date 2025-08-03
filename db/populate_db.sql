INSERT INTO Makes (name) VALUES
('Acura'), ('Alfa Romeo'), ('Aston Martin'), ('Audi'), ('Bentley'),
('BMW'), ('Buick'), ('Cadillac'), ('Chevrolet'), ('Chrysler'),
('Dodge'), ('Fiat'), ('Ford'), ('Genesis'), ('GMC'),
('Honda'), ('Hyundai'), ('Infiniti'), ('Jaguar'), ('Jeep'),
('Kia'), ('Lamborghini'), ('Land Rover'), ('Lexus'), ('Lincoln'),
('Lucid'), ('Maserati'), ('Mazda'), ('McLaren'), ('Mercedes-Benz'),
('Mini'), ('Mitsubishi'), ('Nissan'), ('Polestar'), ('Porsche'),
('Ram'), ('Rivian'), ('Rolls-Royce'), ('Subaru'), ('Tesla'),
('Toyota'), ('Volkswagen'), ('Volvo');

INSERT INTO Models (name, make_id) VALUES
--  Acura
('Integra', 1), ('TLX', 1), ('RDX', 1), ('MDX', 1), ('NSX', 1),
--  Alfa Romeo
('Giulia', 2), ('Stelvio', 2), ('Tonale', 2), ('4C', 2),
--  Aston Martin
('DB11', 3), ('DBS', 3), ('Vantage', 3), ('Rapide', 3),
--  Audi
('A3', 4), ('A4', 4), ('A6', 4), ('A8', 4), ('Q3', 4), ('Q5', 4), ('Q7', 4), ('Q8', 4), ('R8', 4),
--  Bentley
('Bentayga', 5), ('Continental GT', 5), ('Flying Spur', 5), ('Mulsanne', 5),
--  BMW
('3 Series', 6), ('5 Series', 6), ('7 Series', 6), ('X3', 6), ('X5', 6), ('X7', 6), ('M3', 6), ('M5', 6),
--  Buick
('Encore', 7), ('Enclave', 7), ('Envision', 7),
--  Cadillac
('CT4', 8), ('CT5', 8), ('Escalade', 8), ('XT4', 8), ('XT6', 8),
--  Chevrolet
('Silverado', 9), ('Camaro', 9), ('Malibu', 9), ('Tahoe', 9), ('Suburban', 9), ('Equinox', 9), ('Blazer', 9), ('Corvette', 9),
--  Chrysler
('300', 10), ('Pacifica', 10), ('Voyager', 10),
--  Dodge
('Charger', 11), ('Challenger', 11), ('Durango', 11), ('Journey', 11), ('Hornet', 11),
--  Fiat
('500', 12), ('500X', 12), ('124 Spider', 12),
--  Ford
('F-150', 13), ('Mustang', 13), ('Explorer', 13), ('Escape', 13), ('Bronco', 13), ('Expedition', 13), ('Edge', 13), ('Focus', 13),
--  Genesis
('G70', 14), ('G80', 14), ('G90', 14), ('GV70', 14), ('GV80', 14),
--  GMC
('Sierra', 15), ('Yukon', 15), ('Terrain', 15), ('Acadia', 15),
--  Honda
('Civic', 16), ('Accord', 16), ('CR-V', 16), ('Pilot', 16), ('Odyssey', 16), ('HR-V', 16), ('Passport', 16), ('Ridgeline', 16),
--  Hyundai
('Elantra', 17), ('Sonata', 17), ('Tucson', 17), ('Santa Fe', 17), ('Palisade', 17), ('Kona', 17), ('Veloster', 17), ('Ioniq 5', 17),
--  Infiniti
('Q50', 18), ('Q60', 18), ('QX50', 18), ('QX60', 18), ('QX80', 18),
--  Jaguar
('F-Type', 19), ('E-PACE', 19), ('F-PACE', 19), ('I-PACE', 19),
--  Jeep
('Wrangler', 20), ('Grand Cherokee', 20), ('Compass', 20), ('Renegade', 20),
--  Kia
('Soul', 21), ('Forte', 21), ('Optima', 21), ('Sportage', 21), ('Telluride', 21), ('Sorento', 21), ('Carnival', 21),
--  Lamborghini
('Huracan', 22), ('Aventador', 22), ('Urus', 22),
--  Land Rover
('Defender', 23), ('Discovery', 23), ('Range Rover', 23), ('Velar', 23),
--  Lexus
('RX', 24), ('ES', 24), ('NX', 24), ('GX', 24), ('LX', 24),
--  Lincoln
('Aviator', 25), ('Corsair', 25), ('Navigator', 25), ('Nautilus', 25),
--  Lucid
('Air', 26),
--  Maserati
('Ghibli', 27), ('Levante', 27), ('Quattroporte', 27),
--  Mazda
('Mazda3', 28), ('Mazda6', 28), ('CX-5', 28), ('CX-9', 28),
--  McLaren
('720S', 29), ('Artura', 29), ('GT', 29),
--  Mercedes-Benz
('C-Class', 30), ('E-Class', 30), ('S-Class', 30), ('GLA', 30), ('GLC', 30), ('GLE', 30), ('GLS', 30), ('AMG GT', 30),
--  Mini
('Cooper', 31), ('Countryman', 31),
--  Mitsubishi
('Outlander', 32), ('Eclipse Cross', 32),
--  Nissan
('Altima', 33), ('Sentra', 33), ('Maxima', 33), ('Rogue', 33), ('Pathfinder', 33),
--  Polestar
('Polestar 2', 34),
-- Porsche
('911', 35), ('Cayenne', 35), ('Macan', 35),
--  Ram
('1500', 36), ('2500', 36), ('3500', 36),
--  Rivian
('R1T', 37), ('R1S', 37),
--  Rolls-Royce
('Ghost', 38), ('Phantom', 38), ('Cullinan', 38),
--  Subaru
('Outback', 39), ('Forester', 39),
--  Tesla
('Model S', 40), ('Model 3', 40), ('Model X', 40), ('Model Y', 40),
--  Toyota
('Corolla', 41), ('Camry', 41), ('RAV4', 41), ('Highlander', 41),
--  Volkswagen
('Jetta', 42), ('Passat', 42), ('Tiguan', 42),
--  Volvo
('XC40', 43), ('XC60', 43), ('XC90', 43);

INSERT INTO Employees (first_name, last_name, role, labor_price, phone, email) VALUES
('John', 'Doe', 'Mechanic', 24.00, '555-1234', 'john.doe@example.com'),
('Jane', 'Smith', 'Service_Advisor', 25.00, '555-5678', 'jane.smith@example.com'),
('Michael', 'Johnson', 'Mechanic', 24.00, '555-8765', 'michael.johnson@example.com'),
('Emily', 'Davis', 'Receptionist', 21.00, '555-4321', 'emily.davis@example.com'),
('Robert', 'Brown', 'Manager', 28.00, '555-3456', 'robert.brown@example.com');

-- Insert common categories into PartCategories
INSERT INTO PartCategories (name) VALUES
('Engine'),
('Brakes'),
('Tires'),
('Battery'),
('Transmission'),
('Cooling System'),
('AC System'),
('Exhaust System'),
('Suspension & Steering'),
('Ignition System'),
('Lighting'),
('Wipers'),
('Exterior'),
('Rustproofing'),
('Hybrid/Electric Vehicle');

-- Insert common categories into ServiceCategories
INSERT INTO ServiceCategories (name) VALUES
('Oil & Fluids'),
('Brakes'),
('Tires & Wheels'),
('Battery & Electrical'),
('Transmission'),
('Cooling System'),
('AC & Heating'),
('Exhaust System'),
('Suspension & Steering'),
('Diagnostics'),
('Lighting'),
('Wipers & Visibility'),
('Exterior & Detailing'),
('Rustproofing'),
('Hybrid/Electric Vehicle');

-- Insert more sample parts linked to categories
INSERT INTO Parts (name, category_id, price, stock_quantity) VALUES
('Engine Oil', (SELECT id FROM PartCategories WHERE name = 'Engine'), 29.99, 50),
('Oil Filter', (SELECT id FROM PartCategories WHERE name = 'Engine'), 12.99, 40),
('Brake Pads', (SELECT id FROM PartCategories WHERE name = 'Brakes'), 59.99, 30),
('Brake Rotors', (SELECT id FROM PartCategories WHERE name = 'Brakes'), 89.99, 20),
('All-Season Tires', (SELECT id FROM PartCategories WHERE name = 'Tires'), 129.99, 40),
('Winter Tires', (SELECT id FROM PartCategories WHERE name = 'Tires'), 149.99, 35),
('Car Battery', (SELECT id FROM PartCategories WHERE name = 'Battery'), 149.99, 20),
('Battery Terminals', (SELECT id FROM PartCategories WHERE name = 'Battery'), 15.99, 50),
('Transmission Fluid', (SELECT id FROM PartCategories WHERE name = 'Transmission'), 39.99, 35),
('Clutch Kit', (SELECT id FROM PartCategories WHERE name = 'Transmission'), 229.99, 10),
('Coolant Antifreeze', (SELECT id FROM PartCategories WHERE name = 'Cooling System'), 24.99, 50),
('Radiator', (SELECT id FROM PartCategories WHERE name = 'Cooling System'), 199.99, 15),
('AC Compressor', (SELECT id FROM PartCategories WHERE name = 'AC System'), 249.99, 10),
('Cabin Air Filter', (SELECT id FROM PartCategories WHERE name = 'AC System'), 19.99, 60),
('Muffler', (SELECT id FROM PartCategories WHERE name = 'Exhaust System'), 199.99, 15),
('Oxygen Sensor', (SELECT id FROM PartCategories WHERE name = 'Exhaust System'), 69.99, 25),
('Shock Absorbers', (SELECT id FROM PartCategories WHERE name = 'Suspension & Steering'), 179.99, 12),
('Steering Rack', (SELECT id FROM PartCategories WHERE name = 'Suspension & Steering'), 349.99, 5),
('Spark Plugs', (SELECT id FROM PartCategories WHERE name = 'Ignition System'), 19.99, 60),
('Ignition Coils', (SELECT id FROM PartCategories WHERE name = 'Ignition System'), 89.99, 25),
('Headlight Bulbs', (SELECT id FROM PartCategories WHERE name = 'Lighting'), 29.99, 50),
('Taillight Assembly', (SELECT id FROM PartCategories WHERE name = 'Lighting'), 99.99, 20),
('Windshield Wipers', (SELECT id FROM PartCategories WHERE name = 'Wipers'), 24.99, 40),
('Wiper Fluid', (SELECT id FROM PartCategories WHERE name = 'Wipers'), 9.99, 100),
('Side Mirrors', (SELECT id FROM PartCategories WHERE name = 'Exterior'), 129.99, 15),
('Door Handles', (SELECT id FROM PartCategories WHERE name = 'Exterior'), 34.99, 30),
('Undercoating Spray', (SELECT id FROM PartCategories WHERE name = 'Rustproofing'), 24.99, 50),
('Hybrid Battery Pack', (SELECT id FROM PartCategories WHERE name = 'Hybrid/Electric Vehicle'), 2499.99, 5);

-- Insert more sample services linked to categories
INSERT INTO Services (name, category_id, description, price) VALUES
('Oil Change', (SELECT id FROM ServiceCategories WHERE name = 'Oil & Fluids'), 'Replace engine oil and filter for optimal performance.', 49.99),
('Brake Inspection & Repair', (SELECT id FROM ServiceCategories WHERE name = 'Brakes'), 'Check and replace brake pads, rotors, and fluid.', 149.99),
('Tire Rotation & Balancing', (SELECT id FROM ServiceCategories WHERE name = 'Tires & Wheels'), 'Improve tire wear and handling with rotation and balance.', 39.99),
('Battery Replacement', (SELECT id FROM ServiceCategories WHERE name = 'Battery & Electrical'), 'Test and replace car battery with installation.', 129.99),
('Alternator Replacement', (SELECT id FROM ServiceCategories WHERE name = 'Battery & Electrical'), 'Replace alternator to ensure proper charging system function.', 349.99),
('Starter Motor Replacement', (SELECT id FROM ServiceCategories WHERE name = 'Battery & Electrical'), 'Replace faulty starter motor to resolve starting issues.', 199.99),
('Transmission Service', (SELECT id FROM ServiceCategories WHERE name = 'Transmission'), 'Inspect, flush, and refill transmission fluid.', 199.99),
('Clutch Replacement', (SELECT id FROM ServiceCategories WHERE name = 'Transmission'), 'Replace worn clutch to restore smooth shifting.', 599.99),
('Coolant Flush', (SELECT id FROM ServiceCategories WHERE name = 'Cooling System'), 'Drain old coolant and refill with new antifreeze solution.', 79.99),
('Radiator Replacement', (SELECT id FROM ServiceCategories WHERE name = 'Cooling System'), 'Replace radiator to prevent overheating.', 399.99),
('AC System Repair', (SELECT id FROM ServiceCategories WHERE name = 'AC & Heating'), 'Diagnose and recharge air conditioning refrigerant.', 99.99),
('Heater Core Repair', (SELECT id FROM ServiceCategories WHERE name = 'AC & Heating'), 'Repair heater core to restore heating function.', 299.99),
('Exhaust System Repair', (SELECT id FROM ServiceCategories WHERE name = 'Exhaust System'), 'Inspect and repair muffler, catalytic converter, or leaks.', 129.99),
('Suspension & Steering Repair', (SELECT id FROM ServiceCategories WHERE name = 'Suspension & Steering'), 'Inspect and replace shocks, struts, and steering components.', 179.99),
('Alignment Service', (SELECT id FROM ServiceCategories WHERE name = 'Suspension & Steering'), 'Adjust wheel alignment for better handling.', 89.99),
('Engine Diagnostics', (SELECT id FROM ServiceCategories WHERE name = 'Diagnostics'), 'Full vehicle diagnostic scan for engine trouble codes.', 59.99),
('Check Engine Light Inspection', (SELECT id FROM ServiceCategories WHERE name = 'Diagnostics'), 'Analyze check engine codes and recommend solutions.', 79.99),
('Headlight & Taillight Replacement', (SELECT id FROM ServiceCategories WHERE name = 'Lighting'), 'Replace dim or broken lights for road safety.', 49.99),
('Windshield Wiper Replacement', (SELECT id FROM ServiceCategories WHERE name = 'Wipers & Visibility'), 'Ensure clear visibility in rainy and snowy conditions.', 29.99),
('Full Car Wash & Detailing', (SELECT id FROM ServiceCategories WHERE name = 'Exterior & Detailing'), 'Complete interior and exterior cleaning service.', 79.99),
('Rustproofing & Undercoating', (SELECT id FROM ServiceCategories WHERE name = 'Rustproofing'), 'Protect vehicle from rust and corrosion.', 149.99),
('Hybrid/Electric Vehicle Maintenance', (SELECT id FROM ServiceCategories WHERE name = 'Hybrid/Electric Vehicle'), 'Battery diagnostics and system checks.', 199.99),
('Pre-Purchase Vehicle Inspection', (SELECT id FROM ServiceCategories WHERE name = 'Diagnostics'), 'Full vehicle assessment before purchasing a used car.', 129.99);
