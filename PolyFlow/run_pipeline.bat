@echo off
setlocal
title PolyFlow - Polyglot Data Pipeline Execution

REM ============================================================
REM PolyFlow - Main Pipeline Launcher
REM Authors: Randall AC, Keilor MC
REM ============================================================

REM Execute from the directory containing this script.
cd /d "%~dp0"

cls
echo ============================================================
echo                 POLYFLOW DATA PIPELINE
echo ============================================================
echo.

REM ------------------------------------------------------------
REM 1. Validate project structure
REM ------------------------------------------------------------

if not exist "src\polyflow\PolyFlow.java" (
echo [ERROR] PolyFlow.java was not found.
pause
exit /b 1
)

if not exist "src\polyflow\RuleEvaluator.java" (
echo [ERROR] RuleEvaluator.java was not found.
pause
exit /b 1
)

if not exist "src\basic\data_cleaner.kbs" (
echo [ERROR] BASIC-256 source file was not found.
pause
exit /b 1
)

if not exist "src\fortran\metrics_calculator.f90" (
echo [ERROR] Fortran source file was not found.
pause
exit /b 1
)

if not exist "src\mips\checksum_verifier.asm" (
echo [ERROR] MIPS source file was not found.
pause
exit /b 1
)

if not exist "data\entrada.csv" (
echo [ERROR] Input file data\entrada.csv was not found.
pause
exit /b 1
)

if not exist "data\reglas.txt" (
echo [ERROR] Rules file data\reglas.txt was not found.
pause
exit /b 1
)

if not exist "Mars.jar" (
echo [ERROR] Mars.jar was not found.
pause
exit /b 1
)

REM ------------------------------------------------------------
REM 2. Configure Java environment
REM ------------------------------------------------------------

set "JAVAC_CMD=javac"
set "JAVA_CMD=java"

if defined JAVA_HOME (
if exist "%JAVA_HOME%\bin\javac.exe" (
set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
)
if exist "%JAVA_HOME%\bin\java.exe" (
set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
)
)

REM Verify Java compiler
"%JAVAC_CMD%" -version >nul 2>&1

if errorlevel 1 (
echo [ERROR] Java compiler not available.
echo Please configure JDK 21 or make javac available through PATH.
pause
exit /b 1
)

REM Verify Java runtime
"%JAVA_CMD%" -version >nul 2>&1

if errorlevel 1 (
echo [ERROR] Java runtime not available.
pause
exit /b 1
)

REM ------------------------------------------------------------
REM 3. Clean Java compilation artifacts
REM ------------------------------------------------------------

echo ============================================================
echo                 PREPARING POLYFLOW
echo ============================================================
echo.

if exist "bin" (
rmdir /s /q "bin"
)

mkdir "bin"

if errorlevel 1 (
echo [ERROR] Could not create the bin directory.
pause
exit /b 1
)

echo [OK] Build directory prepared.
echo.

REM ------------------------------------------------------------
REM 4. Compile Java modules
REM
REM NOTE: kept as a single line on purpose. The "^" line-continuation
REM character used by cmd.exe only works reliably when the .bat file
REM has Windows-style (CRLF) line endings; a file saved or copied with
REM Unix-style (LF) endings breaks the continuation and javac receives
REM a literal "^" as an argument ("invalid flag: ^"). A single line
REM avoids the problem entirely regardless of how the file was saved.
REM ------------------------------------------------------------

echo ============================================================
echo                 COMPILING POLYFLOW
echo ============================================================
echo.

"%JAVAC_CMD%" -encoding UTF-8 -d "bin" "src\polyflow\PolyFlow.java" "src\polyflow\RuleEvaluator.java"

if errorlevel 1 (
echo.
echo [ERROR] Java compilation failed.
pause
exit /b 1
)

echo.
echo [OK] Java compilation completed successfully.
echo.

REM ------------------------------------------------------------
REM 5. Execute complete polyglot pipeline
REM ------------------------------------------------------------

echo ============================================================
echo              EXECUTING POLYFLOW PIPELINE
echo ============================================================
echo.

"%JAVA_CMD%" -cp "bin" polyflow.PolyFlow

if errorlevel 1 (
echo.
echo [ERROR] The pipeline was interrupted due to a stage failure.
pause
exit /b 1
)

echo.
echo ============================================================
echo            POLYFLOW FINISHED SUCCESSFULLY
echo ============================================================
echo.

pause
endlocal
exit /b 0