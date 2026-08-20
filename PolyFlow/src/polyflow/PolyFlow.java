package polyflow;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * =============================================================================
 * MAIN MODULE: PolyFlow Orchestrator Project: PolyFlow - Integrated Polyglot
 * Data Pipeline Authors: Randall AC, Keilor MC
 *
 * Description: Central Java orchestrator managing the sequential execution of
 * the heterogeneous pipeline (BASIC-256 -> Fortran -> Java -> MIPS Assembly).
 * Ensures pre-run cleanup, dynamic path resolution, real-time console logging,
 * and inter-stage contract validation.
 * =============================================================================
 */
public class PolyFlow {

    public static void main(String[] args) {
        System.out.println("===================================================");
        System.out.println("        POLYFLOW - POLYGLOT DATA PIPELINE          ");
        System.out.println("===================================================");

        // Pre-run cleanup of artifacts from previous executions
        cleanIntermediateFiles();
        prepareBasicScript();

        // ---------------------------------------------------------------------
        // STAGE 1: BASIC-256 (Data Cleaning & Normalization)
        // ---------------------------------------------------------------------
        System.out.println("\n[STAGE 1] Executing BASIC-256 (Data Cleaning)...");
        if (!executeCommand("BASIC-256", "basic256", "-a", "src/basic/data_cleaner.kbs")) {
            System.err.println("[ERROR] Stage 1 execution failed.");
            return;
        }
        printFileSummary("data/datos_normalizados.csv", "Cleaned Records Output (CSV)");

        // Ensure binaries directory exists
        new File("bin").mkdirs();

        // ---------------------------------------------------------------------
        // STAGE 2: FORTRAN (Numerical Analysis & Metrics Computation)
        // ---------------------------------------------------------------------
        System.out.println("\n[STAGE 2] Compiling & Executing Fortran (Numerical Analysis)...");
        if (!executeCommand("Fortran Compile", "gfortran",
                "src/fortran/metrics_calculator.f90", "-o",
                "bin/metrics_calculator.exe")) {
            System.err.println("[ERROR] Fortran compilation failed.");
            return;
        }
        if (!executeCommand("Fortran Execute", "bin/metrics_calculator.exe")) {
            System.err.println("[ERROR] Fortran binary execution failed.");
            return;
        }
        printFileSummary("data/metricas.csv", "Calculated Metrics Output (CSV)");

        // ---------------------------------------------------------------------
        // STAGE 3: JAVA (DSL Rule Engine, Polymorphism & HTML Dashboard)
        // ---------------------------------------------------------------------
        System.out.println("\n[STAGE 3] Java Evaluator (DSL Grammar, Parser & Polymorphism)...");
        try {
            RuleEvaluator.evaluateAndGenerateHtml();
            printFileSummary("data/alertas.csv", "Generated Alerts Output (CSV)");
            printFileSummary("data/secuencia.txt", "Numeric Sequence for MIPS (TXT)");
        } catch (IOException e) {
            System.err.println("[ERROR] Java Stage 3 failed: " + e.getMessage());
            return;
        }

        // ---------------------------------------------------------------------
        // STAGE 4: MIPS ASSEMBLY (Integrity Checksum Verification via MARS)
        // ---------------------------------------------------------------------
        System.out.println("\n[STAGE 4] Verifying System Integrity with MIPS Assembly...");
        if (!executeCommand("MIPS Verification", "java", "-jar",
                "Mars.jar", "nc", "sm", "src/mips/checksum_verifier.asm")) {
            System.err.println("[ERROR] MIPS Assembly verification failed.");
            return;
        }

        // Successful execution completion
        System.out.println("\n===================================================");
        System.out.println("   === PIPELINE EXECUTED WITH TOTAL SUCCESS ===");
        System.out.println("===================================================");
        System.out.println("-> HTML Dashboard available at: data/reporte.html");
    }

    /**
     * Prints the top lines of a generated file to audit its contents in the
     * console.
     *
     * @param filePath path to the file being audited
     * @param description readable label describing the output
     */
    private static void printFileSummary(String filePath, String description) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                System.out.println("   [PIPE CONSOLE] " + description + " (" + lines.size() + " lines):");
                int limit = Math.min(lines.size(), 6);
                for (int i = 0; i < limit; i++) {
                    System.out.println("     | " + lines.get(i));
                }
                if (lines.size() > 6) {
                    System.out.println("     | ... (" + (lines.size() - 6) + " additional lines)");
                }
            }
        } catch (IOException ignored) {
            // Silent catch if temporary file access fails
        }
    }

    /**
     * Dynamically injects absolute system paths into the BASIC-256 KBS script.
     */
    private static void prepareBasicScript() {
        try {
            String inputAbs = new File("data/entrada.csv").getAbsolutePath().replace("\\", "/");
            String outputAbs = new File("data/datos_normalizados.csv").getAbsolutePath().replace("\\", "/");

            Path scriptPath = Paths.get("src/basic/data_cleaner.kbs");
            String content = Files.readString(scriptPath);

            content = content.replaceAll("open 1, \".*?\"", "open 1, \"" + inputAbs + "\"");
            content = content.replaceAll("open 2, \".*?\"", "open 2, \"" + outputAbs + "\"");

            Files.writeString(scriptPath, content);
        } catch (IOException e) {
            System.err.println("[WARNING] Could not update BASIC paths: " + e.getMessage());
        }
    }

    /**
     * Ensures cleanup of intermediate files from previous execution runs.
     */
    private static void cleanIntermediateFiles() {
        String[] filesToDelete = {
            "data/datos_normalizados.csv",
            "data/metricas.csv",
            "data/secuencia.txt",
            "data/alertas.csv",
            "data/reporte.html",
            "bin/metrics_calculator.exe"
        };
        for (String filePath : filesToDelete) {
            File f = new File(filePath);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * Executes external console commands while streaming stdout/stderr in
     * real-time.
     *
     * @param stageName descriptive identifier for logging
     * @param command array of command-line tokens
     * @return true if process completed with exit code 0, false otherwise
     */
    private static boolean executeCommand(String stageName, String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File("."));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("  [" + stageName + "] > " + line);
                }
            }

            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Could not execute " + stageName + ": " + e.getMessage());
            return false;
        }
    }
}
