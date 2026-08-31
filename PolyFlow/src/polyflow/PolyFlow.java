package polyflow;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Main orchestrator for the PolyFlow polyglot data pipeline.
 *
 * <p>The pipeline integrates four programming languages:</p>
 *
 * <ol>
 *     <li>BASIC-256: data cleaning and validation</li>
 *     <li>Fortran: numerical processing</li>
 *     <li>Java: DSL parsing, rules, inheritance and polymorphism</li>
 *     <li>MIPS Assembly: final integrity checksum</li>
 * </ol>
 *
 * <p>The orchestrator contains no machine-specific absolute paths.
 * All project resources are resolved relative to the discovered project root.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 */
public final class PolyFlow {

    /**
     * Controls reduced console output.
     */
    private static boolean quietMode = false;

    /**
     * Prevents instantiation of the main orchestrator.
     */
    private PolyFlow() {
    }

    /**
     * Starts the complete PolyFlow pipeline.
     *
     * @param args optional command-line arguments
     */
    public static void main(String[] args) {

        quietMode = hasQuietFlag(args);

        if (!quietMode) {
            printHeader();
        }

        Path projectRoot;

        try {

            projectRoot = findProjectRoot();

        } catch (IllegalStateException exception) {

            handleFailure(
                    exception.getMessage());

            System.exit(1);
            return;
        }

        cleanIntermediateFiles(projectRoot);

        if (!runBasicStage(projectRoot)) {
            handleFailure("Stage 1 (BASIC-256)");
            System.exit(1);
            return;
        }

        if (!runFortranStage(projectRoot)) {
            handleFailure("Stage 2 (Fortran)");
            System.exit(1);
            return;
        }

        if (!runJavaStage(projectRoot)) {
            handleFailure("Stage 3 (Java)");
            System.exit(1);
            return;
        }

        if (!runMipsStage(projectRoot)) {
            handleFailure("Stage 4 (MIPS Assembly)");
            System.exit(1);
            return;
        }

        printSuccess(projectRoot);

        openHtmlReport(projectRoot);
    }

    /**
     * Determines whether quiet mode was requested.
     *
     * @param args command-line arguments
     * @return true when quiet mode is enabled
     */
    private static boolean hasQuietFlag(
            String[] args) {

        if (args == null || args.length == 0) {
            return false;
        }

        return "--quiet".equals(args[0])
                || "-q".equals(args[0]);
    }

    /**
     * Prints the pipeline header.
     */
    private static void printHeader() {

        System.out.println(
                "=================================================");

        System.out.println(
                "              POLYFLOW DATA PIPELINE");

        System.out.println(
                "=================================================");
    }

    /**
     * Executes Stage 1 using BASIC-256.
     *
     * @param projectRoot PolyFlow project root
     * @return true when the stage succeeds and creates its artifact
     */
    private static boolean runBasicStage(
            Path projectRoot) {

        if (quietMode) {
            System.out.print(
                    "[BASIC-256] Processing data... ");
        } else {
            System.out.println(
                    "\n[STAGE 1] BASIC-256 "
                    + "Data Cleaning and Validation");
        }

        Path basicDirectory =
                projectRoot.resolve("src/basic");

        Path basicProgram =
                basicDirectory.resolve(
                        "data_cleaner.kbs");

        Path normalizedFile =
                projectRoot.resolve(
                        "data/datos_normalizados.csv");

        Path inputFile =
                projectRoot.resolve(
                        "data/entrada.csv");

        if (!Files.isDirectory(basicDirectory)
                || !Files.isRegularFile(basicProgram)) {

            return false;
        }

        // Fail with a controlled message here rather than letting
        // BASIC-256 abort with a raw interpreter error, since that
        // language has no structured exception handling of its own.
        if (!verifyFileOutput(inputFile)) {

            if (!quietMode) {
                System.err.println(
                        "[ERROR] Missing or empty input file: "
                        + inputFile);
            }

            return false;
        }

        boolean success = executeCommand(
                "BASIC-256",
                basicDirectory.toFile(),
                "basic256",
                "-a",
                "data_cleaner.kbs");

        if (!success
                || !verifyFileOutput(normalizedFile)) {

            return false;
        }

        if (quietMode) {
            System.out.println("OK");
        } else {
            printFileSummary(
                    normalizedFile.toFile(),
                    "Normalized data");
        }

        return true;
    }

