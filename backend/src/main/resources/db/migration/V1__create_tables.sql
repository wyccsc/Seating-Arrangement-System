-- Create tables for Seating Arrangement System

CREATE TABLE floors (
    floor_id INT PRIMARY KEY,
    floor_name VARCHAR(50) NOT NULL,
    display_order INT NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL
);

CREATE TABLE seats (
    seat_id INT IDENTITY(1,1) PRIMARY KEY,
    floor_id INT NOT NULL,
    seat_no VARCHAR(20) NOT NULL,
    row_no INT NOT NULL,
    column_no INT NOT NULL,
    seat_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    CONSTRAINT fk_seats_floor FOREIGN KEY (floor_id) REFERENCES floors(floor_id),
    CONSTRAINT uq_seats_floor_seat_no UNIQUE (floor_id, seat_no),
    CONSTRAINT ck_seats_status CHECK (seat_status IN ('AVAILABLE', 'OCCUPIED', 'DISABLED'))
);

CREATE TABLE employees (
    employee_id INT IDENTITY(1,1) PRIMARY KEY,
    employee_no CHAR(5) NOT NULL,
    employee_name NVARCHAR(50) NOT NULL,
    department NVARCHAR(50) NOT NULL,
    job_title NVARCHAR(50) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    CONSTRAINT uq_employees_employee_no UNIQUE (employee_no),
    CONSTRAINT ck_employees_employee_no CHECK (employee_no LIKE '[0-9][0-9][0-9][0-9][0-9]')
);

CREATE TABLE seat_assignments (
    assignment_id INT IDENTITY(1,1) PRIMARY KEY,
    seat_id INT NOT NULL,
    employee_id INT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT GETDATE(),
    assigned_by NVARCHAR(50) NOT NULL,
    released_at DATETIME NULL,
    released_by NVARCHAR(50) NULL,
    assignment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_assignments_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id),
    CONSTRAINT fk_assignments_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT ck_assignments_status CHECK (assignment_status IN ('ACTIVE', 'RELEASED'))
);

CREATE UNIQUE INDEX ux_active_assignment_by_seat
ON seat_assignments(seat_id)
WHERE assignment_status = 'ACTIVE';

CREATE UNIQUE INDEX ux_active_assignment_by_employee
ON seat_assignments(employee_id)
WHERE assignment_status = 'ACTIVE';

CREATE TABLE seat_change_logs (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    seat_id INT NOT NULL,
    employee_id INT NULL,
    action_type VARCHAR(20) NOT NULL,
    action_by NVARCHAR(50) NOT NULL,
    action_at DATETIME NOT NULL DEFAULT GETDATE(),
    remark NVARCHAR(200) NULL,
    CONSTRAINT fk_change_logs_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id),
    CONSTRAINT fk_change_logs_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT ck_change_logs_action CHECK (action_type IN ('ASSIGN', 'RELEASE', 'DISABLE', 'ENABLE'))
);
