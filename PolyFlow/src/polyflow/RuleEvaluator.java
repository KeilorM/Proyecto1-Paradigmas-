package polyflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates Domain-Specific Language (DSL) rules against calculated
 * environmental metrics. Implements Object-Oriented Programming principles
 * including inheritance and polymorphism, generating alerts, MIPS-compatible
 * numeric sequences, and an interactive HTML Dashboard.
 *
 * @author Randall AC
 * @author Keilor MC
 */
public class RuleEvaluator {

    /**
     * Abstract base class representing a Domain-Specific Language (DSL) rule.
     * Demonstrates abstraction and inheritance for concrete rule evaluation.
     */
    public static abstract class Regla {

        protected String operador;
        protected double valorUmbral;

        /**
         * Constructs a rule with a relational operator and a threshold value.
         *
         * @param operador the comparison operator (e.g., ">", "<", ">=", "<=")
         * @param valorUmbral the numeric boundary limit
         */
        public Regla(String operador, double valorUmbral) {
            this.operador = operador;
            this.valorUmbral = valorUmbral;
        }

        /**
         * Polymorphic method to evaluate whether a metric triggers this rule.
         *
         * @param valorActual the current value of the metric
         * @return true if the rule conditions are met, false otherwise
         */
        public abstract boolean evaluar(double valorActual);

        /**
         * Returns the numeric protocol code assigned for MIPS validation.
         *
         * @return internal MIPS protocol code
         */
        public abstract int getCodigoMips();

        /**
         * Returns the DSL identifier of the rule.
         *
         * @return rule name (e.g., TEMP_ALTA)
         */
        public abstract String getNombreRegla();

        /**
         * Returns the human-readable description of the associated metric.
         *
         * @return descriptive metric label
         */
        public abstract String getMetricaTexto();

        /**
         * Returns the physical measurement unit.
         *
         * @return measurement unit symbol
         */
        public abstract String getUnidad();

        /**
         * Helper method performing relational scalar comparisons.
         *
         * @param valorActual the value being evaluated
         * @return result of relational comparison
         */
        protected boolean comparar(double valorActual) {
            return switch (operador) {
                case ">" ->
                    valorActual > valorUmbral;
                case "<" ->
                    valorActual < valorUmbral;
                case ">=" ->
                    valorActual >= valorUmbral;
                case "<=" ->
                    valorActual <= valorUmbral;
                case "==" ->
                    valorActual == valorUmbral;
                default ->
                    false;
            };
        }
    }

    /**
     * Concrete polymorphic rule implementation for high temperature alerts.
     */
    public static class ReglaTemperatura extends Regla {

        public ReglaTemperatura(String op, double val) {
            super(op, val);
        }

        @Override
        public boolean evaluar(double v) {
            return comparar(v);
        }

        @Override
        public int getCodigoMips() {
            return 10;
        }

        @Override
        public String getNombreRegla() {
            return "TEMP_ALTA";
        }

        @Override
        public String getMetricaTexto() {
            return "Average Temperature";
        }

        @Override
        public String getUnidad() {
            return "&deg;C";
        }
    }

    /**
     * Concrete polymorphic rule implementation for heavy precipitation alerts.
     */
    public static class ReglaPrecipitacion extends Regla {

        public ReglaPrecipitacion(String op, double val) {
            super(op, val);
        }

        @Override
        public boolean evaluar(double v) {
            return comparar(v);
        }

        @Override
        public int getCodigoMips() {
            return 20;
        }

        @Override
        public String getNombreRegla() {
            return "LLUVIA_INTENSA";
        }

        @Override
        public String getMetricaTexto() {
            return "Average Precipitation";
        }

        @Override
        public String getUnidad() {
            return "mm";
        }
    }

    /**
     * Concrete polymorphic rule implementation for strong wind speed alerts.
     */
    public static class ReglaViento extends Regla {

        public ReglaViento(String op, double val) {
            super(op, val);
        }

        @Override
        public boolean evaluar(double v) {
            return comparar(v);
        }

        @Override
        public int getCodigoMips() {
            return 30;
        }

        @Override
        public String getNombreRegla() {
            return "VIENTO_FUERTE";
        }

        @Override
        public String getMetricaTexto() {
            return "Average Wind Speed";
        }

        @Override
        public String getUnidad() {
            return "km/h";
        }
    }