    /**
     * Executes Stage 2 using GNU Fortran.
     *
     * @param projectRoot PolyFlow project root
     * @return true when the stage succeeds
     */
    private static boolean runFortranStage(
            Path projectRoot) {

        if (quietMode) {
            System.out.print(
                    "[FORTRAN] Calculating metrics... ");
        } else {
            System.out.println(
                    "\n[STAGE 2] Fortran "
                    + "Numerical Processing");
        }

        Path source =
                projectRoot.resolve(
                        "src/fortran/metrics_calculator.f90");

        Path executable =
                projectRoot.resolve(
                        "bin/metrics_calculator.exe");

        Path metrics =
                projectRoot.resolve(
                        "data/metricas.csv");

        try {

            Files.createDirectories(
                    projectRoot.resolve("bin"));

        } catch (IOException exception) {

            return false;
        }

        boolean compiled = executeCommand(
                "Fortran Compile",
                projectRoot.toFile(),
                "gfortran",
                source.toString(),
                "-o",
                executable.toString());

        if (!compiled) {
            return false;
        }

        boolean executed = executeCommand(
                "Fortran Execute",
                projectRoot.toFile(),
                executable.toString());

        if (!executed
                || !verifyFileOutput(metrics)) {

            return false;
        }

        if (quietMode) {
            System.out.println("OK");
        } else {
            printFileSummary(
                    metrics.toFile(),
                    "Calculated metrics");
        }

        return true;
    }

    /**
     * Executes Stage 3 using the Java rule engine.
     *
     * @param projectRoot PolyFlow project root
     * @return true when all expected Java artifacts are produced
     */
    private static boolean runJavaStage(
            Path projectRoot) {

        if (quietMode) {
            System.out.print(
                    "[JAVA] Evaluating rules... ");
        } else {
            System.out.println(
                    "\n[STAGE 3] Java "
                    + "DSL Parser and Rule Engine");
        }

        try {

            RuleEvaluator.evaluateAndGenerateHtml(
                    projectRoot);

        } catch (IOException exception) {

            if (!quietMode) {
                System.err.println(
                        "[ERROR] Java stage: "
                        + exception.getMessage());
            }

            return false;
        }

        Path alerts =
                projectRoot.resolve(
                        "data/alertas.csv");

        Path sequence =
                projectRoot.resolve(
                        "data/secuencia.txt");

        Path report =
                projectRoot.resolve(
                        "data/reporte.html");

        if (!verifyFileOutput(alerts)
                || !verifyFileOutput(sequence)
                || !verifyFileOutput(report)) {

            return false;
        }

        if (quietMode) {
            System.out.println("OK");
        } else {

            printFileSummary(
                    alerts.toFile(),
                    "Generated alerts");

            printFileSummary(
                    sequence.toFile(),
                    "MIPS sequence");
        }

        return true;
    }

    /**
     * Executes Stage 4 using the MARS MIPS simulator.
     *
     * @param projectRoot PolyFlow project root
     * @return true when MIPS finishes successfully
     */
    private static boolean runMipsStage(
            Path projectRoot) {

        if (quietMode) {
            System.out.print(
                    "[MIPS] Calculating signature... ");
        } else {
            System.out.println(
                    "\n[STAGE 4] MIPS "
                    + "Integrity Verification");
        }

        Path mars =
                projectRoot.resolve("Mars.jar");

        Path source =
                projectRoot.resolve(
                        "src/mips/checksum_verifier.asm");

        Path sequence =
                projectRoot.resolve(
                        "data/secuencia.txt");

        if (!Files.isRegularFile(mars)
                || !Files.isRegularFile(source)
                || !verifyFileOutput(sequence)) {

            return false;
        }

        boolean success = executeCommand(
                "MIPS",
                projectRoot.toFile(),
                "java",
                "-jar",
                mars.toString(),
                "nc",
                "sm",
                "ae1",
                "se1",
                source.toString());

        if (quietMode && success) {
            System.out.println("OK");
        }

        return success;
    }

    /**
     * Locates the PolyFlow root by searching upward from the compiled
     * Java class location.
     *
     * @return project root
     * @throws IllegalStateException when the root cannot be located
     */
    private static Path findProjectRoot() {

        try {

            File location =
                    new File(
                            PolyFlow.class
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI());

            Path current;

            if (location.isFile()) {
                current = location
                        .toPath()
                        .getParent();
            } else {
                current = location.toPath();
            }

            while (current != null) {

                boolean hasData =
                        Files.isDirectory(
                                current.resolve("data"));

                boolean hasSource =
                        Files.isDirectory(
                                current.resolve("src"));

                boolean hasMars =
                        Files.isRegularFile(
                                current.resolve("Mars.jar"));

                if (hasData
                        && hasSource
                        && hasMars) {

                    return current.toAbsolutePath()
                            .normalize();
                }

                current = current.getParent();
            }

        } catch (URISyntaxException exception) {

            throw new IllegalStateException(
                    "Could not locate PolyFlow project root.",
                    exception);
        }

        throw new IllegalStateException(
                "PolyFlow project root not found.");
    }

