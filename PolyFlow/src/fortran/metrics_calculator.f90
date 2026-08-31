!===============================================================================
! Module: Fortran Metrics Calculator
! Stage: 2
! Language: Fortran
! Authors: Randall AC, Keilor MC
!
! Description:
!   Reads normalized environmental telemetry produced by BASIC-256 and
!   calculates aggregate numerical metrics.
!
! Input:
!   data/datos_normalizados.csv
!
! Output:
!   data/metricas.csv
!===============================================================================

program metrics_calculator

    implicit none

    character(len=250) :: line_buffer

    real :: temp_val
    real :: precip_val
    real :: wind_val
    real :: battery_val

    real :: sum_temp
    real :: sum_precip
    real :: sum_wind
    real :: sum_battery

    real :: max_temp
    real :: min_temp
    real :: max_wind

    integer :: record_count
    integer :: io_status
    integer :: read_err

    integer :: p1
    integer :: p2
    integer :: p3
    integer :: p4
    integer :: p5

    ! --------------------------------------------------------------------------
    ! Open normalized input.
    ! --------------------------------------------------------------------------

    open(unit=10, file='data/datos_normalizados.csv', status='old', action='read', iostat=io_status)

    if (io_status /= 0) then
        print *, "Fortran ERROR: Could not open normalized input file."
        stop 1
    end if

    ! --------------------------------------------------------------------------
    ! Create metrics output.
    ! --------------------------------------------------------------------------

    open(unit=20, file='data/metricas.csv', status='replace', action='write', iostat=io_status)

    if (io_status /= 0) then
        print *, "Fortran ERROR: Could not create metrics output file."
        close(10)
        stop 1
    end if

    ! --------------------------------------------------------------------------
    ! Skip CSV header.
    ! --------------------------------------------------------------------------

    read(10, '(A)', iostat=io_status) line_buffer

    if (io_status /= 0) then
        print *, "Fortran ERROR: Normalized input is empty."
        close(10)
        close(20)
        stop 1
    end if

    ! --------------------------------------------------------------------------
    ! Initialize accumulators.
    ! --------------------------------------------------------------------------

    sum_temp = 0.0
    sum_precip = 0.0
    sum_wind = 0.0
    sum_battery = 0.0

    record_count = 0

    ! --------------------------------------------------------------------------
    ! Read normalized records.
    ! --------------------------------------------------------------------------

    do

        read(10, '(A)', iostat=io_status) line_buffer

        if (io_status /= 0) exit

        if (len_trim(line_buffer) == 0) cycle

        ! ----------------------------------------------------------------------
        ! Locate CSV separators.
        ! ----------------------------------------------------------------------

        p1 = index(line_buffer, ',')

        if (p1 <= 0) cycle

        p2 = p1 + index(line_buffer(p1 + 1:), ',')

        if (p2 <= p1) cycle

        p3 = p2 + index(line_buffer(p2 + 1:), ',')

        if (p3 <= p2) cycle

        p4 = p3 + index(line_buffer(p3 + 1:), ',')

        if (p4 <= p3) cycle

        p5 = p4 + index(line_buffer(p4 + 1:), ',')

        if (p5 <= p4) cycle

        ! ----------------------------------------------------------------------
        ! Read numerical fields.
        !
        ! ID,STATION,TEMPERATURE,PRECIPITATION,WIND,BATTERY
        ! ----------------------------------------------------------------------

        read(line_buffer(p2 + 1:p3 - 1), *, iostat=read_err) temp_val

        if (read_err /= 0) cycle

        read(line_buffer(p3 + 1:p4 - 1), *, iostat=read_err) precip_val

        if (read_err /= 0) cycle

        read(line_buffer(p4 + 1:p5 - 1), *, iostat=read_err) wind_val

        if (read_err /= 0) cycle

        read(line_buffer(p5 + 1:), *, iostat=read_err) battery_val

        if (read_err /= 0) cycle

        ! ----------------------------------------------------------------------
        ! Initialize min/max with first valid record.
        ! ----------------------------------------------------------------------

        if (record_count == 0) then
            max_temp = temp_val
            min_temp = temp_val
            max_wind = wind_val
        else

            if (temp_val > max_temp) then
                max_temp = temp_val
            end if

            if (temp_val < min_temp) then
                min_temp = temp_val
            end if

            if (wind_val > max_wind) then
                max_wind = wind_val
            end if

        end if

        ! ----------------------------------------------------------------------
        ! Update accumulators.
        ! ----------------------------------------------------------------------

        sum_temp = sum_temp + temp_val
        sum_precip = sum_precip + precip_val
        sum_wind = sum_wind + wind_val
        sum_battery = sum_battery + battery_val

        record_count = record_count + 1

    end do

    close(10)

    ! --------------------------------------------------------------------------
    ! Write metrics.
    ! --------------------------------------------------------------------------

    write(20, '(A)') 'METRIC,VALUE'

    if (record_count > 0) then

        write(20, '(A,F10.2)') 'AVERAGE_TEMPERATURE,', sum_temp / real(record_count)

        write(20, '(A,F10.2)') 'MAX_TEMPERATURE,', max_temp

        write(20, '(A,F10.2)') 'MIN_TEMPERATURE,', min_temp

        write(20, '(A,F10.2)') 'TOTAL_PRECIPITATION,', sum_precip

        write(20, '(A,F10.2)') 'AVERAGE_WIND,', sum_wind / real(record_count)

        write(20, '(A,F10.2)') 'MAX_WIND,', max_wind

        write(20, '(A,F10.2)') 'AVERAGE_BATTERY,', sum_battery / real(record_count)

        write(20, '(A,I0)') 'TOTAL_RECORDS,', record_count

        print *, "Fortran: Metrics calculated successfully."
        print *, "Fortran: Records processed:", record_count

    else

        print *, "Fortran ERROR: No normalized records available."

        close(20)
        stop 1

    end if

    close(20)

end program metrics_calculator