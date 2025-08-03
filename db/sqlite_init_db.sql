-- Customers - Stores customer details
-- One-to-Many (1:M) relationship with Vehicles
CREATE TABLE IF NOT EXISTS Customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    address TEXT,
    city TEXT,
    state TEXT,
    postal_code TEXT,
    phone TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE
);

CREATE TABLE IF NOT EXISTS Makes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS Models (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    make_id INTEGER NOT NULL,
    FOREIGN KEY (make_id) REFERENCES Makes(id) ON DELETE CASCADE
);

-- One-to-Many (1:M) relationship with Orders
CREATE TABLE IF NOT EXISTS Vehicles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    make_id INTEGER NOT NULL,
    model_id INTEGER NOT NULL,
    year INTEGER NULL NOT NULL,
    vin TEXT UNIQUE,
    license_plate TEXT UNIQUE,
    FOREIGN KEY (customer_id) REFERENCES Customers(id) ON DELETE CASCADE,
    FOREIGN KEY (make_id) REFERENCES Makes(id),
    FOREIGN KEY (model_id) REFERENCES Models(id)
);

-- Appointments
CREATE TABLE IF NOT EXISTS Appointments (
    id INTEGER  PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    vehicle_id INTEGER NOT NULL,
    date_time DATETIME NOT NULL,
    status TEXT CHECK(status IN ('Scheduled', 'Canceled', 'Completed')) NOT NULL DEFAULT 'Scheduled',
    issue TEXT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customers(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES Vehicles(id) ON DELETE CASCADE
);

-- Employees - Stores service technicians and staff
CREATE TABLE IF NOT EXISTS Employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT,
    role TEXT CHECK(role IN ('Mechanic', 'Service_Advisor', 'Manager', 'Receptionist')) NOT NULL,
    labor_price DECIMAL(10,2) NOT NULL,
    phone TEXT UNIQUE,
    email TEXT UNIQUE
);

CREATE TABLE IF NOT EXISTS PartCategories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS Parts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category_id INTEGER,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER,
    FOREIGN KEY (category_id) REFERENCES PartCategories(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ServiceCategories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS Services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category_id INTEGER,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (category_id) REFERENCES ServiceCategories(id) ON DELETE SET NULL
);

-- Orders - Stores maintenance/service jobs
CREATE TABLE IF NOT EXISTS Orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    vehicle_id INTEGER NOT NULL,
    odometer INTEGER,
    order_date DATE NOT NULL,
    type TEXT CHECK(type IN ('REPAIR', 'MAINTENANCE', 'INSPECTION', 'INSTALLATION', 'DETAILING', 'OTHER')) NOT NULL,
    status TEXT CHECK(status IN ('Pending', 'In_Progress', 'Completed', 'Canceled')) DEFAULT 'Pending',
    labor_hours DECIMAL(10,2) DEFAULT 1.00,
    notes TEXT,
    FOREIGN KEY (customer_id) REFERENCES Customers(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES Vehicles(id) ON DELETE CASCADE
);

-- Invoices - Stores billing details
-- One-to-One (1:1) relationship with Orders
CREATE TABLE IF NOT EXISTS Invoices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    date DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status TEXT CHECK(status IN ('Unpaid', 'Paid', 'Canceled')) DEFAULT 'Unpaid',
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
);

-- This table links Orders and Parts, tracking how many of each part was used in an order.
CREATE TABLE IF NOT EXISTS Order_Parts (
    order_id INTEGER NOT NULL,
    part_id INTEGER NOT NULL,
    part_price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, part_id),
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (part_id) REFERENCES Parts(id) ON DELETE CASCADE
);

-- This table links Orders and Services, storing the services provided for each order
CREATE TABLE IF NOT EXISTS Order_Services (
    order_id INTEGER NOT NULL,
    service_id INTEGER NOT NULL,
    service_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, service_id),
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES Services(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Order_Employees (
    order_id INTEGER NOT NULL,
    employee_id INTEGER NOT NULL,
    labor_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, employee_id),
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES Employees(id) ON DELETE CASCADE
);

-- Store multiple photo paths per order
-- Each row stores a separate image for an order
CREATE TABLE IF NOT EXISTS Order_Photos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    photo_path TEXT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
);