    /**
     * Removes artifacts generated by previous executions.
     *
     * @param projectRoot PolyFlow project root
     */
    private static void cleanIntermediateFiles(
            Path projectRoot) {

        String[] relativeFiles = {
            "data/datos_normalizados.csv",
            "data/metricas.csv",
            "data/alertas.csv",
            "data/secuencia.txt",
            "data/reporte.html",
            "bin/metrics_calculator.exe"
        };

        for (String relativeFile : relativeFiles) {

            Path file =
                    projectRoot.resolve(relativeFile);

            try {
                Files.deleteIfExists(file);
            } catch (IOException exception) {

                if (!quietMode) {
                    System.err.println(
                            "[WARNING] Could not remove: "
                            + relativeFile);
                }
            }
        }
    }

    /**
     * Executes an external process.
     *
     * @param stageName human-readable process name
     * @param workingDirectory process working directory
     * @param command executable and arguments
     * @return true when the process exits with code zero
     */
    private static boolean executeCommand(
            String stageName,
            File workingDirectory,
            String... command) {

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(command);

            processBuilder.directory(
                    workingDirectory);

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        process.getInputStream()))
            ) {

                String line;

                while ((line =
                        reader.readLine()) != null) {

                    if (!quietMode) {

                        System.out.println(
                                "  ["
                                + stageName
                                + "] "
                                + line);
                    }
                }
            }

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                if (!quietMode) {
                    System.err.println(
                            "[ERROR] "
                            + stageName
                            + " exited with code "
                            + exitCode);
                }

                return false;
            }

            return true;

        } catch (IOException exception) {

            if (!quietMode) {

                System.err.println(
                        "[ERROR] Could not execute "
                        + stageName
                        + ": "
                        + exception.getMessage());
            }

            return false;

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            if (!quietMode) {

                System.err.println(
                        "[ERROR] "
                        + stageName
                        + " was interrupted.");
            }

            return false;
        }
    }

    /**
     * Verifies that an expected artifact exists and is not empty.
     *
     * @param file expected artifact
     * @return true when the artifact is a non-empty regular file
     */
    private static boolean verifyFileOutput(
            Path file) {

        try {

            return Files.isRegularFile(file)
                    && Files.size(file) > 0;

        } catch (IOException exception) {

            return false;
        }
    }

    /**
     * Prints a small preview of a generated artifact.
     *
     * @param file artifact to preview
     * @param description artifact description
     */
    private static void printFileSummary(
            File file,
            String description) {

        try {

            if (!file.isFile()) {
                return;
            }

            List<String> lines =
                    Files.readAllLines(
                            file.toPath());

            System.out.println(
                    "   [PIPE CONSOLE] "
                    + description
                    + " ("
                    + lines.size()
                    + " lines):");

            int limit =
                    Math.min(
                            lines.size(),
                            6);

            for (int i = 0;
                    i < limit;
                    i++) {

                System.out.println(
                        "     | "
                        + lines.get(i));
            }

            if (lines.size() > 6) {

                System.out.println(
                        "     | ... ("
                        + (lines.size() - 6)
                        + " additional lines)");
            }

        } catch (IOException exception) {

            if (!quietMode) {

                System.err.println(
                        "[WARNING] Could not preview "
                        + file.getName());
            }
        }
    }

    /**
     * Handles a pipeline failure.
     *
     * @param stageName failed stage
     */
    private static void handleFailure(
            String stageName) {

        if (quietMode) {

            System.out.println("ERROR");

            System.out.println(
                    "PIPELINE FAILED AT: "
                    + stageName);

        } else {

            System.err.println(
                    "\n[ERROR] Pipeline interrupted at: "
                    + stageName);
        }
    }

    /**
     * Prints the successful pipeline message.
     *
     * @param projectRoot PolyFlow project root
     */
    private static void printSuccess(
            Path projectRoot) {

        if (quietMode) {

            System.out.println();
            System.out.println(
                    "PIPELINE COMPLETED SUCCESSFULLY");

            return;
        }

        System.out.println(
                "\n=================================================");

        System.out.println(
                "       POLYFLOW PIPELINE COMPLETED SUCCESSFULLY");

        System.out.println(
                "=================================================");

        System.out.println(
                "Dashboard: data/reporte.html");
    }

    /**
     * Opens the generated HTML report using the native desktop browser.
     *
     * @param projectRoot PolyFlow project root
     */
    private static void openHtmlReport(
            Path projectRoot) {

        Path report =
                projectRoot.resolve(
                        "data/reporte.html");

        if (!Files.isRegularFile(report)) {
            return;
        }

        try {

            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop()
                    .isSupported(
                            Desktop.Action.BROWSE)) {

                Desktop.getDesktop()
                        .browse(
                                report.toUri());
            }

        } catch (IOException exception) {

            if (!quietMode) {

                System.err.println(
                        "[WARNING] Could not open "
                        + "HTML dashboard.");
            }
        }
    }
}