# COMO EJECUTAR EL PROGRAMA

## Requisitos

Antes de ejecutar PolyFlow, se debe contar con:

* JDK 21 o superior (`java` y `javac`).
* BASIC-256 instalado y disponible en el `PATH`.
* GNU Fortran (`gfortran`) instalado y disponible en el `PATH`.
* `Mars.jar` ubicado en la raíz del proyecto.

La estructura esperada es:

PolyFlow/
├── Mars.jar
├── data/
│   ├── entrada.csv
│   └── reglas.txt
├── src/
│   ├── basic/
│   │   └── data_cleaner.kbs
│   ├── fortran/
│   │   └── metrics_calculator.f90
│   ├── mips/
│   │   └── checksum_verifier.asm
│   └── polyflow/
│       ├── PolyFlow.java
│       └── RuleEvaluator.java
├── run.bat
└── run_pipeline.sh

## Ejecución en Windows

Desde la raíz del proyecto se puede ejecutar:

run.bat

También es posible hacer doble clic sobre `run.bat`.

El script:

1. Verifica que estén presentes los archivos necesarios.
2. Configura el entorno de Java.
3. Elimina los archivos compilados anteriores de `bin/`.
4. Compila `PolyFlow.java` y `RuleEvaluator.java`.
5. Ejecuta el orquestador Java.
6. El orquestador ejecuta las cuatro etapas del pipeline:

   * BASIC-256.
   * Fortran.
   * Java.
   * MIPS.
7. Al finalizar correctamente, se genera el dashboard en:

data/reporte.html

## Ejecución en Linux, macOS, Git Bash o WSL

Desde la raíz del proyecto:

chmod +x run_pipeline.sh
./run_pipeline.sh

El script limpia los archivos compilados anteriores, compila el orquestador Java
y ejecuta el pipeline completo. El orquestador limpia los artefactos generados 
por ejecuciones anteriores antes de iniciar las etapas.

En caso de que el sistema tenga más de un JDK instalado, se puede indicar 
explícitamente la ubicación del JDK 21 mediante `JAVA21_HOME`.

Ejemplo:

JAVA21_HOME="/c/Program Files/Java/jdk-21" ./run_pipeline.sh

## Resultado esperado

Una ejecución exitosa muestra que las cuatro etapas fueron completadas 
y termina con:

POLYFLOW PIPELINE COMPLETED SUCCESSFULLY

Los principales archivos generados son:

data/datos_normalizados.csv
data/metricas.csv
data/alertas.csv
data/secuencia.txt
data/reporte.html

El checksum final de MIPS se muestra directamente en la consola.

El dashboard puede abrirse posteriormente desde:

data/reporte.html

## Importante

`data/entrada.csv` y `data/reglas.txt` son archivos de entrada del sistema y 
deben estar presentes antes de ejecutar el pipeline. Los demás archivos de 
resultados son generados automáticamente durante la ejecución.
