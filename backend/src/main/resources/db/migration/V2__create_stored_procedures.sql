-- Create stored procedures for Seating Arrangement System

/* Purpose: Query active floors. */
CREATE PROCEDURE dbo.sp_get_floors
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        floor_id,
        floor_name,
        display_order,
        is_active
    FROM floors
    WHERE is_active = 1
    ORDER BY display_order, floor_id;
END;
GO
/* Purpose: Query seats, optionally filtered by floor, with active employee assignment. */
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
        s.row_no,
        s.column_no,
        s.seat_status,
        s.is_active,
        e.employee_id,
        e.employee_no,
        e.employee_name,
        e.department,
        e.job_title
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
    ORDER BY f.display_order, s.row_no, s.column_no, s.seat_no;
END;
GO
/* Purpose: Query active employees by employee number, name, or department. */
CREATE PROCEDURE dbo.sp_get_employees
    @keyword NVARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        employee_id,
        employee_no,
        employee_name,
        department,
        job_title,
        is_active
    FROM employees
    WHERE is_active = 1
      AND (
            @keyword IS NULL
         OR employee_no LIKE '%' + @keyword + '%'
         OR employee_name LIKE N'%' + @keyword + N'%'
         OR department LIKE N'%' + @keyword + N'%'
      )
    ORDER BY employee_no;
END;
GO
/* Purpose: Assign a seat after checking employee and seat availability. */
CREATE PROCEDURE dbo.sp_assign_seat
    @seat_id INT,
    @employee_no CHAR(5),
    @assigned_by NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @employee_id INT;
    DECLARE @assignment_id INT;

    IF @seat_id IS NULL
        THROW 50001, 'seat_id is required.', 1;

    IF @employee_no IS NULL OR @employee_no NOT LIKE '[0-9][0-9][0-9][0-9][0-9]'
        THROW 50002, 'employee_no must be exactly 5 digits.', 1;

    IF @assigned_by IS NULL OR LTRIM(RTRIM(@assigned_by)) = N''
        THROW 50003, 'assigned_by is required.', 1;

    SELECT @employee_id = employee_id
    FROM employees
    WHERE employee_no = @employee_no
      AND is_active = 1;

    IF @employee_id IS NULL
        THROW 50004, 'Employee does not exist or is inactive.', 1;

    IF NOT EXISTS (
        SELECT 1
        FROM seats
        WHERE seat_id = @seat_id
          AND is_active = 1
    )
        THROW 50005, 'Seat does not exist or is inactive.', 1;

    IF EXISTS (
        SELECT 1
        FROM seats
        WHERE seat_id = @seat_id
          AND seat_status = 'DISABLED'
    )
        THROW 50006, 'Seat is disabled.', 1;

    IF EXISTS (
        SELECT 1
        FROM seat_assignments
        WHERE employee_id = @employee_id
          AND assignment_status = 'ACTIVE'
    )
        THROW 50007, 'Employee already has an active seat.', 1;

    IF EXISTS (
        SELECT 1
        FROM seat_assignments
        WHERE seat_id = @seat_id
          AND assignment_status = 'ACTIVE'
    )
        THROW 50008, 'Seat is already occupied.', 1;

    INSERT INTO seat_assignments (
        seat_id,
        employee_id,
        assigned_at,
        assigned_by,
        assignment_status
    )
    VALUES (
        @seat_id,
        @employee_id,
        GETDATE(),
        @assigned_by,
        'ACTIVE'
    );

    SET @assignment_id = CONVERT(INT, SCOPE_IDENTITY());

    UPDATE seats
    SET seat_status = 'OCCUPIED',
        updated_at = GETDATE()
    WHERE seat_id = @seat_id;

    INSERT INTO seat_change_logs (
        seat_id,
        employee_id,
        action_type,
        action_by,
        action_at,
        remark
    )
    VALUES (
        @seat_id,
        @employee_id,
        'ASSIGN',
        @assigned_by,
        GETDATE(),
        N'Assign seat'
    );

    SELECT
        sa.assignment_id,
        sa.seat_id,
        s.seat_no,
        s.floor_id,
        f.floor_name,
        sa.employee_id,
        e.employee_no,
        e.employee_name,
        sa.assignment_status,
        sa.assigned_at
    FROM seat_assignments sa
    INNER JOIN seats s
        ON s.seat_id = sa.seat_id
    INNER JOIN floors f
        ON f.floor_id = s.floor_id
    INNER JOIN employees e
        ON e.employee_id = sa.employee_id
    WHERE sa.assignment_id = @assignment_id;
END;
GO
/* Purpose: Clear an active seat assignment by seat_id or employee_no. */
CREATE PROCEDURE dbo.sp_clear_seat
    @seat_id INT = NULL,
    @employee_no CHAR(5) = NULL,
    @released_by NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @assignment_id INT;
    DECLARE @target_seat_id INT;
    DECLARE @employee_id INT;

    IF @seat_id IS NULL AND @employee_no IS NULL
        THROW 50101, 'seat_id or employee_no is required.', 1;

    IF @employee_no IS NOT NULL AND @employee_no NOT LIKE '[0-9][0-9][0-9][0-9][0-9]'
        THROW 50102, 'employee_no must be exactly 5 digits.', 1;

    IF @released_by IS NULL OR LTRIM(RTRIM(@released_by)) = N''
        THROW 50103, 'released_by is required.', 1;

    SELECT @employee_id = employee_id
    FROM employees
    WHERE employee_no = @employee_no
      AND is_active = 1;

    IF @employee_no IS NOT NULL AND @employee_id IS NULL
        THROW 50104, 'Employee does not exist or is inactive.', 1;

    SELECT
        @assignment_id = sa.assignment_id,
        @target_seat_id = sa.seat_id,
        @employee_id = sa.employee_id
    FROM seat_assignments sa
    WHERE sa.assignment_status = 'ACTIVE'
      AND (@seat_id IS NULL OR sa.seat_id = @seat_id)
      AND (@employee_no IS NULL OR sa.employee_id = @employee_id);

    IF @assignment_id IS NULL
        THROW 50105, 'Active seat assignment was not found.', 1;

    UPDATE seat_assignments
    SET assignment_status = 'RELEASED',
        released_at = GETDATE(),
        released_by = @released_by
    WHERE assignment_id = @assignment_id;

    UPDATE seats
    SET seat_status = 'AVAILABLE',
        updated_at = GETDATE()
    WHERE seat_id = @target_seat_id
      AND seat_status <> 'DISABLED';

    INSERT INTO seat_change_logs (
        seat_id,
        employee_id,
        action_type,
        action_by,
        action_at,
        remark
    )
    VALUES (
        @target_seat_id,
        @employee_id,
        'RELEASE',
        @released_by,
        GETDATE(),
        N'Clear seat'
    );

    SELECT
        @assignment_id AS assignment_id,
        @target_seat_id AS seat_id,
        @employee_id AS employee_id,
        'RELEASED' AS assignment_status;
END;
GO