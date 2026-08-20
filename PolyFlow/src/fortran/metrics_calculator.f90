!===============================================================================
! Module: Fortran Metrics Calculator (Stage 2)
! Authors: Randall AC, Keilor MC
! Description:
!   Reads cleaned telemetry data from 'data/datos_normalizados.csv', parses
!   numerical fields, computes aggregate metrics (averages and record counts),
!   and outputs key-value pairs to 'data/metricas.csv'.
!===============================================================================

program metrics_calculator
    implicit none

    ! Character buffer for line-by-line reading
    character(len=250) :: line_buffer

    ! Numeric variables for row values
    real :: temp_val, precip_val, wind_val, battery_val

    ! Accumulators for statistics
    real :: sum_temp, sum_precip, sum_wind, sum_battery
    integer :: record_count

    ! File I/O control and CSV parsing indices
    integer :: io_status, read_err
    integer :: p1, p2, p3, p4, p5

    ! Open input stream (cleaned CSV)
    open(unit=10, file='data/datos_normalizados.csv', status='old', action='read', iostat=io_status)
    if (io_status /= 0) then
        print *, "Fortran Error: Could not open input file 'data/datos_normalizados.csv'."
        stop 1
    end if

    ! Open output stream (metrics report)
    open(unit=20, file='data/metricas.csv', status='replace', action='write', iostat=io_status)
    if (io_status /= 0) then
        print *, "Fortran Error: Could not create output file 'data/metricas.csv'."
        close(10)
        stop 1
    end if

    ! Skip CSV header row
    read(10, '(A)', iostat=io_status) line_buffer
    if (io_status /= 0) then
        print *, "Fortran Warning: Input file is completely empty."
    end if

    ! Initialize aggregation variables
    sum_temp = 0.0
    sum_precip = 0.0
    sum_wind = 0.0
    sum_battery = 0.0
    record_count = 0

    ! Main record processing loop
    do
        read(10, '(A)', iostat=io_status) line_buffer
        if (io_status /= 0) exit ! End of File reached or read error

        ! Skip empty lines (Stress protection)
        if (len_trim(line_buffer) == 0) cycle

        ! Locate CSV comma delimiters sequentially
        p1 = index(line_buffer, ',')
        if (p1 <= 0) cycle

        p2 = p1 + index(line_buffer(p1+1:), ',')
        if (p2 <= p1) cycle

        p3 = p2 + index(line_buffer(p2+1:), ',')
        if (p3 <= p2) cycle

        p4 = p3 + index(line_buffer(p3+1:), ',')
        if (p4 <= p3) cycle

        p5 = p4 + index(line_buffer(p4+1:), ',')
        if (p5 <= p4) cycle

        ! Parse floating point values with internal read safety checks
        read(line_buffer(p2+1:p3-1), *, iostat=read_err) temp_val
        if (read_err /= 0) cycle

        read(line_buffer(p3+1:p4-1), *, iostat=read_err) precip_val
        if (read_err /= 0) cycle

        read(line_buffer(p4+1:p5-1), *, iostat=read_err) wind_val
        if (read_err /= 0) cycle

        read(line_buffer(p5+1:), *, iostat=read_err) battery_val
        if (read_err /= 0) cycle

        ! Accumulate valid parsed metrics
        sum_temp = sum_temp + temp_val
        sum_precip = sum_precip + precip_val
        sum_wind = sum_wind + wind_val
        sum_battery = sum_battery + battery_val
        record_count = record_count + 1
    end do

    close(10)

    ! Write final computed metrics to file
    if (record_count > 0) then
        write(20, '(A,F8.2)') "AVERAGE_TEMPERATURE=", sum_temp / real(record_count)
        write(20, '(A,F8.2)') "AVERAGE_PRECIPITATION=", sum_precip / real(record_count)
        write(20, '(A,F8.2)') "AVERAGE_WIND=", sum_wind / real(record_count)
        write(20, '(A,F8.2)') "AVERAGE_BATTERY=", sum_battery / real(record_count)
        write(20, '(A,I0)')   "TOTAL_RECORDS=", record_count
        print *, "Fortran: Metrics calculated successfully."
    else
        print *, "Fortran: No valid records found to process."
    end if

    close(20)

end program metrics_calculator