-- Insert initial data for Seating Arrangement System

INSERT INTO floors (floor_id, floor_name, display_order)
VALUES
(1, N'1F', 1),
(2, N'2F', 2),
(3, N'3F', 3);

INSERT INTO seats (floor_id, seat_no, row_no, column_no, seat_status)
VALUES
-- Floor 1 (6 seats: 2 rows x 3 columns)
(1, 'A-01', 1, 1, 'AVAILABLE'),
(1, 'A-02', 1, 2, 'AVAILABLE'),
(1, 'A-03', 1, 3, 'AVAILABLE'),
(1, 'B-01', 2, 1, 'AVAILABLE'),
(1, 'B-02', 2, 2, 'AVAILABLE'),
(1, 'B-03', 2, 3, 'AVAILABLE'),
-- Floor 2 (6 seats: 2 rows x 3 columns)
(2, 'A-01', 1, 1, 'AVAILABLE'),
(2, 'A-02', 1, 2, 'AVAILABLE'),
(2, 'A-03', 1, 3, 'AVAILABLE'),
(2, 'B-01', 2, 1, 'AVAILABLE'),
(2, 'B-02', 2, 2, 'AVAILABLE'),
(2, 'B-03', 2, 3, 'AVAILABLE'),
-- Floor 3 (6 seats: 2 rows x 3 columns)
(3, 'A-01', 1, 1, 'AVAILABLE'),
(3, 'A-02', 1, 2, 'AVAILABLE'),
(3, 'A-03', 1, 3, 'AVAILABLE'),
(3, 'B-01', 2, 1, 'AVAILABLE'),
(3, 'B-02', 2, 2, 'AVAILABLE'),
(3, 'B-03', 2, 3, 'AVAILABLE');

INSERT INTO employees (employee_no, employee_name, department, job_title)
VALUES
('00001', N'Alice Wang', N'IT', N'Backend Engineer'),
('00002', N'Betty Lin', N'IT', N'Frontend Engineer'),
('00003', N'Charles Chen', N'HR', N'HR Specialist'),
('00004', N'Diana Chang', N'Finance', N'Accountant'),
('00005', N'Evan Lee', N'Operations', N'Operations Specialist');
