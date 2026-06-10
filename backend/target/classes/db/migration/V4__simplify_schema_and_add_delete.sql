-- Simplify employee and seat schema; update procedures; add delete procedures

IF COL_LENGTH('employees', 'department') IS NOT NULL
    ALTER TABLE employees DROP COLUMN department;
GO
IF COL_LENGTH('employees', 'job_title') IS NOT NULL
    ALTER TABLE employees DROP COLUMN job_title;
GO
IF COL_LENGTH('seats', 'row_no') IS NOT NULL
    ALTER TABLE seats DROP COLUMN row_no;
GO
IF COL_LENGTH('seats', 'column_no') IS NOT NULL
    ALTER TABLE seats DROP COLUMN column_no;
GO

DROP PROCEDURE IF EXISTS dbo.sp_get_seats;
GO
CREATE PROCEDURE dbo.sp_get_seats
    @floor_id INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        s.seat_id,
        s.floor_id,
        f.floor_name,
        s.seat_no,
        s.seat_status,
        s.is_active,
        e.employee_id,
        e.employee_no,
        e.employee_name
    FROM seats s
    INNER JOIN floors f
        ON f.floor_id = s.floor_id
    LEFT JOIN seat_assignments sa
        ON sa.seat_id = s.seat_id
       AND sa.assignment_status = 'ACTIVE'
    LEFT JOIN employees e
        ON e.employee_id = sa.employee_id
    WHERE s.is_active = 1
      AND f.is_active = 1
      AND (@floor_id IS NULL OR s.floor_id = @floor_id)
    ORDER BY f.display_order, s.seat_no;
END;
GO

DROP PROCEDURE IF EXISTS dbo.sp_get_employees;
GO
CREATE PROCEDURE dbo.sp_get_employees
    @keyword NVARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        employee_id,
        employee_no,
        employee_name,
        is_active
    FROM employees
    WHERE is_active = 1
      AND (
            @keyword IS NULL
         OR employee_no LIKE '%' + @keyword + '%'
         OR employee_name LIKE N'%' + @keyword + N'%'
      )
    ORDER BY employee_no;
END;
GO

CREATE PROCEDURE dbo.sp_delete_employee
    @employee_no CHAR(5)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @employee_id INT;

    IF @employee_no IS NULL OR @employee_no NOT LIKE '[0-9][0-9][0-9][0-9][0-9]'
        THROW 50201, 'employee_no must be exactly 5 digits.', 1;

    SELECT @employee_id = employee_id
    FROM employees
    WHERE employee_no = @employee_no
      AND is_active = 1;

    IF @employee_id IS NULL
        THROW 50202, 'Employee does not exist or is inactive.', 1;

    IF EXISTS (
        SELECT 1
        FROM seat_assignments
        WHERE employee_id = @employee_id
          AND assignment_status = 'ACTIVE'
    )
        THROW 50203, 'Employee has an active seat assignment.', 1;

    UPDATE employees
    SET is_active = 0,
        updated_at = GETDATE()
    WHERE employee_id = @employee_id;
END;
GO

CREATE PROCEDURE dbo.sp_delete_seat
    @seat_id INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @seat_id IS NULL
        THROW 50301, 'seat_id is required.', 1;

    IF NOT EXISTS (
        SELECT 1
        FROM seats
        WHERE seat_id = @seat_id
          AND is_active = 1
    )
        THROW 50302, 'Seat does not exist or is inactive.', 1;

    IF EXISTS (
        SELECT 1
        FROM seat_assignments
        WHERE seat_id = @seat_id
          AND assignment_status = 'ACTIVE'
    )
        THROW 50303, 'Seat has an active assignment.', 1;

    UPDATE seats
    SET is_active = 0,
        updated_at = GETDATE()
    WHERE seat_id = @seat_id;
END;
GO
