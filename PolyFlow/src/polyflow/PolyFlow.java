package polyflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class PolyFlow {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO PIPELINE POLYFLOW ===");

        // --- ETAPA 1: BASIC-256 ---
        System.out.println("\n[ETAPA 1] Ejecutando BASIC-256...");
        if (!ejecutarComando("BASIC-256", "basic256", "-a", "src/basic/limpieza.kbs")) return;

        // Crear la carpeta 'bin' si no existe
        new File("bin").mkdirs();

        // --- ETAPA 2: FORTRAN ---
        System.out.println("\n[ETAPA 2] Compilando Fortran...");
        if (!ejecutarComando("Fortran Compilacion", "gfortran", "src/fortran/metricas.f90", "-o", "bin/metricas.exe")) return;

        System.out.println("[ETAPA 2] Ejecutando Fortran...");
        if (!ejecutarComando("Fortran Ejecucion", "bin/metricas.exe")) return;

        System.out.println("\n=== PIPELINE EJECUTADO HASTA ETAPA 2 CON EXITO ===");
    }

    private static boolean ejecutarComando(String nombreEtapa, String... comando) {
        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.directory(new File(".")); // Garantiza la ejecucion en la raiz del proyecto
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    System.out.println("  [" + nombreEtapa + "] > " + linea);
                }
            }

            int exitCode = proceso.waitFor();
            if (exitCode == 0) {
                return true;
            } else {
                System.err.println("[ERROR] " + nombreEtapa + " fallo con codigo de salida: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo ejecutar " + nombreEtapa + ": " + e.getMessage());
            return false;
        }
    }
}