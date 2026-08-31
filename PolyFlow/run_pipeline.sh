#!/bin/bash
# ==============================================================================
# PolyFlow - Polyglot Data Pipeline Launcher
# Authors: Randall AC, Keilor MC
#
# Description:
#   Compiles the Java orchestration layer and executes the complete PolyFlow
#   pipeline:
#
#       BASIC-256 -> Fortran -> Java -> MIPS
#
#   The script uses only project-relative paths and does not depend on any
#   machine-specific absolute paths.
# ==============================================================================
clear
echo "==================================================="
echo "             POLYFLOW DATA PIPELINE                "
echo "==================================================="
echo ""
# Always execute from the project root directory.
cd "$(dirname "$0")" || {
    echo "[ERROR] Could not access the project directory."
    exit 1
}
# ------------------------------------------------------------------------------
# 1. Locate a JDK 21+ toolchain.
#
#   The system PATH may point to an older JDK (e.g. Java 8, kept for tools
#   like NetBeans' unpack200). Rather than requiring that PATH be changed,
#   this script looks for a JDK 21+ install in common Windows locations
#   first, and only falls back to whatever "java"/"javac" are on PATH if
#   none is found. JAVAC_CMD/JAVA_CMD are used for every subsequent call
#   instead of the bare "javac"/"java" commands.
# ------------------------------------------------------------------------------

JAVAC_CMD=""
JAVA_CMD=""

for candidate in \
    /c/Program\ Files/Java/jdk-2* \
    /c/Program\ Files/Java/jdk21* \
    /c/Program\ Files/Eclipse\ Adoptium/jdk-2* \
    /c/Program\ Files/Microsoft/jdk-2* \
    /c/Program\ Files/BellSoft/LibericaJDK-2* \
    /c/Program\ Files/Zulu/zulu-2* \
    "$USERPROFILE"/.jdks/*2*
do
    if [ -x "$candidate/bin/javac.exe" ] && [ -x "$candidate/bin/java.exe" ]; then
        JAVAC_CMD="$candidate/bin/javac.exe"
        JAVA_CMD="$candidate/bin/java.exe"
        break
    fi
done

if [ -z "$JAVAC_CMD" ]; then
    # No JDK 21+ auto-detected: fall back to PATH.
    if command -v javac >/dev/null 2>&1; then
        JAVAC_CMD="javac"
    else
     echo "[ERROR] No JDK 21+ was found automatically, and javac was not found in PATH."
     echo "Set JAVA21_HOME to your JDK 21 install directory and re-run, e.g.:"
     echo "  JAVA21_HOME=\"/c/Program Files/Java/jdk-21.0.4\" ./run_pipeline.sh"
     exit 1
    fi

    if command -v java >/dev/null 2>&1; then
        JAVA_CMD="java"
    else
     echo "[ERROR] No JDK 21+ was found automatically, and java was not found in PATH."
     exit 1
    fi
fi

# Explicit override always wins, in case auto-detection picks the wrong JDK.
if [ -n "$JAVA21_HOME" ]; then
    JAVAC_CMD="$JAVA21_HOME/bin/javac.exe"
    JAVA_CMD="$JAVA21_HOME/bin/java.exe"
fi

echo "[OK] Using javac: $JAVAC_CMD"
echo "[OK] Using java:  $JAVA_CMD"
echo ""

"$JAVAC_CMD" -version >/dev/null 2>&1 || {
    echo "[ERROR] The selected javac could not be executed: $JAVAC_CMD"
    exit 1
}
# ------------------------------------------------------------------------------
# 2. Clean previous Java compilation artifacts
# ------------------------------------------------------------------------------
rm -rf bin
mkdir -p bin
# ------------------------------------------------------------------------------
# 3. Compile Java orchestration modules
# ------------------------------------------------------------------------------
echo "==================================================="
echo "                 COMPILING POLYFLOW                "
echo "==================================================="
echo ""
"$JAVAC_CMD" -d bin \
    src/polyflow/PolyFlow.java \
    src/polyflow/RuleEvaluator.java
if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Java compilation failed."
    echo ""
    exit 1
fi
echo ""
echo "[OK] Java compilation completed successfully."
echo ""
# ------------------------------------------------------------------------------
# 4. Execute complete polyglot pipeline (quiet mode: terse per-stage OK/ERROR
#    output, matching the format requested by the optional automation
#    challenge). The ASCII banner is suppressed by PolyFlow.java itself when
#    --quiet is passed, so only this script's own header is shown.
# ------------------------------------------------------------------------------
clear
echo "==================================================="
echo "             EXECUTING POLYFLOW PIPELINE           "
echo "==================================================="
echo ""
"$JAVA_CMD" -cp bin polyflow.PolyFlow --quiet
JAVA_RESULT=$?
if [ $JAVA_RESULT -ne 0 ]; then
    echo ""
    echo "[ERROR] The pipeline was interrupted by a stage failure."
    echo ""
    exit $JAVA_RESULT
fi
echo ""
echo "==================================================="
echo "           POLYFLOW FINISHED SUCCESSFULLY          "
echo "==================================================="
echo ""
exit 0