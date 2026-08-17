package polyflow;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PolyFlow {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO PIPELINE POLYFLOW ===");
        
        try {
            System.out.println("\n[ETAPA 1] Ejecutando BASIC-256...");
            ProcessBuilder pb = new ProcessBuilder("basic256", "--headless", "src/basic/limpieza.k");
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    System.out.println("  > " + linea);
                }
            }

            int exitCode = proceso.waitFor();
            if (exitCode == 0) {
                System.out.println("[ETAPA 1] Completada. Revisa data/datos_normalizados.csv");
            } else {
                System.out.println("[ERROR] BASIC-256 finalizo con codigo: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo ejecutar BASIC-256. Verifica que esté en el PATH.");
            e.printStackTrace();
        }
    }
}