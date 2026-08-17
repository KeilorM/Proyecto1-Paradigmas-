rem ===================================================
rem Módulo PolyFlow: BASIC-256 (Limpieza)
rem Entrada: data/entrada.csv
rem Salida:  data/datos_normalizados.csv
rem ===================================================

open 1, "data/entrada.csv"
open 2, "data/datos_normalizados.csv"

linein 1, encabezado$
writeln 2, "ID,ESTACION,TEMPERATURA,PRECIPITACION,VIENTO,BATERIA"

while not eof(1)
    linein 1, linea$
    if length(linea$) > 0 then
        call procesarLinea(linea$)
    endif
end while

close 1
close 2
print "BASIC-256: Limpieza completada con exito."
end

subroutine procesarLinea(registro$)
    p1 = instr(registro$, ",")
    if p1 = 0 then return
    id$ = left(registro$, p1 - 1)
    resto1$ = mid(registro$, p1 + 1)

    p2 = instr(resto1$, ",")
    if p2 = 0 then return
    estacion$ = left(resto1$, p2 - 1)
    resto2$ = mid(resto1$, p2 + 1)

    p3 = instr(resto2$, ",")
    if p3 = 0 then return
    temp$ = left(resto2$, p3 - 1)
    resto3$ = mid(resto2$, p3 + 1)

    p4 = instr(resto3$, ",")
    if p4 = 0 then return
    precip$ = left(resto3$, p4 - 1)
    resto4$ = mid(resto3$, p4 + 1)

    p5 = instr(resto4$, ",")
    if p5 = 0 then return
    viento$ = left(resto4$, p5 - 1)
    bat$ = mid(resto4$, p5 + 1)

    if id$="" or estacion$="" or temp$="" or precip$="" or viento$="" or bat$="" then return

    t = val(temp$)
    p = val(precip$)
    v = val(viento$)
    b = val(bat$)

    valido = 1
    if t < -50 or t > 60 then valido = 0
    if p < 0 then valido = 0
    if v < 0 then valido = 0
    if b < 0 or b > 100 then valido = 0

    if valido = 1 then
        writeln 2, registro$
    endif
end subroutine