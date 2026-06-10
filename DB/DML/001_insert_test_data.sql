INSERT INTO floors (floor_id, floor_name, display_order)
VALUES
(1, N'1F', 1),
(2, N'2F', 2),
(3, N'3F', 3);

INSERT INTO seats (floor_id, seat_no, seat_status)
VALUES
(1, 'A-01', 'AVAILABLE'),
(1, 'A-02', 'AVAILABLE'),
(1, 'A-03', 'AVAILABLE'),
(1, 'B-01', 'AVAILABLE'),
(1, 'B-02', 'AVAILABLE'),
(1, 'B-03', 'AVAILABLE'),
(2, 'A-01', 'AVAILABLE'),
(2, 'A-02', 'AVAILABLE'),
(2, 'A-03', 'AVAILABLE'),
(2, 'B-01', 'AVAILABLE'),
(2, 'B-02', 'AVAILABLE'),
(2, 'B-03', 'AVAILABLE'),
(3, 'A-01', 'AVAILABLE'),
(3, 'A-02', 'AVAILABLE'),
(3, 'A-03', 'AVAILABLE'),
(3, 'B-01', 'AVAILABLE'),
(3, 'B-02', 'AVAILABLE'),
(3, 'B-03', 'AVAILABLE');

INSERT INTO employees (employee_no, employee_name)
VALUES
('00001', N'Alice Wang'),
('00002', N'Betty Lin'),
('00003', N'Charles Chen'),
('00004', N'Diana Chang'),
('00005', N'Evan Lee');
