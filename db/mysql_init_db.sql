-- DROP DATABASE car_service;

-- CREATE DATABASE IF NOT EXISTS car_service;

USE car_service;

-- Customers - Stores customer details
-- One-to-Many (1:M) relationship with Vehicles
CREATE TABLE IF NOT EXISTS Customers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    address VARCHAR(100),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE
);

CREATE TABLE IF NOT EXISTS Makes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS Models (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    make_id INT NOT NULL,
    FOREIGN KEY (make_id) REFERENCES Makes(id) ON DELETE CASCADE
);

-- One-to-Many (1:M) relationship with Orders
CREATE TABLE IF NOT EXISTS Vehicles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    make_id INT NOT NULL,
    model_id INT NOT NULL,
    year YEAR NOT NULL,
    vin VARCHAR(50) UNIQUE,
    license_plate VARCHAR(20) UNIQUE,
    FOREIGN KEY (customer_id) REFERENCES Customers(id) ON DELETE CASCADE,
    FOREIGN KEY (make_id) REFERENCES Makes(id),
    FOREIGN KEY (model_id) REFERENCES Models(id)
);

-- Appointments
CREATE TABLE IF NOT EXISTS Appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    date_time DATETIME NOT NULL,
    status ENUM('Scheduled', 'Canceled', 'Completed') NOT NULL DEFAULT 'Scheduled',
    issue TEXT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customers(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES Vehicles(id) ON DELETE CASCADE
);

-- Employees - Stores service technicians and staff
CREATE TABLE IF NOT EXISTS Employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50),
    role ENUM('Mechanic', 'Service_Advisor', 'Manager', 'Receptionist') NOT NULL,
    labor_price DECIMAL(10,2) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE
);

CREATE TABLE IF NOT EXISTS PartCategories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS Parts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category_id INT,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT,
    FOREIGN KEY (category_id) REFERENCES PartCategories(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ServiceCategories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS Services (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category_id INT,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (category_id) REFERENCES ServiceCategories(id) ON DELETE SET NULL
);

-- Orders - Stores maintenance/service jobs
CREATE TABLE IF NOT EXISTS Orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    odometer INT,
    order_date DATE NOT NULL,
    type ENUM('REPAIR', 'MAINTENANCE', 'INSPECTION', 'INSTALLATION', 'DETAILING', 'OTHER') NOT NULL,
    status ENUM('Pending', 'In_Progress', 'Completed', 'Canceled') DEFAULT 'Pending',
    labor_hours DECIMAL(10,2) DEFAULT 1.00,
    notes TEXT,
    FOREIGN KEY (customer_id) REFERENCES Customers(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES Vehicles(id) ON DELETE CASCADE
);

-- Invoices - Stores billing details
-- One-to-One (1:1) relationship with Orders
CREATE TABLE IF NOT EXISTS Invoices (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    date DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('Unpaid', 'Paid', 'Canceled') DEFAULT 'Unpaid',
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
);

-- This table links Orders and Parts, tracking how many of each part was used in an order.
CREATE TABLE IF NOT EXISTS Order_Parts (
    order_id INT NOT NULL,
    part_id INT NOT NULL,
    part_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, part_id),
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (part_id) REFERENCES Parts(id) ON DELETE CASCADE
);


-- This table links Orders and Services, storing the services provided for each order
CREATE TABLE IF NOT EXISTS Order_Services (
    order_id INT NOT NULL,
    service_id INT NOT NULL,
    service_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, service_id),
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES Services(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Order_Employees (
    order_id INT NOT NULL,
    employee_id INT NOT NULL,
    labor_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, employee_id),
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES Employees(id) ON DELETE CASCADE
);

-- Store multiple photo paths per order
-- Each row stores a separate image for an order
CREATE TABLE IF NOT EXISTS Order_Photos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    photo_path VARCHAR(255) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
);
