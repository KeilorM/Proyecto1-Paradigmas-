@echo off
title PolyFlow - Polyglot Data Pipeline Execution
echo Cleaning build directory...
if exist bin rmdir /s /q bin
mkdir bin

echo Compiling Java Orchestrator...
javac -d bin src/polyflow/PolyFlow.java src/polyflow/RuleEvaluator.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Java compilation failed! Check file paths in src/polyflow/
    pause
    exit /b %errorlevel%
)

echo.
echo Executing PolyFlow Pipeline...
java -cp bin polyflow.PolyFlow

:: Abre el archivo HTML directamente en el navegador por defecto
if exist data\reporte.html start "" "data\reporte.html"

echo.
pause