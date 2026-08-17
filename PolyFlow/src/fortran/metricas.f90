program metricas
    implicit none
    character(len=200) :: linea
    character(len=50) :: id_str, estacion, temp_str, precip_str, viento_str, bat_str
    real :: temp, precip, viento, bat
    real :: suma_temp, suma_precip, suma_viento, suma_bat
    integer :: count, io, p1, p2, p3, p4, p5

    open(unit=10, file='data/datos_normalizados.csv', status='old', action='read', iostat=io)
    if (io /= 0) then
        print *, "Error: No se pudo abrir data/datos_normalizados.csv"
        stop
    end if

    open(unit=20, file='data/metricas.txt', status='replace', action='write')

    ! Leer encabezado
    read(10, '(A)', iostat=io) linea

    suma_temp = 0.0
    suma_precip = 0.0
    suma_viento = 0.0
    suma_bat = 0.0
    count = 0

    do
        read(10, '(A)', iostat=io) linea
        if (io /= 0) exit
        if (len_trim(linea) == 0) cycle

        ! Parsear campos separados por comas
        p1 = index(linea, ',')
        p2 = p1 + index(linea(p1+1:), ',')
        p3 = p2 + index(linea(p2+1:), ',')
        p4 = p3 + index(linea(p3+1:), ',')
        p5 = p4 + index(linea(p4+1:), ',')

        read(linea(p2+1:p3-1), *) temp
        read(linea(p3+1:p4-1), *) precip
        read(linea(p4+1:p5-1), *) viento
        read(linea(p5+1:), *) bat

        suma_temp = suma_temp + temp
        suma_precip = suma_precip + precip
        suma_viento = suma_viento + viento
        suma_bat = suma_bat + bat
        count = count + 1
    end do

    close(10)

    if (count > 0) then
        write(20, '(A,F8.2)') "PROMEDIO_TEMPERATURA=", suma_temp / count
        write(20, '(A,F8.2)') "PROMEDIO_PRECIPITACION=", suma_precip / count
        write(20, '(A,F8.2)') "PROMEDIO_VIENTO=", suma_viento / count
        write(20, '(A,F8.2)') "PROMEDIO_BATERIA=", suma_bat / count
        write(20, '(A,I0)')   "TOTAL_REGISTROS=", count
        print *, "Fortran: Metricas calculadas exitosamente."
    else
        print *, "Fortran: No se encontraron registros para procesar."
    end if

    close(20)
end program metricas