    /**
     * Concrete polymorphic rule implementation for low battery status alerts.
     */
    public static class ReglaBateria extends Regla {

        public ReglaBateria(String op, double val) {
            super(op, val);
        }

        @Override
        public boolean evaluar(double v) {
            return comparar(v);
        }

        @Override
        public int getCodigoMips() {
            return 40;
        }

        @Override
        public String getNombreRegla() {
            return "BATERIA_BAJA";
        }

        @Override
        public String getMetricaTexto() {
            return "Average Battery";
        }

        @Override
        public String getUnidad() {
            return "%";
        }
    }

    /**
     * Parses the grammar rules defined in the DSL rule file. Enforces strict
     * token validation: &lt;identifier&gt; &lt;operator&gt; &lt;number&gt;.
     *
     * @param path file path pointing to rules file (e.g., 'data/reglas.txt')
     * @return a map associating metric keys with instantiated polymorphic Regla
     * objects
     * @throws IOException if an error occurs reading the file
     */
    public static Map<String, Regla> parsearReglas(Path path) throws IOException {
        Map<String, Regla> mapa = new HashMap<>();
        if (!Files.exists(path)) {
            return mapa;
        }

        List<String> lineas = Files.readAllLines(path);
        for (String line : lineas) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            if (tokens.length != 3) {
                continue; // Skip malformed lines for stress resilience
            }
            String id = tokens[0];
            String op = tokens[1];

            try {
                double val = Double.parseDouble(tokens[2]);

                switch (id) {
                    case "TEMP_ALTA" ->
                        mapa.put("AVERAGE_TEMPERATURE", new ReglaTemperatura(op, val));
                    case "LLUVIA_INTENSA" ->
                        mapa.put("AVERAGE_PRECIPITATION", new ReglaPrecipitacion(op, val));
                    case "VIENTO_FUERTE" ->
                        mapa.put("AVERAGE_WIND", new ReglaViento(op, val));
                    case "BATERIA_BAJA" ->
                        mapa.put("AVERAGE_BATTERY", new ReglaBateria(op, val));
                }
            } catch (NumberFormatException ignored) {
                // Stress test safety: ignore invalid numeric thresholds
            }
        }
        return mapa;
    }

    /**
     * Executes the main stage 3 workflow: reads metrics, evaluates DSL rules,
     * exports alerts CSV and MIPS sequence text files, and generates the HTML
     * Dashboard.
     *
     * @throws IOException if file reading or writing operations fail
     */
    public static void evaluateAndGenerateHtml() throws IOException {
        Path metricsPath = Paths.get("data/metricas.csv");
        Map<String, Double> metricas = new HashMap<>();

        if (Files.exists(metricsPath)) {
            for (String line : Files.readAllLines(metricsPath)) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        metricas.put(parts[0].trim(), Double.valueOf(parts[1].trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        Map<String, Regla> reglas = parsearReglas(Paths.get("data/reglas.txt"));

        List<String> alertasCsv = new ArrayList<>();
        List<String> codigosMips = new ArrayList<>();
        StringBuilder htmlAlertsRows = new StringBuilder();

        for (Map.Entry<String, Regla> entry : reglas.entrySet()) {
            String claveMetrica = entry.getKey();
            Regla regla = entry.getValue();

            if (metricas.containsKey(claveMetrica)) {
                double valorActual = metricas.get(claveMetrica);
                if (regla.evaluar(valorActual)) {
                    String unidadConsola = regla.getUnidad().replace("&deg;", "");
                    String msg = regla.getNombreRegla() + ": " + regla.getMetricaTexto()
                            + " (" + valorActual + " " + unidadConsola + ") triggered rule "
                            + regla.operador + " " + regla.valorUmbral;

                    alertasCsv.add(msg);
                    codigosMips.add(String.valueOf(regla.getCodigoMips()));

                    // HTML row output with &deg;C rendering
                    htmlAlertsRows.append("<tr>")
                            .append("<td><span class='badge badge-danger'>").append(regla.getNombreRegla()).append("</span></td>")
                            .append("<td>").append(regla.getMetricaTexto()).append("</td>")
                            .append("<td><b>").append(valorActual).append(" ").append(regla.getUnidad()).append("</b></td>")
                            .append("<td><code>").append(regla.operador).append(" ").append(regla.valorUmbral).append("</code></td>")
                            .append("</tr>");
                }
            }
        }

        if (alertasCsv.isEmpty()) {
            alertasCsv.add("SYSTEM OK: All parameters within operational limits.");
            codigosMips.add("0");
            htmlAlertsRows.append("<tr><td colspan='4' style='text-align:center; color:#2e7d32;'><b>✓ All parameters are within safe operational limits.</b></td></tr>");
        }

        Files.write(Paths.get("data/alertas.csv"), alertasCsv);
        Files.write(Paths.get("data/secuencia.txt"), codigosMips);

        // HTML Dashboard Construction
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>")
                .append("<title>PolyFlow Analytics Dashboard</title>")
                .append("<style>")
                .append("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }")
                .append("body { background-color: #f4f6f9; color: #333; padding: 30px; }")
                .append(".header { display: flex; justify-content: space-between; align-items: center; background: #1e293b; color: #fff; padding: 20px 30px; border-radius: 12px; margin-bottom: 25px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }")
                .append(".header h1 { font-size: 24px; font-weight: 600; }")
                .append(".header .status { background: #10b981; color: white; padding: 6px 16px; border-radius: 20px; font-size: 13px; font-weight: bold; }")
                .append(".grid-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px; margin-bottom: 25px; }")
                .append(".card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); border-left: 5px solid #2563eb; }")
                .append(".card.temp { border-color: #ef4444; } .card.rain { border-color: #3b82f6; } .card.wind { border-color: #8b5cf6; } .card.bat { border-color: #10b981; }")
                .append(".card h3 { font-size: 13px; text-transform: uppercase; color: #64748b; margin-bottom: 8px; }")
                .append(".card .value { font-size: 28px; font-weight: bold; color: #0f172a; }")
                .append(".panel { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }")
                .append(".panel h2 { font-size: 18px; margin-bottom: 15px; color: #1e293b; border-bottom: 2px solid #f1f5f9; padding-bottom: 10px; }")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }")
                .append("th, td { text-align: left; padding: 12px 15px; border-bottom: 1px solid #e2e8f0; }")
                .append("th { background-color: #f8fafc; color: #475569; font-size: 13px; text-transform: uppercase; }")
                .append(".badge { padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: bold; }")
                .append(".badge-danger { background-color: #fee2e2; color: #dc2626; }")
                .append("code { background: #f1f5f9; padding: 3px 6px; border-radius: 4px; font-family: monospace; color: #475569; }")
                .append("</style></head><body>")
                // Header
                .append("<div class='header'>")
                .append("<div><h1>PolyFlow Environmental Analytics</h1><p style='color:#94a3b8; font-size:13px;'>Automated Polyglot Pipeline Executed</p></div>")
                .append("<div class='status'>PIPELINE SUCCESS</div>")
                .append("</div>")
                // Metric Cards
                .append("<div class='grid-cards'>")
                .append("<div class='card temp'><h3>Avg Temperature</h3><div class='value'>").append(metricas.getOrDefault("AVERAGE_TEMPERATURE", 0.0)).append(" &deg;C</div></div>")
                .append("<div class='card rain'><h3>Avg Precipitation</h3><div class='value'>").append(metricas.getOrDefault("AVERAGE_PRECIPITATION", 0.0)).append(" mm</div></div>")
                .append("<div class='card wind'><h3>Avg Wind Speed</h3><div class='value'>").append(metricas.getOrDefault("AVERAGE_WIND", 0.0)).append(" km/h</div></div>")
                .append("<div class='card bat'><h3>Avg Battery</h3><div class='value'>").append(metricas.getOrDefault("AVERAGE_BATTERY", 0.0)).append(" %</div></div>")
                .append("</div>")
                // Rules Alerts Panel
                .append("<div class='panel'>")
                .append("<h2>DSL Rule Evaluation Engine</h2>")
                .append("<table><thead><tr><th>Rule ID</th><th>Metric</th><th>Current Value</th><th>Threshold Rule</th></tr></thead><tbody>")
                .append(htmlAlertsRows.toString())
                .append("</tbody></table>")
                .append("</div></body></html>");

        Files.writeString(Paths.get("data/reporte.html"), html.toString(), StandardCharsets.UTF_8);
        System.out.println("Java: Professional Dashboard HTML generated.");
    }
}
