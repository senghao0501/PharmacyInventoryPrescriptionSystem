CREATE DATABASE IF NOT EXISTS pharmacy_system;

USE pharmacy_system;

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS sales_transactions;
DROP TABLE IF EXISTS inventory_transactions;
DROP TABLE IF EXISTS prescription_items;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS medicines;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(30),
    email VARCHAR(100),
    role ENUM('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,

    medical_record_number VARCHAR(50),
    date_of_birth DATE,
    allergy_history VARCHAR(500),

    license_number VARCHAR(50),
    specialization VARCHAR(100),
    department VARCHAR(100),

    pharmacist_license_id VARCHAR(50),
    shift_schedule VARCHAR(100),

    admin_access_level VARCHAR(50)
);

CREATE TABLE medicines (
    medicine_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category ENUM(
        'PRESCRIPTION_ONLY',
        'OVER_THE_COUNTER',
        'CONTROLLED_SUBSTANCE'
    ) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    min_threshold_quantity INT NOT NULL DEFAULT 5,
    expiry_date DATE,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE prescriptions (
    prescription_id VARCHAR(30) PRIMARY KEY,
    prescription_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM(
        'PENDING',
        'PREPARING',
        'READY_FOR_COLLECTION',
        'DISPENSED',
        'CANCELLED'
    ) NOT NULL,

    remarks VARCHAR(500),

    patient_id VARCHAR(20) NOT NULL,
    prescribing_doctor_id VARCHAR(20) NOT NULL,
    dispensing_pharmacist_id VARCHAR(20),

    total_price DECIMAL(10,2) DEFAULT 0.00,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    cancellation_reason VARCHAR(500),

    FOREIGN KEY (patient_id) REFERENCES users(user_id),
    FOREIGN KEY (prescribing_doctor_id) REFERENCES users(user_id),
    FOREIGN KEY (dispensing_pharmacist_id) REFERENCES users(user_id)
);

CREATE TABLE prescription_items (
    item_id VARCHAR(30) PRIMARY KEY,
    prescription_id VARCHAR(30) NOT NULL,
    medicine_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    dosage_instructions VARCHAR(500),
    unit_price_at_time DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (prescription_id)
        REFERENCES prescriptions(prescription_id)
        ON DELETE CASCADE,

    FOREIGN KEY (medicine_id)
        REFERENCES medicines(medicine_id)
);

CREATE TABLE inventory_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    performed_by VARCHAR(20) NOT NULL,
    remarks VARCHAR(500),

    FOREIGN KEY (medicine_id)
        REFERENCES medicines(medicine_id),

    FOREIGN KEY (performed_by)
        REFERENCES users(user_id)
);

CREATE TABLE sales_transactions (
    transaction_id VARCHAR(30) PRIMARY KEY,
    prescription_id VARCHAR(30) NOT NULL UNIQUE,
    patient_id VARCHAR(20) NOT NULL,
    pharmacist_id VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (prescription_id)
        REFERENCES prescriptions(prescription_id),

    FOREIGN KEY (patient_id)
        REFERENCES users(user_id),

    FOREIGN KEY (pharmacist_id)
        REFERENCES users(user_id)
);

CREATE TABLE notifications (
    notification_id VARCHAR(30) PRIMARY KEY,
    message VARCHAR(500) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    type ENUM(
        'LOW_STOCK_WARNING',
        'PRESCRIPTION_READY',
        'SYSTEM_ALERT'
    ) NOT NULL,

    recipient_id VARCHAR(20) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (recipient_id)
        REFERENCES users(user_id)
);

INSERT INTO users
(
    user_id,
    username,
    password,
    full_name,
    contact_number,
    email,
    role,
    is_active,
    medical_record_number,
    date_of_birth,
    allergy_history
)
VALUES
(
    'P001',
    'patient1',
    'patient123',
    'John Patient',
    '0123456789',
    'patient@gmail.com',
    'PATIENT',
    TRUE,
    'MRN001',
    '2002-05-10',
    'Penicillin'
);

INSERT INTO users
(
    user_id,
    username,
    password,
    full_name,
    contact_number,
    email,
    role,
    is_active,
    license_number,
    specialization,
    department
)
VALUES
(
    'D001',
    'doctor1',
    'doctor123',
    'Dr. Sarah Tan',
    '0121111111',
    'doctor@gmail.com',
    'DOCTOR',
    TRUE,
    'DOC001',
    'General Medicine',
    'Outpatient'
);

INSERT INTO users
(
    user_id,
    username,
    password,
    full_name,
    contact_number,
    email,
    role,
    is_active,
    pharmacist_license_id,
    shift_schedule
)
VALUES
(
    'PH001',
    'pharmacist1',
    'pharm123',
    'Pharmacist Lee',
    '0122222222',
    'pharmacist@gmail.com',
    'PHARMACIST',
    TRUE,
    'PHL001',
    'Morning Shift'
);

INSERT INTO users
(
    user_id,
    username,
    password,
    full_name,
    contact_number,
    email,
    role,
    is_active,
    admin_access_level
)
VALUES
(
    'A001',
    'admin',
    'admin123',
    'System Administrator',
    '0123333333',
    'admin@gmail.com',
    'ADMIN',
    TRUE,
    'FULL_ACCESS'
);

INSERT INTO medicines
(
    medicine_id,
    name,
    category,
    unit_price,
    stock_quantity,
    min_threshold_quantity,
    expiry_date,
    is_active
)
VALUES
(
    'M001',
    'Paracetamol',
    'OVER_THE_COUNTER',
    5.00,
    100,
    20,
    '2027-12-31',
    TRUE
);

INSERT INTO medicines
(
    medicine_id,
    name,
    category,
    unit_price,
    stock_quantity,
    min_threshold_quantity,
    expiry_date,
    is_active
)
VALUES
(
    'M002',
    'Amoxicillin',
    'PRESCRIPTION_ONLY',
    12.50,
    50,
    10,
    '2027-06-30',
    TRUE
);

INSERT INTO medicines
(
    medicine_id,
    name,
    category,
    unit_price,
    stock_quantity,
    min_threshold_quantity,
    expiry_date,
    is_active
)
VALUES
(
    'M003',
    'Aspirin',
    'OVER_THE_COUNTER',
    4.50,
    5,
    10,
    '2027-10-31',
    TRUE
);

INSERT INTO medicines
(
    medicine_id,
    name,
    category,
    unit_price,
    stock_quantity,
    min_threshold_quantity,
    expiry_date,
    is_active
)
VALUES
(
    'M004',
    'Insulin',
    'PRESCRIPTION_ONLY',
    35.00,
    20,
    5,
    '2027-04-30',
    TRUE
);