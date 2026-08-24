#!/bin/bash
clear

echo "==================================================="
echo "             COMPILANDO SCRIPT POLYFLOW             "
echo "==================================================="

# -----------------------------------------------------------------------------
# 1. DIRECCIONAMIENTO AL PROYECTO DESDE EL ESCRITORIO
# ¡IMPORTANTE! Cambia esta ruta por la ubicación real de tu carpeta del proyecto
# -----------------------------------------------------------------------------
cd "C:\Users\Laboratorio_M\Documents\Proyecto1-Paradigmas-\PolyFlow"

# Validar que realmente estamos en la carpeta del proyecto antes de continuar
if [ ! -d "src" ]; then
    echo ""
    echo "[ERROR] No se encontro la carpeta del proyecto en la ruta especificada."
    echo "Por favor, edita este script y verifica la ruta de la linea 14."
    echo ""
    read -p "Presiona [Enter] para salir..."
    exit 1
fi

# Limpieza y creación del directorio de binarios
rm -rf bin
mkdir -p bin

# Compilación de los módulos de Java
javac -d bin src/polyflow/PolyFlow.java src/polyflow/RuleEvaluator.java
if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Error de compilacion en Java."
    echo ""
    read -p "Presiona [Enter] para salir..."
    exit 1
fi

clear
echo "==================================================="
echo "        EJECUTANDO POLYFLOW DATA PIPELINE          "
echo "==================================================="
echo ""

# Ejecuta el flujo secuencial completo (BASIC -> Fortran -> Java -> MIPS)
java -cp bin polyflow.PolyFlow
JAVA_RESULT=$?

if [ $JAVA_RESULT -eq 0 ]; then
    echo ""
    echo "---------------------------------------------------"
    echo " PIPELINE COMPLETADO CON EXITO"
    echo "---------------------------------------------------"
    
    # -----------------------------------------------------------------------------
    # 2. APERTURA FORZADA DEL DASHBOARD HTML DESDE EL FONDO
    # -----------------------------------------------------------------------------
    HTML_PATH="$(pwd)/data/reporte.html"
    
    echo "[OK] Pipeline ejecutado con exito. Abriendo dashboard..."
    
    if [ -f "$HTML_PATH" ]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # --- SOLUCIÓN PARA MACOS (Lanza procesos al fondo con &) ---
            open "$HTML_PATH" 2>/dev/null &
            open -a "Safari" "$HTML_PATH" 2>/dev/null &
            open -a "Google Chrome" "$HTML_PATH" 2>/dev/null &
        else
            # --- SOLUCIÓN PARA LINUX (Evita el congelamiento de la consola) ---
            xdg-open "$HTML_PATH" >/dev/null 2>&1 &
            gnome-open "$HTML_PATH" >/dev/null 2>&1 &
            sensible-browser "$HTML_PATH" >/dev/null 2>&1 &
        fi
    else
        echo "[WARNING] El pipeline termino, pero no se encontro el archivo: $HTML_PATH"
    fi
else
    echo ""
    echo "[ERROR] El pipeline se interrumpio en alguna etapa."
fi

# -----------------------------------------------------------------------------
# 3. TRUCO DE CONGELAMIENTO DE TERMINAL (Evita el cierre de golpe)
# -----------------------------------------------------------------------------
echo ""
echo "==================================================="
echo "    PROCESO TERMINADO CON EXITO"
echo "==================================================="
echo ""
read -p "Presiona [Enter] para cerrar esta ventana..."
exit 0

